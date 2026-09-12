package com.hxcoe.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.erp.dto.integration.MaterialEventRequest;
import com.hxcoe.erp.entity.IntegrationTaskEntity;
import com.hxcoe.erp.entity.MaterialEntity;
import com.hxcoe.erp.repository.IntegrationTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class MaterialEventOutboxService {

    public static final String ACTION_TYPE_BOM_MATERIAL_EVENT = "BOM_MATERIAL_EVENT";
    public static final String ACTION_TYPE_SRM_MATERIAL_EVENT = "SRM_MATERIAL_EVENT";

    @Autowired
    private IntegrationTaskRepository integrationTaskRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${erp.integration.bom-base-url:http://bom:8085}")
    private String bomBaseUrl;

    @Value("${erp.integration.srm-base-url:http://srm:8092}")
    private String srmBaseUrl;

    @Transactional
    public void enqueueMaterialEvent(MaterialEntity material, String eventType) {
        if (material == null) {
            return;
        }
        if (material.getMaterialCode() == null || material.getMaterialCode().isBlank()) {
            return;
        }
        if (eventType == null || eventType.isBlank()) {
            return;
        }

        LocalDateTime ut = material.getUpdatedTime() == null ? LocalDateTime.now() : material.getUpdatedTime();
        String eventKey = eventType + ":" + material.getMaterialCode() + ":" + ut.toString();
        MaterialEventRequest req = buildEvent(material, eventType, eventKey);

        enqueueIfAbsent(ACTION_TYPE_BOM_MATERIAL_EVENT, "BOM:" + eventKey, material.getMaterialCode(), req);
        enqueueIfAbsent(ACTION_TYPE_SRM_MATERIAL_EVENT, "SRM:" + eventKey, material.getMaterialCode(), req);
    }

    private void enqueueIfAbsent(String actionType, String idempotencyKey, String refNo, MaterialEventRequest req) {
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

        String url;
        if (ACTION_TYPE_BOM_MATERIAL_EVENT.equals(task.getActionType())) {
            url = bomBaseUrl + "/api/v1/bom/integration/erp/material-events";
        } else if (ACTION_TYPE_SRM_MATERIAL_EVENT.equals(task.getActionType())) {
            url = srmBaseUrl + "/api/v1/srm/integration/erp/material-events";
        } else {
            markFailed(task, "未知actionType");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(task.getRequestBody(), headers);

        try {
            Map<?, ?> raw = restTemplate.postForObject(url, entity, Map.class);
            if (!ResponseStatusAdapter.isSuccess(raw)) {
                String message = ResponseStatusAdapter.extractMessage(raw);
                markFailed(task, raw == null ? "下游无响应" : ("下游失败:" + (message == null ? "未知错误" : message)));
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
        log.warn("ERP->下游 物料事件推送失败 eventId={} traceId={} actionType={} idempotencyKey={} retry={} error={}",
                task.getEventId(), task.getTraceId(), task.getActionType(), task.getIdempotencyKey(), next, error);
    }

    private MaterialEventRequest buildEvent(MaterialEntity material, String eventType, String eventKey) {
        MaterialEventRequest req = new MaterialEventRequest();
        req.setEventId(UUID.randomUUID().toString());
        req.setTraceId(req.getEventId());
        req.setEventType(eventType);
        req.setEventKey(eventKey);
        req.setIdempotencyKey(eventKey);
        req.setPartitionKey(material.getMaterialCode());
        req.setEventVersion(1);
        req.setProducer("erp-service");
        req.setEventTime(LocalDateTime.now());

        MaterialEventRequest.MaterialPayload payload = new MaterialEventRequest.MaterialPayload();
        payload.setMaterialId(material.getId());
        payload.setMaterialCode(material.getMaterialCode());
        payload.setMaterialName(material.getMaterialName());
        payload.setMaterialType(material.getMaterialType());
        payload.setSpecification(material.getSpecification());
        payload.setUnit(material.getUnit());
        payload.setUnitPrice(material.getUnitPrice());
        payload.setApprovalStatus(material.getApprovalStatus());
        payload.setStatus(material.getStatus());
        payload.setIsDeleted(material.getIsDeleted());
        payload.setUpdatedTime(material.getUpdatedTime());
        req.setMaterial(payload);
        return req;
    }
}
