package com.hxcoe.bom.service;

import com.hxcoe.bom.client.dto.erp.MaterialEventRequest;
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
public class MaterialMirrorService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public void applyErpEvent(MaterialEventRequest req) {
        if (req == null) {
            return;
        }
        String eventKey = (req.getEventKey() == null || req.getEventKey().isBlank()) ? req.getIdempotencyKey() : req.getEventKey();
        if (eventKey == null || eventKey.isBlank()) {
            return;
        }
        if (req.getMaterial() == null || req.getMaterial().getMaterialCode() == null || req.getMaterial().getMaterialCode().isBlank()) {
            return;
        }
        if (!tryAcquireInbox(eventKey, req)) {
            return;
        }
        upsertMaterial(req);
    }

    private boolean tryAcquireInbox(String eventKey, MaterialEventRequest req) {
        Map<String, Object> params = new HashMap<>();
        params.put("eventKey", eventKey);
        params.put("eventId", req.getEventId());
        params.put("traceId", (req.getTraceId() == null || req.getTraceId().isBlank()) ? req.getEventId() : req.getTraceId());
        params.put("producer", req.getProducer());
        params.put("eventVersion", req.getEventVersion() == null ? 1 : req.getEventVersion());
        params.put("partitionKey", req.getPartitionKey());
        params.put("idempotencyKey", (req.getIdempotencyKey() == null || req.getIdempotencyKey().isBlank()) ? eventKey : req.getIdempotencyKey());
        params.put("eventType", req.getEventType() == null || req.getEventType().isBlank() ? "UNKNOWN" : req.getEventType());
        try {
            jdbcTemplate.update(
                    "INSERT INTO bom_integration_inbox (event_key, event_id, trace_id, producer, event_version, partition_key, idempotency_key, event_type, received_time) "
                            + "VALUES (:eventKey, :eventId, :traceId, :producer, :eventVersion, :partitionKey, :idempotencyKey, :eventType, NOW())",
                    params
            );
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    private void upsertMaterial(MaterialEventRequest req) {
        MaterialEventRequest.MaterialPayload m = req.getMaterial();
        String materialCode = m.getMaterialCode();

        Long existingId = findMaterialId(materialCode);

        Map<String, Object> params = new HashMap<>();
        params.put("materialCode", materialCode);
        params.put("materialName", m.getMaterialName());
        params.put("materialType", m.getMaterialType());
        params.put("materialSpec", m.getSpecification());
        params.put("unit", m.getUnit());
        params.put("unitPrice", m.getUnitPrice());
        params.put("status", mapStatus(m.getIsDeleted(), m.getStatus()));
        params.put("remark", req.getEventType());

        if (existingId == null) {
            jdbcTemplate.update(
                    "INSERT INTO bom_material (material_code, material_name, material_type, unit, material_spec, unit_price, status, created_by, created_time, updated_by, updated_time, remark) "
                            + "VALUES (:materialCode, :materialName, :materialType, :unit, :materialSpec, :unitPrice, :status, 'erp-event', NOW(), 'erp-event', NOW(), :remark)",
                    params
            );
        } else {
            jdbcTemplate.update(
                    // 单价做NULL保护：ERP事件未携带单价时保留BOM侧已有单价，避免清空成本卷积基础数据
                    "UPDATE bom_material SET material_name=:materialName, material_type=:materialType, unit=:unit, material_spec=:materialSpec, unit_price=COALESCE(:unitPrice, unit_price), status=:status, "
                            + "updated_by='erp-event', updated_time=NOW(), remark=:remark WHERE material_code=:materialCode",
                    params
            );
        }
    }

    private Long findMaterialId(String materialCode) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM bom_material WHERE material_code=:materialCode LIMIT 1",
                new MapSqlParameterSource("materialCode", materialCode),
                (rs, rowNum) -> rs.getLong("id")
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    private String mapStatus(Integer isDeleted, Integer status) {
        if (isDeleted != null && isDeleted == 1) {
            return "DELETED";
        }
        if (status == null) {
            return "ACTIVE";
        }
        return status == 1 ? "ACTIVE" : "INACTIVE";
    }
}
