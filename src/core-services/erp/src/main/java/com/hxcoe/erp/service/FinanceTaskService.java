package com.hxcoe.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.erp.dto.integration.PoFactRequest;
import com.hxcoe.erp.entity.FinanceTaskEntity;
import com.hxcoe.erp.repository.FinanceTaskRepository;
import com.hxcoe.erp.support.FinanceTaskStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class FinanceTaskService {

    @Autowired
    private FinanceTaskRepository financeTaskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public FinanceTaskEntity createFromPoFact(PoFactRequest req) {
        if (req == null || req.getFact() == null) {
            return null;
        }
        String idempotencyKey = req.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = req.getEventKey();
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = "ERP:PO_FACT:" + (req.getEventId() == null ? LocalDateTime.now() : req.getEventId());
        }

        Optional<FinanceTaskEntity> existing = financeTaskRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        FinanceTaskEntity e = new FinanceTaskEntity();
        e.setFactType(req.getFact().getFactType());
        e.setSourceType("PO");
        e.setSourceNo(req.getFact().getPoNo());
        e.setRefNo(req.getFact().getRefNo());
        e.setResult(req.getFact().getResult());
        e.setEventId(req.getEventId());
        e.setTraceId(req.getTraceId() == null || req.getTraceId().isBlank() ? req.getEventId() : req.getTraceId());
        e.setProducer(req.getProducer());
        e.setEventVersion(req.getEventVersion() == null ? 1 : req.getEventVersion());
        e.setPartitionKey(req.getPartitionKey());
        e.setIdempotencyKey(idempotencyKey);
        e.setStatus(FinanceTaskStatus.PENDING);
        try {
            e.setPayloadJson(objectMapper.writeValueAsString(req));
        } catch (Exception ex) {
            log.warn("ERP财务任务载荷序列化失败 eventId={} traceId={} error={}", e.getEventId(), e.getTraceId(), ex.getMessage());
            e.setPayloadJson(null);
        }
        return financeTaskRepository.save(e);
    }
}
