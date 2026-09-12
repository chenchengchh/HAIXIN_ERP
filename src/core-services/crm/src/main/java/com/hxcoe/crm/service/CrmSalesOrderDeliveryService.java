package com.hxcoe.crm.service;

import com.hxcoe.crm.dto.integration.WmsOutboundShippedRequest;
import com.hxcoe.crm.entity.SalesOrderEntity;
import com.hxcoe.crm.repository.SalesOrderRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrmSalesOrderDeliveryService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public boolean applyWmsOutboundShipped(WmsOutboundShippedRequest req) {
        if (req == null || req.getOutboundOrder() == null) {
            return false;
        }
        String orderType = trim(req.getOutboundOrder().getOrderType());
        String sourceNo = trim(req.getOutboundOrder().getSourceNo());
        String orderNo = trim(req.getOutboundOrder().getOrderNo());
        if (orderNo == null) {
            return false;
        }

        String eventKey = trim(req.getEventKey());
        if (eventKey == null) {
            eventKey = trim(req.getIdempotencyKey());
        }
        if (eventKey == null) {
            eventKey = "WMS_OUTBOUND_SHIPPED_CRM:" + orderNo;
        }

        if (!tryAcquireInbox(req, eventKey)) {
            return true;
        }

        if (!"SALES".equalsIgnoreCase(orderType) || sourceNo == null) {
            return true;
        }

        SalesOrderEntity order = salesOrderRepository.findByOrderNo(sourceNo);
        if (order == null) {
            return true;
        }

        order.setDeliveryStatus("SHIPPED");
        LocalDateTime shippedTime = req.getOutboundOrder().getShippedTime() == null
                ? req.getOutboundOrder().getUpdatedTime()
                : req.getOutboundOrder().getShippedTime();
        order.setDeliveryTime(shippedTime == null ? LocalDateTime.now() : shippedTime);
        order.setUpdateTime(LocalDateTime.now());

        String currentStatus = trim(order.getStatus());
        if (currentStatus == null || "approved".equalsIgnoreCase(currentStatus) || "submitted".equalsIgnoreCase(currentStatus)) {
            order.setStatus("in_progress");
        }
        salesOrderRepository.save(order);
        return true;
    }

    private boolean tryAcquireInbox(WmsOutboundShippedRequest req, String eventKey) {
        String traceId = trim(req.getTraceId());
        if (traceId == null) {
            traceId = trim(req.getEventId());
        }
        String idempotencyKey = trim(req.getIdempotencyKey());
        if (idempotencyKey == null) {
            idempotencyKey = eventKey;
        }
        String eventType = trim(req.getEventType());
        if (eventType == null) {
            eventType = "UNKNOWN";
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("eventKey", eventKey);
        params.addValue("eventId", trim(req.getEventId()));
        params.addValue("traceId", traceId);
        params.addValue("producer", trim(req.getProducer()));
        params.addValue("eventVersion", req.getEventVersion());
        params.addValue("partitionKey", trim(req.getPartitionKey()));
        params.addValue("idempotencyKey", idempotencyKey);
        params.addValue("eventType", eventType);

        try {
            jdbcTemplate.update(
                    "INSERT INTO crm_integration_inbox(event_key, event_id, trace_id, producer, event_version, partition_key, idempotency_key, event_type, received_time) "
                            + "VALUES (:eventKey, :eventId, :traceId, :producer, :eventVersion, :partitionKey, :idempotencyKey, :eventType, NOW())",
                    params
            );
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
