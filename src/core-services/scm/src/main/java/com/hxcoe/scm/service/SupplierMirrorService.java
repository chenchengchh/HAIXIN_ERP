package com.hxcoe.scm.service;

import com.hxcoe.scm.client.dto.srm.SupplierEventRequest;
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

    /**
     * 消费 SRM 推送的 Supplier 事件并落 SCM 镜像（通过 Inbox 幂等去重）。
     */
    @Transactional
    public void applySrmEvent(SupplierEventRequest req) {
        if (req == null) {
            return;
        }
        String eventKey = (req.getEventKey() == null || req.getEventKey().isBlank()) ? req.getIdempotencyKey() : req.getEventKey();
        if (eventKey == null || eventKey.isBlank()) {
            return;
        }
        if (req.getSupplier() == null || req.getSupplier().getSupplierCode() == null || req.getSupplier().getSupplierCode().isBlank()) {
            return;
        }
        if (!tryAcquireInbox(eventKey, req.getEventType(), req.getEventId(), req.getTraceId(), req.getProducer(), req.getEventVersion(), req.getPartitionKey(), req.getIdempotencyKey())) {
            return;
        }
        upsertSupplier(req);
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

    private void upsertSupplier(SupplierEventRequest req) {
        SupplierEventRequest.SupplierPayload s = req.getSupplier();
        String supplierCode = s.getSupplierCode();

        Long existingId = findSupplierId(supplierCode);

        Map<String, Object> params = new HashMap<>();
        params.put("supplierCode", supplierCode);
        params.put("supplierName", s.getSupplierName());
        params.put("supplierType", s.getType());
        params.put("supplierLevel", s.getCategory());
        params.put("status", s.getStatus() == null || s.getStatus().isBlank() ? "ACTIVE" : s.getStatus());
        params.put("contactPerson", s.getContactPerson());
        params.put("contactPhone", s.getContactPhone());
        params.put("contactEmail", s.getEmail());
        params.put("address", s.getAddress());
        params.put("remark", req.getEventType());

        if (existingId == null) {
            jdbcTemplate.update(
                    "INSERT INTO scm_supplier (supplier_code, supplier_name, supplier_type, supplier_level, status, contact_person, contact_phone, contact_email, address, created_by, created_time, updated_by, updated_time, remark) "
                            + "VALUES (:supplierCode, :supplierName, :supplierType, :supplierLevel, :status, :contactPerson, :contactPhone, :contactEmail, :address, 'srm-event', NOW(), 'srm-event', NOW(), :remark)",
                    params
            );
        } else {
            jdbcTemplate.update(
                    "UPDATE scm_supplier SET supplier_name=:supplierName, supplier_type=:supplierType, supplier_level=:supplierLevel, status=:status, "
                            + "contact_person=:contactPerson, contact_phone=:contactPhone, contact_email=:contactEmail, address=:address, "
                            + "updated_by='srm-event', updated_time=NOW(), remark=:remark WHERE supplier_code=:supplierCode",
                    params
            );
        }
    }

    private Long findSupplierId(String supplierCode) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM scm_supplier WHERE supplier_code=:supplierCode LIMIT 1",
                new MapSqlParameterSource("supplierCode", supplierCode),
                (rs, rowNum) -> rs.getLong("id")
        );
        return ids.isEmpty() ? null : ids.get(0);
    }
}
