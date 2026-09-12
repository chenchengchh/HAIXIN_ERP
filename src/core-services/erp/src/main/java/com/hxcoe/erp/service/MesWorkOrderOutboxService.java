package com.hxcoe.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.dto.mes.WorkOrderCreateRequestDTO;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.common.result.Result;
import com.hxcoe.erp.client.MesIntegrationClient;
import com.hxcoe.erp.entity.IntegrationTaskEntity;
import com.hxcoe.erp.entity.ProductionEntity;
import com.hxcoe.erp.repository.IntegrationTaskRepository;
import com.hxcoe.erp.repository.ProductionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * ERP→MES 工单创建 Outbox 服务。
 *
 * <p>复刻 SCM 的 ErpPoFactOutboxService 三态模式（HTTP/RABBIT/REDIS_STREAM）。
 * ERP 生产单创建后，同事务入 erp_integration_task 表，由 RetryJob 异步推送 MES。
 * 解耦 ERP 与 MES，避免 MES 宕机影响 ERP 创建。
 */
@Slf4j
@Service
public class MesWorkOrderOutboxService {

    /** Outbox 动作类型：MES 工单创建 */
    public static final String ACTION_TYPE_MES_WORK_ORDER_CREATE = "MES_WORK_ORDER_CREATE";

    /** 事件类型 */
    private static final String EVENT_TYPE = "production.created.v1";

    @Autowired
    private IntegrationTaskRepository integrationTaskRepository;

    @Autowired
    private ProductionRepository productionRepository;

    @Autowired(required = false)
    private MesIntegrationClient mesIntegrationClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntegrationTransportProperties integrationTransportProperties;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生产单创建后入队 MES 工单创建事件（同事务调用，保证原子性）。
     *
     * @param production ERP 生产单实体
     */
    @Transactional
    public void enqueueProductionCreated(ProductionEntity production) {
        if (production == null) {
            return;
        }
        if (production.getProductionNo() == null || production.getProductionNo().isBlank()) {
            return;
        }

        String productionNo = production.getProductionNo();
        String idempotencyKey = "ERP:PRODUCTION_CREATED:" + productionNo;

        // 幂等：同一生产单只入队一次
        Optional<IntegrationTaskEntity> existing = integrationTaskRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return;
        }

        WorkOrderCreateRequestDTO req = buildRequest(production, idempotencyKey);

        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setActionType(ACTION_TYPE_MES_WORK_ORDER_CREATE);
        task.setEventId(req.getEventId());
        task.setTraceId(req.getTraceId());
        task.setProducer(req.getProducer());
        task.setEventVersion(req.getEventVersion());
        task.setPartitionKey(req.getPartitionKey());
        task.setIdempotencyKey(idempotencyKey);
        task.setExternalRefNo(productionNo);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        try {
            task.setRequestBody(objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            task.setRequestBody(null);
        }
        integrationTaskRepository.save(task);
        // 更新生产单的 MES 集成状态为 PENDING（F1 前端配套）
        production.setMesIntegrationStatus("PENDING");
        productionRepository.save(production);
        log.info("ERP->MES 工单创建事件已入队 productionNo={} idempotencyKey={} mesIntegrationStatus=PENDING", productionNo, idempotencyKey);
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
                ACTION_TYPE_MES_WORK_ORDER_CREATE
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
        WorkOrderCreateRequestDTO req;
        try {
            req = objectMapper.readValue(task.getRequestBody(), WorkOrderCreateRequestDTO.class);
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
                        integrationTransportProperties.getRabbit().getRoutingKeyMesWorkOrderCreate(),
                        task.getRequestBody()
                );
            } else if (transport == IntegrationTransportProperties.Transport.REDIS_STREAM) {
                if (stringRedisTemplate == null) {
                    markFailed(task, "Redis不可用");
                    return;
                }
                stringRedisTemplate.opsForStream().add(
                        integrationTransportProperties.getRedis().getStreamMesWorkOrderCreate(),
                        Map.of("body", task.getRequestBody())
                );
            } else {
                // HTTP 直推 MES
                if (mesIntegrationClient == null) {
                    markFailed(task, "MES集成客户端不可用");
                    return;
                }
                Result<Map<String, Object>> res = mesIntegrationClient.receiveProductionCreated(req);
                if (res == null || !ResponseStatusAdapter.isSuccess(res.getCode())) {
                    markFailed(task, res == null ? "MES返回为空" : ("MES失败:" + res.getMessage()));
                    return;
                }
            }
            task.setStatus("CONFIRMED");
            task.setLastError(null);
            integrationTaskRepository.save(task);
            // 推送成功后更新生产单的 MES 集成状态为 CONFIRMED（F1 前端配套）
            updateProductionMesStatus(task.getExternalRefNo(), "CONFIRMED");
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
        log.warn("ERP->MES 工单创建事件推送失败 eventId={} traceId={} idempotencyKey={} retry={} error={}",
                task.getEventId(), task.getTraceId(), task.getIdempotencyKey(), next, error);
    }

    /**
     * 更新生产单的 MES 集成状态（F1 前端配套）。
     *
     * @param productionNo 生产单号（对应 IntegrationTask.externalRefNo）
     * @param status       目标状态（PENDING / CONFIRMED）
     */
    private void updateProductionMesStatus(String productionNo, String status) {
        if (productionNo == null || productionNo.isBlank()) {
            return;
        }
        try {
            ProductionEntity production = productionRepository.findByProductionNo(productionNo);
            if (production != null) {
                production.setMesIntegrationStatus(status);
                productionRepository.save(production);
                log.info("生产单 MES 集成状态已更新 productionNo={} mesIntegrationStatus={}", productionNo, status);
            }
        } catch (Exception e) {
            log.warn("更新生产单 MES 集成状态失败 productionNo={} status={} error={}", productionNo, status, e.getMessage());
        }
    }

    /**
     * 构造工单创建请求 DTO。
     */
    private WorkOrderCreateRequestDTO buildRequest(ProductionEntity production, String idempotencyKey) {
        WorkOrderCreateRequestDTO req = new WorkOrderCreateRequestDTO();
        String eventId = UUID.randomUUID().toString();
        req.setEventId(eventId);
        req.setTraceId(eventId);
        req.setEventType(EVENT_TYPE);
        req.setEventKey(idempotencyKey);
        req.setIdempotencyKey(idempotencyKey);
        req.setPartitionKey(production.getProductionNo());
        req.setEventVersion(1);
        req.setProducer("erp-service");
        req.setEventTime(LocalDateTime.now());

        WorkOrderCreateRequestDTO.WorkOrderPayload payload = new WorkOrderCreateRequestDTO.WorkOrderPayload();
        payload.setErpProductionNo(production.getProductionNo());
        payload.setWorkOrderNo("WO-" + production.getProductionNo());
        payload.setProductCode(production.getProductCode());
        payload.setProductName(production.getProductName());
        payload.setPlanQuantity(production.getProductionQuantity() == null
                ? BigDecimal.ZERO : BigDecimal.valueOf(production.getProductionQuantity()));
        payload.setWorkshop(production.getWorkshop());
        payload.setProductionLine(production.getProductionLine());
        payload.setPlanStartTime(production.getPlanStartTime());
        payload.setPlanEndTime(production.getPlanEndTime());
        payload.setSourceSystem("erp-service");
        req.setWorkOrder(payload);
        return req;
    }
}
