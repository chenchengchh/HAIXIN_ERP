package com.hxcoe.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.ErpPurchaseOrderIntegrationClient;
import com.hxcoe.scm.client.SrmIntegrationClient;
import com.hxcoe.scm.client.WmsIntegrationClient;
import com.hxcoe.scm.client.dto.srm.PurchaseOrderEventRequest;
import com.hxcoe.scm.entity.IntegrationTaskEntity;
import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.repository.IntegrationTaskRepository;
import com.hxcoe.scm.repository.PurchaseOrderItemRepository;
import com.hxcoe.scm.repository.PurchaseOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PurchaseOrderEventOutboxService {

    public static final String ACTION_TYPE_SRM_PO_EVENT = "SRM_PO_EVENT";

    @Autowired
    private IntegrationTaskRepository integrationTaskRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @Autowired(required = false)
    private SrmIntegrationClient srmIntegrationClient;

    @Autowired(required = false)
    private WmsIntegrationClient wmsIntegrationClient;

    @Autowired(required = false)
    private ErpPurchaseOrderIntegrationClient erpPurchaseOrderIntegrationClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntegrationTransportProperties integrationTransportProperties;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Value("${scm.integration.po-event.srm-enabled:true}")
    private boolean srmEnabled;

    @Value("${scm.integration.po-event.wms-enabled:false}")
    private boolean wmsEnabled;

    @Value("${scm.integration.po-event.erp-enabled:false}")
    private boolean erpEnabled;

    @Transactional
    public void enqueuePoEvent(String orderNo, String eventType) {
        if (orderNo == null || orderNo.isBlank()) return;
        if (eventType == null || eventType.isBlank()) return;

        PurchaseOrderEntity order = purchaseOrderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) return;
        List<PurchaseOrderItemEntity> items = purchaseOrderItemRepository.findByOrderNo(orderNo);

        LocalDateTime ut = order.getUpdatedTime() == null ? LocalDateTime.now() : order.getUpdatedTime();
        String eventKey = eventType + ":" + orderNo + ":" + ut.toString();
        Optional<IntegrationTaskEntity> existing = integrationTaskRepository.findByIdempotencyKey(eventKey);
        if (existing.isPresent()) return;

        PurchaseOrderEventRequest req = buildEvent(order, items, eventType, eventKey);
        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setActionType(ACTION_TYPE_SRM_PO_EVENT);
        task.setEventId(req.getEventId());
        task.setTraceId(req.getTraceId());
        task.setProducer(req.getProducer());
        task.setEventVersion(req.getEventVersion());
        task.setPartitionKey(req.getPartitionKey());
        task.setIdempotencyKey(eventKey);
        task.setExternalRefNo(orderNo);
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
                ACTION_TYPE_SRM_PO_EVENT
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
        PurchaseOrderEventRequest req;
        try {
            req = objectMapper.readValue(task.getRequestBody(), PurchaseOrderEventRequest.class);
        } catch (Exception e) {
            markFailed(task, "反序列化失败:" + e.getMessage());
            return;
        }
        try {
            if (integrationTransportProperties.getTransport() == IntegrationTransportProperties.Transport.RABBIT) {
                if (rabbitTemplate == null) {
                    markFailed(task, "Rabbit不可用");
                    return;
                }
                rabbitTemplate.convertAndSend(
                        integrationTransportProperties.getRabbit().getExchange(),
                        integrationTransportProperties.getRabbit().getRoutingKeyScmPoEvents(),
                        task.getRequestBody()
                );
                task.setStatus("CONFIRMED");
                task.setLastError(null);
                integrationTaskRepository.save(task);
                return;
            }

            if (integrationTransportProperties.getTransport() == IntegrationTransportProperties.Transport.REDIS_STREAM) {
                if (stringRedisTemplate == null) {
                    markFailed(task, "Redis不可用");
                    return;
                }
                String stream = integrationTransportProperties.getRedis().getStreamScmPoEvents();
                stringRedisTemplate.opsForStream().add(stream, Map.of(
                        "body", task.getRequestBody(),
                        "eventId", task.getEventId() == null ? "" : task.getEventId(),
                        "traceId", task.getTraceId() == null ? "" : task.getTraceId(),
                        "idempotencyKey", task.getIdempotencyKey() == null ? "" : task.getIdempotencyKey()
                ));
                task.setStatus("CONFIRMED");
                task.setLastError(null);
                integrationTaskRepository.save(task);
                return;
            }
            if (srmEnabled) {
                if (srmIntegrationClient == null) {
                    markFailed(task, "SRM集成客户端不可用");
                    return;
                }
                Result<Map<String, Object>> res = srmIntegrationClient.receivePurchaseOrderEvent(req);
                if (!isRemoteSuccess(res)) {
                    markFailed(task, res == null ? "SRM返回为空" : ("SRM失败:" + res.getMessage()));
                    return;
                }
            }
            if (wmsEnabled) {
                if (wmsIntegrationClient == null) {
                    markFailed(task, "WMS集成客户端不可用");
                    return;
                }
                Result<Map<String, Object>> res = wmsIntegrationClient.receiveScmPoEvent(req);
                if (!isRemoteSuccess(res)) {
                    markFailed(task, res == null ? "WMS返回为空" : ("WMS失败:" + res.getMessage()));
                    return;
                }
            }
            if (erpEnabled) {
                if (erpPurchaseOrderIntegrationClient == null) {
                    markFailed(task, "ERP集成客户端不可用");
                    return;
                }
                Result<Map<String, Object>> res = erpPurchaseOrderIntegrationClient.receiveScmPoEvent(req);
                if (!isRemoteSuccess(res)) {
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
        log.warn("SCM PO事件推送失败 eventId={} traceId={} idempotencyKey={} retry={} error={}",
                task.getEventId(), task.getTraceId(), task.getIdempotencyKey(), next, error);
    }

    private boolean isRemoteSuccess(Result<?> res) {
        return res != null && ResponseStatusAdapter.isSuccess(res.getCode());
    }

    private PurchaseOrderEventRequest buildEvent(PurchaseOrderEntity order, List<PurchaseOrderItemEntity> items, String eventType, String eventKey) {
        PurchaseOrderEventRequest req = new PurchaseOrderEventRequest();
        req.setEventId(UUID.randomUUID().toString());
        req.setTraceId(req.getEventId());
        req.setEventType(eventType);
        req.setEventKey(eventKey);
        req.setIdempotencyKey(eventKey);
        req.setPartitionKey(order.getOrderNo());
        req.setEventVersion(1);
        req.setProducer("scm-service");
        req.setEventTime(LocalDateTime.now());

        PurchaseOrderEventRequest.PurchaseOrderPayload payload = new PurchaseOrderEventRequest.PurchaseOrderPayload();
        payload.setOrderNo(order.getOrderNo());
        payload.setSupplierId(order.getSupplierId());
        payload.setSupplierCode(order.getSupplierCode());
        payload.setSupplierName(order.getSupplierName());
        payload.setOrderStatus(order.getOrderStatus());
        payload.setOrderAmount(order.getOrderAmount());
        payload.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        payload.setActualDeliveryDate(order.getActualDeliveryDate());
        payload.setRemark(order.getRemark());
        payload.setUpdatedTime(order.getUpdatedTime());

        List<PurchaseOrderEventRequest.PurchaseOrderItemPayload> itemPayloads = new ArrayList<>();
        if (items != null) {
            for (PurchaseOrderItemEntity it : items) {
                if (it == null) continue;
                PurchaseOrderEventRequest.PurchaseOrderItemPayload ip = new PurchaseOrderEventRequest.PurchaseOrderItemPayload();
                ip.setMaterialCode(it.getMaterialCode());
                ip.setMaterialName(it.getMaterialName());
                ip.setMaterialSpec(it.getMaterialSpec());
                ip.setUnit(it.getUnit());
                ip.setQuantity(it.getQuantity());
                ip.setUnitPrice(it.getUnitPrice());
                ip.setAmount(it.getAmount());
                ip.setReceivedQuantity(it.getReceivedQuantity());
                itemPayloads.add(ip);
            }
        }
        payload.setItems(itemPayloads);
        req.setPurchaseOrder(payload);
        return req;
    }
}
