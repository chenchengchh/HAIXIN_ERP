package com.hxcoe.srm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.Result;
import com.hxcoe.srm.client.ErpSupplierEventClient;
import com.hxcoe.srm.client.ScmSupplierEventClient;
import com.hxcoe.srm.dto.integration.SupplierEventRequest;
import com.hxcoe.srm.entity.IntegrationTaskEntity;
import com.hxcoe.srm.entity.SupplierEntity;
import com.hxcoe.srm.repository.IntegrationTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SupplierEventOutboxService {

    public static final String ACTION_TYPE_SCM_SUPPLIER_EVENT = "SCM_SUPPLIER_EVENT";
    public static final String ACTION_TYPE_ERP_SUPPLIER_EVENT = "ERP_SUPPLIER_EVENT";

    @Autowired
    private IntegrationTaskRepository integrationTaskRepository;

    @Autowired
    private ScmSupplierEventClient scmSupplierEventClient;

    @Autowired
    private ErpSupplierEventClient erpSupplierEventClient;

    @Autowired
    private ObjectMapper objectMapper;


    @Transactional
    public void enqueueSupplierEvent(SupplierEntity supplier, String eventType) {
        if (supplier == null) {
            return;
        }
        if (supplier.getSupplierCode() == null || supplier.getSupplierCode().isBlank()) {
            return;
        }
        if (eventType == null || eventType.isBlank()) {
            return;
        }

        LocalDateTime ut = supplier.getUpdatedTime() == null ? LocalDateTime.now() : supplier.getUpdatedTime();
        String baseEventKey = eventType + ":" + supplier.getSupplierCode() + ":" + ut.toString();
        SupplierEventRequest req = buildEvent(supplier, eventType, baseEventKey);

        enqueueIfAbsent(ACTION_TYPE_SCM_SUPPLIER_EVENT, "SCM:" + baseEventKey, supplier.getSupplierCode(), req);
        enqueueIfAbsent(ACTION_TYPE_ERP_SUPPLIER_EVENT, "ERP:" + baseEventKey, supplier.getSupplierCode(), req);
    }

    private void enqueueIfAbsent(String actionType, String idempotencyKey, String refNo, SupplierEventRequest req) {
        Optional<IntegrationTaskEntity> existing = integrationTaskRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return;
        }
        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setActionType(actionType);
        task.setEventId(req.getEventId());
        task.setTraceId(req.getTraceId());
        task.setProducer(req.getProducer());
        task.setEventVersion(req.getEventVersion());
        task.setPartitionKey(req.getPartitionKey());
        task.setIdempotencyKey(idempotencyKey);
        task.setExternalRefNo(refNo);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        try {
            task.setRequestBody(objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            task.setRequestBody(null);
        }
        integrationTaskRepository.save(task);
    }

    @Transactional
    public int trySendPendingBatch() {
        List<IntegrationTaskEntity> tasks = integrationTaskRepository.findTop50ByStatusInOrderByCreatedTimeAsc(List.of("PENDING", "FAILED"));
        int processed = 0;
        for (IntegrationTaskEntity task : tasks) {
            if (task == null) {
                continue;
            }
            if ("CONFIRMED".equalsIgnoreCase(task.getStatus())) {
                continue;
            }
            trySendOne(task);
            processed++;
        }
        return processed;
    }

    @Transactional
    public void trySendOne(IntegrationTaskEntity task) {
        if (task == null) {
            return;
        }
        if (task.getRequestBody() == null || task.getRequestBody().isBlank()) {
            markFailed(task, "requestBody为空");
            return;
        }

        SupplierEventRequest request;
        try {
            request = objectMapper.readValue(task.getRequestBody(), SupplierEventRequest.class);
        } catch (Exception ex) {
            markFailed(task, "requestBody反序列化失败: " + ex.getMessage());
            return;
        }

        try {
            Result<Map<String, Object>> result = sendByActionType(task.getActionType(), request);
            if (result == null || !ResponseStatusAdapter.isSuccess(result.getCode())) {
                String message = result == null ? null : result.getMessage();
                markFailed(task, targetLabel(task.getActionType()) + "失败:" + (message == null ? "无响应" : message));
                return;
            }
            task.setStatus("CONFIRMED");
            task.setLastError(null);
            integrationTaskRepository.save(task);
        } catch (Exception ex) {
            markFailed(task, ex.getMessage());
        }
    }

    private void markFailed(IntegrationTaskEntity task, String error) {
        int next = task.getRetryCount() == null ? 1 : task.getRetryCount() + 1;
        task.setRetryCount(next);
        task.setStatus("FAILED");
        task.setLastError(error);
        integrationTaskRepository.save(task);
        log.warn("SRM Supplier事件推送失败 eventId={} traceId={} actionType={} idempotencyKey={} retry={} error={}",
                task.getEventId(), task.getTraceId(), task.getActionType(), task.getIdempotencyKey(), next, error);
    }

    private Result<Map<String, Object>> sendByActionType(String actionType, SupplierEventRequest request) {
        if (ACTION_TYPE_SCM_SUPPLIER_EVENT.equals(actionType)) {
            return scmSupplierEventClient.receiveSupplierEvent(request);
        }
        if (ACTION_TYPE_ERP_SUPPLIER_EVENT.equals(actionType)) {
            return erpSupplierEventClient.receiveSupplierEvent(request);
        }
        return null;
    }

    private String targetLabel(String actionType) {
        if (ACTION_TYPE_ERP_SUPPLIER_EVENT.equals(actionType)) {
            return "ERP";
        }
        return "SCM";
    }

    private SupplierEventRequest buildEvent(SupplierEntity supplier, String eventType, String eventKey) {
        SupplierEventRequest req = new SupplierEventRequest();
        req.setEventId(UUID.randomUUID().toString());
        req.setTraceId(req.getEventId());
        req.setEventType(eventType);
        req.setEventKey(eventKey);
        req.setIdempotencyKey(eventKey);
        req.setPartitionKey(supplier.getSupplierCode());
        req.setEventVersion(1);
        req.setProducer("srm-service");
        req.setEventTime(LocalDateTime.now());

        SupplierEventRequest.SupplierPayload payload = new SupplierEventRequest.SupplierPayload();
        payload.setSupplierId(supplier.getId());
        payload.setSupplierCode(supplier.getSupplierCode());
        payload.setSupplierName(supplier.getSupplierName());
        payload.setStatus(supplier.getStatus());
        payload.setType(supplier.getType());
        payload.setCategory(supplier.getCategory());
        payload.setContactPerson(supplier.getContactPerson());
        payload.setContactPhone(supplier.getContactPhone());
        payload.setEmail(supplier.getEmail());
        payload.setAddress(supplier.getAddress());
        payload.setUpdatedTime(supplier.getUpdatedTime());
        req.setSupplier(payload);
        return req;
    }
}
