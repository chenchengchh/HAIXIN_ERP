package com.hxcoe.wms.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.wms.client.CrmIntegrationClient;
import com.hxcoe.wms.client.dto.CrmOutboundShippedRequest;
import com.hxcoe.wms.entity.IntegrationOutboxEntity;
import com.hxcoe.wms.entity.OutboundOrderEntity;
import com.hxcoe.wms.entity.OutboundOrderItemEntity;
import com.hxcoe.wms.repository.IntegrationOutboxRepository;
import com.hxcoe.wms.repository.OutboundOrderRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CrmOutboundShipmentSyncService {

    public static final String EVENT_OUTBOUND_SHIPPED_CRM = "WMS_OUTBOUND_SHIPPED_CRM";

    @Autowired
    private IntegrationOutboxRepository outboxRepository;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired(required = false)
    private CrmIntegrationClient crmIntegrationClient;

    @Transactional
    public void enqueueOutboundShippedIfAbsent(OutboundOrderEntity order) {
        if (order == null || order.getId() == null) {
            return;
        }
        if (!isSalesOrder(order) || isBlank(order.getSourceNo()) || isBlank(order.getOrderNo())) {
            return;
        }
        outboxRepository.findByEventTypeAndRefNo(EVENT_OUTBOUND_SHIPPED_CRM, order.getOrderNo()).orElseGet(() -> {
            IntegrationOutboxEntity e = new IntegrationOutboxEntity();
            e.setEventType(EVENT_OUTBOUND_SHIPPED_CRM);
            e.setRefNo(order.getOrderNo());
            e.setEntityId(order.getId());
            e.setEventId(UUID.randomUUID().toString());
            e.setTraceId(e.getEventId());
            e.setProducer("wms-service");
            e.setEventVersion(1);
            e.setPartitionKey(order.getSourceNo());
            e.setIdempotencyKey(EVENT_OUTBOUND_SHIPPED_CRM + ":" + order.getOrderNo());
            e.setStatus("PENDING");
            e.setRetryCount(0);
            e.setNextRetryAt(LocalDateTime.now());
            return outboxRepository.save(e);
        });
    }

    @Transactional
    public void trySendOne(IntegrationOutboxEntity outbox) {
        if (outbox == null) {
            return;
        }
        if (crmIntegrationClient == null) {
            markFailed(outbox, "CRM集成客户端不可用");
            return;
        }

        Optional<OutboundOrderEntity> optional = outboundOrderRepository.findById(outbox.getEntityId());
        if (optional.isEmpty()) {
            markFailed(outbox, "出库单不存在");
            return;
        }

        OutboundOrderEntity order = optional.get();
        if (!isSalesOrder(order)) {
            markSent(outbox);
            return;
        }
        if (isBlank(order.getSourceNo())) {
            markSent(outbox);
            return;
        }
        if (isBlank(order.getOrderNo())) {
            markFailed(outbox, "orderNo缺失");
            return;
        }

        CrmOutboundShippedRequest req = buildRequest(outbox, order);
        try {
            Result<Map<String, Object>> res = crmIntegrationClient.outboundShipped(req);
            if (!isRemoteSuccess(res)) {
                markFailed(outbox, res == null ? "CRM返回为空" : ("CRM失败:" + res.getMessage()));
                return;
            }
            markSent(outbox);
        } catch (Exception ex) {
            markFailed(outbox, ex.getMessage());
        }
    }

    @Transactional
    public int trySendPendingBatch() {
        List<IntegrationOutboxEntity> list = outboxRepository.findTop50ByStatusInAndNextRetryAtBeforeOrderByNextRetryAtAsc(
                List.of("PENDING", "FAILED"),
                LocalDateTime.now()
        );
        int processed = 0;
        for (IntegrationOutboxEntity e : list) {
            if (!EVENT_OUTBOUND_SHIPPED_CRM.equals(e.getEventType())) {
                continue;
            }
            if ("SENT".equalsIgnoreCase(e.getStatus())) {
                continue;
            }
            trySendOne(e);
            processed++;
        }
        return processed;
    }

    private CrmOutboundShippedRequest buildRequest(IntegrationOutboxEntity outbox, OutboundOrderEntity order) {
        CrmOutboundShippedRequest req = new CrmOutboundShippedRequest();
        req.setEventId(outbox.getEventId());
        req.setTraceId(outbox.getTraceId());
        req.setEventType("WMS_OUTBOUND_SHIPPED");
        req.setEventKey(outbox.getIdempotencyKey());
        req.setEventVersion(outbox.getEventVersion());
        req.setProducer(outbox.getProducer());
        req.setEventTime(order.getUpdatedTime());
        req.setIdempotencyKey(outbox.getIdempotencyKey());
        req.setPartitionKey(outbox.getPartitionKey());

        CrmOutboundShippedRequest.OutboundOrderPayload payload = new CrmOutboundShippedRequest.OutboundOrderPayload();
        payload.setOutboundOrderId(order.getId());
        payload.setOrderNo(order.getOrderNo());
        payload.setOrderType(order.getType());
        payload.setSourceNo(order.getSourceNo());
        payload.setCustomerName(order.getCustomerName());
        payload.setAddress(order.getAddress());
        payload.setStatus(order.getStatus());
        payload.setShippedTime(order.getUpdatedTime());
        payload.setUpdatedTime(order.getUpdatedTime());
        payload.setItems(buildItems(order));
        req.setOutboundOrder(payload);
        return req;
    }

    private List<CrmOutboundShippedRequest.OutboundOrderItemPayload> buildItems(OutboundOrderEntity order) {
        List<CrmOutboundShippedRequest.OutboundOrderItemPayload> items = new ArrayList<>();
        if (order.getItems() == null) {
            return items;
        }
        for (OutboundOrderItemEntity item : order.getItems()) {
            if (item == null) {
                continue;
            }
            CrmOutboundShippedRequest.OutboundOrderItemPayload payload = new CrmOutboundShippedRequest.OutboundOrderItemPayload();
            payload.setMaterialCode(item.getMaterialCode());
            payload.setMaterialName(item.getMaterialName());
            payload.setQuantity(item.getQuantity());
            payload.setUnit(item.getUnit());
            payload.setLocationCode(item.getLocationCode());
            payload.setBatchNo(item.getBatchNo());
            items.add(payload);
        }
        return items;
    }

    private void markSent(IntegrationOutboxEntity outbox) {
        outbox.setStatus("SENT");
        outbox.setLastError(null);
        outbox.setNextRetryAt(null);
        outboxRepository.save(outbox);
    }

    private void markFailed(IntegrationOutboxEntity outbox, String error) {
        int next = outbox.getRetryCount() == null ? 1 : outbox.getRetryCount() + 1;
        outbox.setRetryCount(next);
        outbox.setStatus("FAILED");
        outbox.setLastError(error);
        outbox.setNextRetryAt(LocalDateTime.now().plusSeconds(Math.min(600L, 5L * next)));
        outboxRepository.save(outbox);
        log.warn("WMS->CRM发货回写失败 eventId={} traceId={} eventType={} refNo={} retry={} error={}",
                outbox.getEventId(), outbox.getTraceId(), outbox.getEventType(), outbox.getRefNo(), next, error);
    }

    private boolean isRemoteSuccess(Result<?> result) {
        Integer code = result == null ? null : result.getCode();
        return code != null && (code == 0 || code == 200);
    }

    private boolean isSalesOrder(OutboundOrderEntity order) {
        return order != null && !isBlank(order.getType()) && "SALES".equalsIgnoreCase(order.getType().trim());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
