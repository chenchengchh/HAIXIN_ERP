package com.hxcoe.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.scm.client.dto.wms.OutboundShippedRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class ScmOutboundShipmentFactService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private ScmOutboundShipmentProjectionService scmOutboundShipmentProjectionService;

    @Transactional
    public boolean applyWmsOutboundShipped(OutboundShippedRequest req) {
        if (req == null || req.getOutboundOrder() == null) {
            return false;
        }

        String orderNo = req.getOutboundOrder().getOrderNo();
        if (orderNo == null || orderNo.isBlank()) {
            return false;
        }

        String eventKey = req.getEventKey() == null || req.getEventKey().isBlank() ? req.getIdempotencyKey() : req.getEventKey();
        if (eventKey == null || eventKey.isBlank()) {
            eventKey = "WMS_OUTBOUND_SHIPPED:" + orderNo;
        }

        if (!tryAcquireInbox(req, eventKey)) {
            return true;
        }

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(req.getOutboundOrder());
        } catch (Exception ignore) {
            payloadJson = "{}";
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("orderNo", orderNo);
        params.addValue("outboundOrderId", req.getOutboundOrder().getOutboundOrderId());
        params.addValue("orderType", req.getOutboundOrder().getOrderType());
        params.addValue("sourceNo", req.getOutboundOrder().getSourceNo());
        params.addValue("customerName", req.getOutboundOrder().getCustomerName());
        params.addValue("address", req.getOutboundOrder().getAddress());
        params.addValue("status", req.getOutboundOrder().getStatus());
        params.addValue("shippedTime", req.getOutboundOrder().getShippedTime());
        params.addValue("lastEventType", req.getEventType() == null || req.getEventType().isBlank() ? "UNKNOWN" : req.getEventType());
        params.addValue("payloadJson", payloadJson);
        jdbcTemplate.update(
                "INSERT INTO scm_outbound_shipment_fact "
                        + "(order_no, outbound_order_id, order_type, source_no, customer_name, address, status, shipped_time, last_event_type, payload_json, created_time, updated_time) "
                        + "VALUES (:orderNo, :outboundOrderId, :orderType, :sourceNo, :customerName, :address, :status, :shippedTime, :lastEventType, CAST(:payloadJson AS JSON), NOW(), NOW()) "
                        + "ON DUPLICATE KEY UPDATE outbound_order_id=VALUES(outbound_order_id), order_type=VALUES(order_type), source_no=VALUES(source_no), customer_name=VALUES(customer_name), address=VALUES(address), status=VALUES(status), shipped_time=VALUES(shipped_time), last_event_type=VALUES(last_event_type), payload_json=VALUES(payload_json), updated_time=NOW()",
                params
        );
        if (scmOutboundShipmentProjectionService != null) {
            scmOutboundShipmentProjectionService.projectWmsOutboundShipped(req);
        }
        return true;
    }

    public Map<String, Object> getByOrderNo(String orderNo) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("orderNo", orderNo);
        return jdbcTemplate.queryForList(
                "SELECT order_no, status, last_event_type, shipped_time, updated_time FROM scm_outbound_shipment_fact WHERE order_no=:orderNo",
                params
        ).stream().findFirst().orElse(null);
    }

    private boolean tryAcquireInbox(OutboundShippedRequest req, String eventKey) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("eventKey", eventKey);
        params.addValue("eventId", req.getEventId());
        params.addValue("traceId", req.getTraceId() == null || req.getTraceId().isBlank() ? req.getEventId() : req.getTraceId());
        params.addValue("producer", req.getProducer());
        params.addValue("eventVersion", req.getEventVersion() == null ? 1 : req.getEventVersion());
        params.addValue("partitionKey", req.getPartitionKey());
        params.addValue("idempotencyKey", req.getIdempotencyKey() == null || req.getIdempotencyKey().isBlank() ? eventKey : req.getIdempotencyKey());
        params.addValue("eventType", req.getEventType() == null || req.getEventType().isBlank() ? "UNKNOWN" : req.getEventType());
        try {
            jdbcTemplate.update(
                    "INSERT INTO scm_integration_inbox (event_key, event_id, trace_id, producer, event_version, partition_key, idempotency_key, event_type, received_time) "
                            + "VALUES (:eventKey, :eventId, :traceId, :producer, :eventVersion, :partitionKey, :idempotencyKey, :eventType, NOW())",
                    params
            );
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }
}
