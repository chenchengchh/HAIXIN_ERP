package com.hxcoe.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.scm.client.ErpIntegrationClient;
import com.hxcoe.scm.client.dto.erp.PoFactRequest;
import com.hxcoe.scm.entity.IntegrationTaskEntity;
import com.hxcoe.scm.repository.IntegrationTaskRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ErpPoFactOutboxService {

    public static final String ACTION_TYPE_ERP_PO_FACT = "ERP_PO_FACT";

    @Autowired
    private IntegrationTaskRepository integrationTaskRepository;

    @Autowired(required = false)
    private ErpIntegrationClient erpIntegrationClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntegrationTransportProperties integrationTransportProperties;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Transactional
    public void enqueueReceiptCompleted(String poNo, String receiptNo, List<PoFactRequest.ReceiptLine> lines, Integer scmOrderStatus) {
        if (poNo == null || poNo.isBlank()) return;
        if (receiptNo == null || receiptNo.isBlank()) return;
        String idempotencyKey = "ERP:RECEIPT_COMPLETED:" + receiptNo;
        Optional<IntegrationTaskEntity> existing = integrationTaskRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) return;

        BigDecimal sum = BigDecimal.ZERO;
        if (lines != null) {
            for (PoFactRequest.ReceiptLine l : lines) {
                if (l == null || l.getQty() == null) continue;
                sum = sum.add(l.getQty());
            }
        }

        PoFactRequest req = buildBase("po.receipt.completed.v1", idempotencyKey, poNo);
        PoFactRequest.PoFactPayload payload = new PoFactRequest.PoFactPayload();
        payload.setFactType("RECEIPT_COMPLETED");
        payload.setPoNo(poNo);
        payload.setRefNo(receiptNo);
        payload.setScmOrderStatus(scmOrderStatus);
        payload.setReceivedQtySum(sum);
        payload.setLines(lines);
        req.setFact(payload);

        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setActionType(ACTION_TYPE_ERP_PO_FACT);
        task.setEventId(req.getEventId());
        task.setTraceId(req.getTraceId());
        task.setProducer(req.getProducer());
        task.setEventVersion(req.getEventVersion());
        task.setPartitionKey(req.getPartitionKey());
        task.setIdempotencyKey(idempotencyKey);
        task.setExternalRefNo(poNo);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        try {
            task.setRequestBody(objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            task.setRequestBody(null);
        }
        integrationTaskRepository.save(task);
    }

    @Transactional
    public void enqueueIqcCompleted(String poNo, String qcNo, String result, Integer scmOrderStatus) {
        if (poNo == null || poNo.isBlank()) return;
        if (qcNo == null || qcNo.isBlank()) return;
        String idempotencyKey = "ERP:IQC_COMPLETED:" + qcNo;
        Optional<IntegrationTaskEntity> existing = integrationTaskRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) return;

        PoFactRequest req = buildBase("po.iqc.completed.v1", idempotencyKey, poNo);
        PoFactRequest.PoFactPayload payload = new PoFactRequest.PoFactPayload();
        payload.setFactType("IQC_COMPLETED");
        payload.setPoNo(poNo);
        payload.setRefNo(qcNo);
        payload.setResult(result);
        payload.setScmOrderStatus(scmOrderStatus);
        req.setFact(payload);

        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setActionType(ACTION_TYPE_ERP_PO_FACT);
        task.setEventId(req.getEventId());
        task.setTraceId(req.getTraceId());
        task.setProducer(req.getProducer());
        task.setEventVersion(req.getEventVersion());
        task.setPartitionKey(req.getPartitionKey());
        task.setIdempotencyKey(idempotencyKey);
        task.setExternalRefNo(poNo);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        try {
            task.setRequestBody(objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            task.setRequestBody(null);
        }
        integrationTaskRepository.save(task);
    }

    @Transactional
    public int trySendPendingBatch() {
        List<IntegrationTaskEntity> tasks = integrationTaskRepository.findTop50ByStatusInAndActionTypeOrderByCreatedTimeAsc(
                List.of("PENDING", "FAILED"),
                ACTION_TYPE_ERP_PO_FACT
        );
        int processed = 0;
        for (IntegrationTaskEntity t : tasks) {
            if ("CONFIRMED".equalsIgnoreCase(t.getStatus())) continue;
            trySendOne(t);
            processed++;
        }
        return processed;
    }

    @Transactional
    public void trySendOne(IntegrationTaskEntity task) {
        if (task == null) return;
        if (task.getRequestBody() == null || task.getRequestBody().isBlank()) {
            markFailed(task, "requestBody为空");
            return;
        }
        PoFactRequest req;
        try {
            req = objectMapper.readValue(task.getRequestBody(), PoFactRequest.class);
        } catch (Exception e) {
            markFailed(task, "反序列化失败:" + e.getMessage());
            return;
        }
        try {
            if (integrationTransportProperties.getTransport() == IntegrationTransportProperties.Transport.RABBIT) {
                if (rabbitTemplate == null) {
                    markFailed(task, "RabbitMQ不可用");
                    return;
                }
                rabbitTemplate.convertAndSend(
                        integrationTransportProperties.getRabbit().getExchange(),
                        integrationTransportProperties.getRabbit().getRoutingKeyErpPoFacts(),
                        task.getRequestBody()
                );
            } else if (integrationTransportProperties.getTransport() == IntegrationTransportProperties.Transport.REDIS_STREAM) {
                if (stringRedisTemplate == null) {
                    markFailed(task, "Redis不可用");
                    return;
                }
                stringRedisTemplate.opsForStream().add(
                        integrationTransportProperties.getRedis().getStreamErpPoFacts(),
                        Map.of("body", task.getRequestBody())
                );
            } else {
                if (erpIntegrationClient == null) {
                    markFailed(task, "ERP集成客户端不可用");
                    return;
                }
                Result<Map<String, Object>> res = erpIntegrationClient.receivePoFacts(req);
                if (res == null || !ResponseStatusAdapter.isSuccess(res.getCode())) {
                    markFailed(task, res == null ? "ERP返回为空" : ("ERP失败:" + res.getMessage()));
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

    private void markFailed(IntegrationTaskEntity task, String error) {
        int next = task.getRetryCount() == null ? 1 : task.getRetryCount() + 1;
        task.setRetryCount(next);
        task.setStatus("FAILED");
        task.setLastError(error);
        integrationTaskRepository.save(task);
        log.warn("SCM->ERP 采购事实回传失败 eventId={} traceId={} idempotencyKey={} retry={} error={}",
                task.getEventId(), task.getTraceId(), task.getIdempotencyKey(), next, error);
    }

    private PoFactRequest buildBase(String eventType, String idempotencyKey, String poNo) {
        PoFactRequest req = new PoFactRequest();
        req.setEventId(UUID.randomUUID().toString());
        req.setTraceId(req.getEventId());
        req.setEventType(eventType);
        req.setEventKey(idempotencyKey);
        req.setIdempotencyKey(idempotencyKey);
        req.setPartitionKey(poNo);
        req.setEventVersion(1);
        req.setProducer("scm-service");
        req.setEventTime(LocalDateTime.now());
        return req;
    }
}
