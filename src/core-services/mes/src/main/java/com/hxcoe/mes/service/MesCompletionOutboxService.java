package com.hxcoe.mes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.dto.scm.MesCompletionFactDTO;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.common.result.Result;
import com.hxcoe.mes.client.ScmIntegrationClient;
import com.hxcoe.mes.entity.IntegrationTaskEntity;
import com.hxcoe.mes.entity.WorkOrderEntity;
import com.hxcoe.mes.repository.IntegrationTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * MES→SCM 工单完工事实 Outbox 服务（B6 闭环推送端）。
 *
 * <p>复刻 ERP 的 {@code MesWorkOrderOutboxService} 三态模式（HTTP/RABBIT/REDIS_STREAM）。
 * MES 工单完工后，同事务入 mes_integration_task 表，由 RetryJob 异步推送 SCM，
 * SCM 收到后落 scm_production_completion_fact 表，闭环生产链。
 *
 * <p>解耦 MES 与 SCM，避免 SCM 宕机影响 MES 工单完工流程。
 */
@Slf4j
@Service
public class MesCompletionOutboxService {

    /** Outbox 动作类型：SCM 完工事实推送 */
    public static final String ACTION_TYPE_SCM_COMPLETION_FACT = "SCM_COMPLETION_FACT";

    /** 事件类型 */
    private static final String EVENT_TYPE = "workorder.completed.v1";

    @Autowired
    private IntegrationTaskRepository integrationTaskRepository;

    @Autowired(required = false)
    private ScmIntegrationClient scmIntegrationClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntegrationTransportProperties integrationTransportProperties;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 工单完工后入队完工事件（同事务调用，保证原子性）。
     *
     * <p>幂等：按 erpProductionNo + workOrderNo 只入队一次。
     *
     * @param order 已完工的 MES 工单
     */
    @Transactional
    public void enqueueWorkOrderCompleted(WorkOrderEntity order) {
        if (order == null) {
            return;
        }
        if (order.getErpProductionNo() == null || order.getErpProductionNo().isBlank()) {
            log.warn("MES->SCM 完工事件入队：工单 {} 未关联 ERP 生产单号，跳过", order.getWorkOrderNo());
            return;
        }
        if (order.getWorkOrderNo() == null || order.getWorkOrderNo().isBlank()) {
            return;
        }

        String idempotencyKey = "MES:WORKORDER_COMPLETED:" + order.getErpProductionNo() + ":" + order.getWorkOrderNo();

        // 幂等：同一工单完工只入队一次
        Optional<IntegrationTaskEntity> existing = integrationTaskRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return;
        }

        MesCompletionFactDTO req = buildRequest(order, idempotencyKey);

        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setActionType(ACTION_TYPE_SCM_COMPLETION_FACT);
        task.setEventId(req.getEventId());
        task.setTraceId(req.getEventId());
        task.setProducer(req.getSourceSystem());
        task.setEventVersion(1);
        task.setPartitionKey(req.getErpProductionNo());
        task.setIdempotencyKey(idempotencyKey);
        task.setExternalRefNo(order.getErpProductionNo());
        task.setStatus("PENDING");
        task.setRetryCount(0);
        try {
            task.setRequestBody(objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            task.setRequestBody(null);
        }
        integrationTaskRepository.save(task);
        log.info("MES->SCM 完工事件已入队 erpProductionNo={} workOrderNo={} idempotencyKey={}",
                order.getErpProductionNo(), order.getWorkOrderNo(), idempotencyKey);
    }

    /**
     * 扫描 PENDING/FAILED 任务批量推送。
     *
     * @return 处理条数
     */
    @Transactional
    public int trySendPendingBatch() {
        List<IntegrationTaskEntity> tasks = integrationTaskRepository.findTop50ByStatusInAndActionTypeOrderByCreatedTimeAsc(
                List.of("PENDING", "FAILED"),
                ACTION_TYPE_SCM_COMPLETION_FACT
        );
        int processed = 0;
        for (IntegrationTaskEntity task : tasks) {
            if ("CONFIRMED".equalsIgnoreCase(task.getStatus())) {
                continue;
            }
            trySendOne(task);
            processed++;
        }
        return processed;
    }

