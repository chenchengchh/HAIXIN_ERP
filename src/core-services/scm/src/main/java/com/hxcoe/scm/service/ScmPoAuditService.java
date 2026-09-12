package com.hxcoe.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.scm.entity.ScmPoStatusHistoryEntity;
import com.hxcoe.scm.entity.ScmPoSupplierCommitEntity;
import com.hxcoe.scm.repository.ScmPoStatusHistoryRepository;
import com.hxcoe.scm.repository.ScmPoSupplierCommitRepository;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScmPoAuditService {

    @Autowired
    private ScmPoStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ScmPoSupplierCommitRepository supplierCommitRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void recordStatusChange(String orderNo, Integer fromStatus, Integer toStatus, String eventType, String operator, Map<String, Object> detail) {
        if (orderNo == null || orderNo.isBlank()) {
            return;
        }
        ScmPoStatusHistoryEntity h = new ScmPoStatusHistoryEntity();
        h.setOrderNo(orderNo);
        h.setFromStatus(fromStatus);
        h.setToStatus(toStatus);
        h.setEventType(eventType == null || eventType.isBlank() ? "PO_UPDATED" : eventType);
        h.setOperator(operator);
        h.setDetailJson(writeJson(detail));
        statusHistoryRepository.save(h);
    }

    @Transactional
    public void upsertSupplierCommit(String orderNo, String supplierCode, LocalDateTime commitDeliveryDate, String confirmStatus, String operator, Map<String, Object> payload) {
        if (orderNo == null || orderNo.isBlank()) {
            return;
        }
        String sc = supplierCode == null ? "" : supplierCode.trim();
        if (sc.isBlank()) {
            sc = "UNKNOWN";
        }

        ScmPoSupplierCommitEntity e = supplierCommitRepository.findByOrderNo(orderNo).orElse(null);
        if (e == null) {
            e = new ScmPoSupplierCommitEntity();
            e.setOrderNo(orderNo);
        }
        e.setSupplierCode(sc);
        e.setCommitDeliveryDate(commitDeliveryDate);
        e.setConfirmStatus(confirmStatus);
        e.setOperator(operator);
        e.setPayloadJson(writeJson(payload));
        supplierCommitRepository.save(e);
    }

    private String writeJson(Map<String, Object> detail) {
        try {
            return objectMapper.writeValueAsString(detail == null ? Map.of() : detail);
        } catch (Exception e) {
            return "{}";
        }
    }
}

