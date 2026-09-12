package com.hxcoe.wms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.wms.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.wms.entity.IntegrationInboxEntity;
import com.hxcoe.wms.entity.PoInstructionEntity;
import com.hxcoe.wms.repository.IntegrationInboxRepository;
import com.hxcoe.wms.repository.PoInstructionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WmsPoInstructionService {

    @Autowired
    private IntegrationInboxRepository integrationInboxRepository;

    @Autowired
    private PoInstructionRepository poInstructionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public boolean applyScmEvent(PurchaseOrderEventRequest req) {
        if (req == null) {
            return false;
        }
        String eventKey = (req.getEventKey() == null || req.getEventKey().isBlank()) ? req.getIdempotencyKey() : req.getEventKey();
        if (eventKey == null || eventKey.isBlank()) {
            return false;
        }

        // 重试同一事件时，若 Inbox 已存在，说明此前已成功接收，按幂等成功处理。
        if (!tryAcquireInbox(eventKey, req)) {
            return true;
        }

        PurchaseOrderEventRequest.PurchaseOrderPayload po = req.getPurchaseOrder();
        if (po == null || po.getOrderNo() == null || po.getOrderNo().isBlank()) {
            return true;
        }

        PoInstructionEntity e = poInstructionRepository.findByPoNo(po.getOrderNo()).orElseGet(PoInstructionEntity::new);
        e.setPoNo(po.getOrderNo());
        e.setLastEventType(req.getEventType() == null || req.getEventType().isBlank() ? "UNKNOWN" : req.getEventType());
        try {
            e.setPayloadJson(objectMapper.writeValueAsString(po));
        } catch (Exception ignore) {
            e.setPayloadJson("{}");
        }
        poInstructionRepository.save(e);
        return true;
    }

    private boolean tryAcquireInbox(String eventKey, PurchaseOrderEventRequest req) {
        if (integrationInboxRepository.findByEventKey(eventKey).isPresent()) {
            return false;
        }

        IntegrationInboxEntity inbox = new IntegrationInboxEntity();
        inbox.setEventKey(eventKey);
        inbox.setEventId(req.getEventId());
        inbox.setTraceId(req.getTraceId() == null || req.getTraceId().isBlank() ? req.getEventId() : req.getTraceId());
        inbox.setProducer(req.getProducer());
        inbox.setEventVersion(req.getEventVersion());
        inbox.setPartitionKey(req.getPartitionKey());
        inbox.setIdempotencyKey(req.getIdempotencyKey() == null || req.getIdempotencyKey().isBlank() ? eventKey : req.getIdempotencyKey());
        inbox.setEventType(req.getEventType() == null || req.getEventType().isBlank() ? "UNKNOWN" : req.getEventType());
        try {
            integrationInboxRepository.save(inbox);
            return true;
        } catch (DataIntegrityViolationException ignored) {
            return false;
        }
    }
}