    /**
     * 推送单条任务（三态分发）。
     *
     * @param task 集成任务
     */
    @Transactional
    public void trySendOne(IntegrationTaskEntity task) {
        if (task == null) {
            return;
        }
        if (task.getRequestBody() == null || task.getRequestBody().isBlank()) {
            markFailed(task, "requestBody为空");
            return;
        }
        MesCompletionFactDTO req;
        try {
            req = objectMapper.readValue(task.getRequestBody(), MesCompletionFactDTO.class);
        } catch (Exception e) {
            markFailed(task, "反序列化失败:" + e.getMessage());
            return;
        }
        try {
            IntegrationTransportProperties.Transport transport = integrationTransportProperties.getTransport();
            if (transport == IntegrationTransportProperties.Transport.RABBIT) {
                if (rabbitTemplate == null) {
                    markFailed(task, "RabbitMQ不可用");
                    return;
                }
                rabbitTemplate.convertAndSend(
                        integrationTransportProperties.getRabbit().getExchange(),
                        integrationTransportProperties.getRabbit().getRoutingKeyMesWorkOrderCompletion(),
                        task.getRequestBody()
                );
            } else if (transport == IntegrationTransportProperties.Transport.REDIS_STREAM) {
                if (stringRedisTemplate == null) {
                    markFailed(task, "Redis不可用");
                    return;
                }
                stringRedisTemplate.opsForStream().add(
                        integrationTransportProperties.getRedis().getStreamMesWorkOrderCompletionScm(),
                        Map.of("body", task.getRequestBody())
                );
            } else {
                // HTTP 直推 SCM
                if (scmIntegrationClient == null) {
                    markFailed(task, "SCM集成客户端不可用");
                    return;
                }
                Result<Map<String, Object>> res = scmIntegrationClient.receiveMesCompletion(req);
                if (res == null || !ResponseStatusAdapter.isSuccess(res.getCode())) {
                    markFailed(task, res == null ? "SCM返回为空" : ("SCM失败:" + res.getMessage()));
                    return;
                }
            }
            task.setStatus("CONFIRMED");
            task.setLastError(null);
            integrationTaskRepository.save(task);
        } catch (Exception ex) {
            markFailed(task, ex.getMessage());
        }
    }

    /**
     * 标记任务失败并记录错误。
     */
    private void markFailed(IntegrationTaskEntity task, String error) {
        int next = task.getRetryCount() == null ? 1 : task.getRetryCount() + 1;
        task.setRetryCount(next);
        task.setStatus("FAILED");
        task.setLastError(error);
        integrationTaskRepository.save(task);
        log.warn("MES->SCM 完工事件推送失败 eventId={} traceId={} idempotencyKey={} retry={} error={}",
                task.getEventId(), task.getTraceId(), task.getIdempotencyKey(), next, error);
    }

    /**
     * 构造完工事实请求 DTO。
     */
    private MesCompletionFactDTO buildRequest(WorkOrderEntity order, String idempotencyKey) {
        MesCompletionFactDTO req = new MesCompletionFactDTO();
        String eventId = UUID.randomUUID().toString();
        req.setEventId(eventId);
        req.setEventKey(idempotencyKey);
        req.setIdempotencyKey(idempotencyKey);
        req.setErpProductionNo(order.getErpProductionNo());
        req.setWorkOrderNo(order.getWorkOrderNo());
        req.setProductCode(order.getProductCode());
        req.setProductName(order.getProductName());
        req.setPlanQuantity(order.getPlanQuantity());
        req.setCompletedQuantity(order.getActualQuantity() != null ? order.getActualQuantity() : order.getPlanQuantity());
        req.setCompletedTime(LocalDateTime.now());
        req.setSourceSystem("mes-service");
        return req;
    }
}
