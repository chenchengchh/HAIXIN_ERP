package com.hxcoe.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.erp.dto.integration.PurchaseOrderEventRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ErpPoSnapshotService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public boolean applyScmPoEvent(PurchaseOrderEventRequest req) {
        if (req == null) {
            return false;
        }
        String eventKey = req.getEventKey() == null || req.getEventKey().isBlank() ? req.getIdempotencyKey() : req.getEventKey();
        if (eventKey == null || eventKey.isBlank()) {
            return false;
        }

        // 重试同一事件时，若 Inbox 已存在，说明此前已成功接收，按幂等成功处理。
        if (!tryAcquireInbox(req, eventKey)) {
            return true;
        }

        if (req.getPurchaseOrder() == null || req.getPurchaseOrder().getOrderNo() == null || req.getPurchaseOrder().getOrderNo().isBlank()) {
            return true;
        }

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(req.getPurchaseOrder());
        } catch (Exception ignore) {
            payloadJson = "{}";
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("poNo", req.getPurchaseOrder().getOrderNo());
        params.addValue("lastEventType", req.getEventType() == null || req.getEventType().isBlank() ? "UNKNOWN" : req.getEventType());
        params.addValue("payloadJson", payloadJson);
        jdbcTemplate.update(
                "INSERT INTO erp_po_snapshot (po_no, last_event_type, payload_json, created_time, updated_time) "
                        + "VALUES (:poNo, :lastEventType, CAST(:payloadJson AS JSON), NOW(), NOW()) "
                        + "ON DUPLICATE KEY UPDATE last_event_type=VALUES(last_event_type), payload_json=VALUES(payload_json), updated_time=NOW()",
                params
        );
        return true;
    }

    public Map<String, Object> getByPoNo(String poNo) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("poNo", poNo);
        return jdbcTemplate.queryForList(
                "SELECT po_no, last_event_type, updated_time FROM erp_po_snapshot WHERE po_no=:poNo",
                params
        ).stream().findFirst().orElse(null);
    }

    private boolean tryAcquireInbox(PurchaseOrderEventRequest req, String eventKey) {
        String traceId = req.getTraceId() == null || req.getTraceId().isBlank() ? req.getEventId() : req.getTraceId();
        String idempotencyKey = req.getIdempotencyKey() == null || req.getIdempotencyKey().isBlank() ? eventKey : req.getIdempotencyKey();
        String eventType = req.getEventType() == null || req.getEventType().isBlank() ? "UNKNOWN" : req.getEventType();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("eventKey", eventKey);
        params.addValue("eventId", req.getEventId());
        params.addValue("traceId", traceId);
        params.addValue("producer", req.getProducer());
        params.addValue("eventVersion", req.getEventVersion());
        params.addValue("partitionKey", req.getPartitionKey());
        params.addValue("idempotencyKey", idempotencyKey);
        params.addValue("eventType", eventType);

        try {
            jdbcTemplate.update(
                    "INSERT INTO erp_integration_inbox(event_key, event_id, trace_id, producer, event_version, partition_key, idempotency_key, event_type, received_time) "
                            + "VALUES (:eventKey, :eventId, :traceId, :producer, :eventVersion, :partitionKey, :idempotencyKey, :eventType, NOW())",
                    params
            );
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }
}

