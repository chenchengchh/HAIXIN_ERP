package com.hxcoe.erp.service;

import com.hxcoe.erp.dto.integration.SupplierEventRequest;
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
public class SupplierMirrorService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional
    public void applySrmEvent(SupplierEventRequest req) {
        if (req == null) {
            return;
        }
        String eventKey = (req.getEventKey() == null || req.getEventKey().isBlank()) ? req.getIdempotencyKey() : req.getEventKey();
        if (eventKey == null || eventKey.isBlank()) {
            return;
        }
        if (!tryAcquireInbox(eventKey, req.getEventType(), req.getEventId(), req.getTraceId(), req.getProducer(), req.getEventVersion(), req.getPartitionKey(), req.getIdempotencyKey())) {
            return;
        }
        SupplierEventRequest.SupplierPayload s = req.getSupplier();
        if (s == null || s.getSupplierCode() == null || s.getSupplierCode().isBlank()) {
            return;
        }
        String eventType = req.getEventType() == null ? "" : req.getEventType();
        if (eventType.toUpperCase().contains("DELETED")) {
            softDeleteByCode(s.getSupplierCode(), eventType);
            return;
        }
        upsertSupplier(s, eventType);
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
                    "INSERT INTO erp_integration_inbox (event_key, event_id, trace_id, producer, event_version, partition_key, idempotency_key, event_type, received_time) "
                            + "VALUES (:eventKey, :eventId, :traceId, :producer, :eventVersion, :partitionKey, :idempotencyKey, :eventType, NOW())",
                    params
            );
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    private void upsertSupplier(SupplierEventRequest.SupplierPayload s, String eventType) {
        Long existingId = findSupplierId(s.getSupplierCode());
        Map<String, Object> params = new HashMap<>();
        params.put("supplierCode", s.getSupplierCode());
        params.put("supplierName", s.getSupplierName());
        params.put("supplierType", s.getType());
        Double rating = null;
        if (s.getCategory() != null && !s.getCategory().isBlank()) {
            rating = switch (s.getCategory().trim().toUpperCase()) {
                case "A" -> 5.0;
                case "B" -> 4.0;
                case "C" -> 3.0;
                case "D" -> 2.0;
                default -> null;
            };
        }
        params.put("rating", rating);
        params.put("contactPerson", s.getContactPerson());
        params.put("contactPhone", s.getContactPhone());
        params.put("email", s.getEmail());
        params.put("address", s.getAddress());
        params.put("status", "ACTIVE".equalsIgnoreCase(s.getStatus()) ? 1 : 0);
        params.put("remark", eventType);

        if (existingId == null) {
            params.put("isDeleted", 0);
            jdbcTemplate.update(
                    "INSERT INTO erp_supplier (supplier_code, supplier_name, supplier_type, rating, contact_person, contact_phone, email, address, status, remark, created_time, updated_time, is_deleted) "
                            + "VALUES (:supplierCode, :supplierName, :supplierType, :rating, :contactPerson, :contactPhone, :email, :address, :status, :remark, NOW(), NOW(), :isDeleted)",
                    params
            );
        } else {
            params.put("id", existingId);
            jdbcTemplate.update(
                    "UPDATE erp_supplier SET supplier_name=:supplierName, supplier_type=:supplierType, rating=:rating, contact_person=:contactPerson, contact_phone=:contactPhone, "
                            + "email=:email, address=:address, status=:status, remark=:remark, is_deleted=0, updated_time=NOW() WHERE id=:id",
                    params
            );
        }
    }

    private void softDeleteByCode(String supplierCode, String eventType) {
        Map<String, Object> params = new HashMap<>();
        params.put("supplierCode", supplierCode);
        params.put("remark", eventType);
        jdbcTemplate.update(
                "UPDATE erp_supplier SET is_deleted=1, status=0, remark=:remark, updated_time=NOW() WHERE supplier_code=:supplierCode",
                params
        );
    }

    private Long findSupplierId(String supplierCode) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM erp_supplier WHERE supplier_code=:supplierCode LIMIT 1",
                new MapSqlParameterSource("supplierCode", supplierCode),
                (rs, rowNum) -> rs.getLong("id")
        );
        return ids.isEmpty() ? null : ids.get(0);
    }
}
