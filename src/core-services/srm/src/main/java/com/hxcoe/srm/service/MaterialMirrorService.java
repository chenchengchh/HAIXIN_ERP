package com.hxcoe.srm.service;

import com.hxcoe.srm.dto.integration.MaterialEventRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialMirrorService {

    @Autowired
    @Qualifier("srmSyncJdbcTemplate")
    private NamedParameterJdbcTemplate srmSyncJdbcTemplate;

    @Transactional(transactionManager = "srmSyncTxManager")
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
            srmSyncJdbcTemplate.update(
                    "INSERT INTO srm_integration_inbox (event_key, event_id, trace_id, producer, event_version, partition_key, idempotency_key, event_type, received_time) "
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
        params.put("specification", m.getSpecification());
        params.put("unit", m.getUnit());
        params.put("price", m.getUnitPrice());
        params.put("status", mapStatus(m.getIsDeleted(), m.getStatus()));

        if (existingId == null) {
            srmSyncJdbcTemplate.update(
                    "INSERT INTO srm_material (material_code, material_name, name, specification, unit, price, status, created_time, updated_time) "
                            + "VALUES (:materialCode, :materialName, :materialName, :specification, :unit, :price, :status, NOW(), NOW())",
                    params
            );
        } else {
            srmSyncJdbcTemplate.update(
                    "UPDATE srm_material SET material_name=:materialName, name=:materialName, specification=:specification, unit=:unit, price=:price, status=:status, updated_time=NOW() "
                            + "WHERE material_code=:materialCode",
                    params
            );
        }
    }

    private Long findMaterialId(String materialCode) {
        List<Long> ids = srmSyncJdbcTemplate.query(
                "SELECT id FROM srm_material WHERE material_code=:materialCode LIMIT 1",
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
