package com.hxcoe.scm.service;

import com.hxcoe.scm.client.dto.wms.LocationEventRequest;
import com.hxcoe.scm.client.dto.wms.WarehouseEventRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseLocationMirrorService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public void applyWarehouseEvent(WarehouseEventRequest req) {
        if (req == null) {
            return;
        }
        String eventKey = (req.getEventKey() == null || req.getEventKey().isBlank()) ? req.getIdempotencyKey() : req.getEventKey();
        if (eventKey == null || eventKey.isBlank()) {
            return;
        }
        String eventType = req.getEventType() == null ? "" : req.getEventType();
        if (!tryAcquireInbox(eventKey, eventType, req.getEventId(), req.getTraceId(), req.getProducer(), req.getEventVersion(), req.getPartitionKey(), req.getIdempotencyKey())) {
            return;
        }
        WarehouseEventRequest.WarehousePayload w = req.getWarehouse();
        String code = w == null ? null : w.getWarehouseCode();
        if (code == null || code.isBlank()) {
            return;
        }
        if (eventType.toUpperCase().contains("DELETED")) {
            Map<String, Object> params = new HashMap<>();
            params.put("warehouseCode", code);
            jdbcTemplate.update("DELETE FROM scm_warehouse WHERE warehouse_code=:warehouseCode", params);
            return;
        }
        upsertWarehouse(w, eventType);
    }

    @Transactional
    public void applyLocationEvent(LocationEventRequest req) {
        if (req == null) {
            return;
        }
        String eventKey = (req.getEventKey() == null || req.getEventKey().isBlank()) ? req.getIdempotencyKey() : req.getEventKey();
        if (eventKey == null || eventKey.isBlank()) {
            return;
        }
        String eventType = req.getEventType() == null ? "" : req.getEventType();
        if (!tryAcquireInbox(eventKey, eventType, req.getEventId(), req.getTraceId(), req.getProducer(), req.getEventVersion(), req.getPartitionKey(), req.getIdempotencyKey())) {
            return;
        }
        LocationEventRequest.LocationPayload l = req.getLocation();
        String code = l == null ? null : l.getLocationCode();
        if (code == null || code.isBlank()) {
            return;
        }
        if (eventType.toUpperCase().contains("DELETED")) {
            Map<String, Object> params = new HashMap<>();
            params.put("locationCode", code);
            jdbcTemplate.update("DELETE FROM scm_location WHERE location_code=:locationCode", params);
            return;
        }
        upsertLocation(l, eventType);
    }

    private boolean tryAcquireInbox(String eventKey, String eventType, String eventId, String traceId, String producer, Integer eventVersion, String partitionKey, String idempotencyKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("eventKey", eventKey);
        params.put("eventId", eventId);
        params.put("traceId", traceId == null || traceId.isBlank() ? eventId : traceId);
        params.put("producer", producer);
        params.put("eventVersion", eventVersion == null ? 1 : eventVersion);
        params.put("partitionKey", partitionKey);
        params.put("idempotencyKey", idempotencyKey == null || idempotencyKey.isBlank() ? eventKey : idempotencyKey);
        params.put("eventType", eventType == null || eventType.isBlank() ? "UNKNOWN" : eventType);
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

    private void upsertWarehouse(WarehouseEventRequest.WarehousePayload w, String eventType) {
        Long existingId = findWarehouseId(w.getWarehouseCode());
        Map<String, Object> params = new HashMap<>();
        params.put("warehouseCode", w.getWarehouseCode());
        params.put("warehouseName", w.getWarehouseName());
        params.put("address", w.getAddress());
        params.put("manager", w.getManager());
        params.put("contact", w.getContact());
        params.put("status", w.getStatus());

        if (existingId == null) {
            jdbcTemplate.update(
                    "INSERT INTO scm_warehouse (warehouse_code, warehouse_name, address, manager, contact, status, created_time, updated_time) "
                            + "VALUES (:warehouseCode, :warehouseName, :address, :manager, :contact, :status, NOW(), NOW())",
                    params
            );
        } else {
            params.put("id", existingId);
            jdbcTemplate.update(
                    "UPDATE scm_warehouse SET warehouse_name=:warehouseName, address=:address, manager=:manager, contact=:contact, status=:status, updated_time=NOW() "
                            + "WHERE id=:id",
                    params
            );
        }
    }

    private void upsertLocation(LocationEventRequest.LocationPayload l, String eventType) {
        Long existingId = findLocationId(l.getLocationCode());
        Map<String, Object> params = new HashMap<>();
        params.put("locationCode", l.getLocationCode());
        params.put("locationName", l.getLocationName());
        params.put("warehouseCode", l.getWarehouseCode());
        params.put("zoneCode", l.getZoneCode());
        params.put("locationTypeCode", l.getLocationTypeCode());
        params.put("status", l.getStatus());
        params.put("remark", l.getRemark());

        if (existingId == null) {
            jdbcTemplate.update(
                    "INSERT INTO scm_location (location_code, location_name, warehouse_code, zone_code, location_type_code, status, remark, created_time, updated_time) "
                            + "VALUES (:locationCode, :locationName, :warehouseCode, :zoneCode, :locationTypeCode, :status, :remark, NOW(), NOW())",
                    params
            );
        } else {
            params.put("id", existingId);
            jdbcTemplate.update(
                    "UPDATE scm_location SET location_name=:locationName, warehouse_code=:warehouseCode, zone_code=:zoneCode, location_type_code=:locationTypeCode, status=:status, remark=:remark, updated_time=NOW() "
                            + "WHERE id=:id",
                    params
            );
        }
    }

    private Long findWarehouseId(String warehouseCode) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM scm_warehouse WHERE warehouse_code=:warehouseCode LIMIT 1",
                new MapSqlParameterSource("warehouseCode", warehouseCode),
                (rs, rowNum) -> rs.getLong("id")
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    private Long findLocationId(String locationCode) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM scm_location WHERE location_code=:locationCode LIMIT 1",
                new MapSqlParameterSource("locationCode", locationCode),
                (rs, rowNum) -> rs.getLong("id")
        );
        return ids.isEmpty() ? null : ids.get(0);
    }
}
