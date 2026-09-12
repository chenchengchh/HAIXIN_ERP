package com.hxcoe.wms.service;

import com.hxcoe.common.api.ResponseStatusAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.wms.dto.integration.LocationEventRequest;
import com.hxcoe.wms.dto.integration.WarehouseEventRequest;
import com.hxcoe.wms.entity.IntegrationTaskEntity;
import com.hxcoe.wms.entity.LocationEntity;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.repository.IntegrationTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class MasterDataEventOutboxService {

    public static final String ACTION_TYPE_ERP_WAREHOUSE_EVENT = "ERP_WAREHOUSE_EVENT";
    public static final String ACTION_TYPE_ERP_LOCATION_EVENT = "ERP_LOCATION_EVENT";
    public static final String ACTION_TYPE_SCM_WAREHOUSE_EVENT = "SCM_WAREHOUSE_EVENT";
    public static final String ACTION_TYPE_SCM_LOCATION_EVENT = "SCM_LOCATION_EVENT";

    @Autowired
    private IntegrationTaskRepository integrationTaskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IntegrationTransportProperties integrationTransportProperties;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Value("${wms.integration.erp-base-url:http://erp:8081}")
    private String erpBaseUrl;

    @Value("${wms.integration.scm-base-url:http://scm:8093}")
    private String scmBaseUrl;

    @Transactional
    public void enqueueWarehouseCreatedOrUpdated(WarehouseEntity wh, String eventType) {
        if (wh == null || wh.getId() == null) {
            return;
        }
        if (wh.getWarehouseCode() == null || wh.getWarehouseCode().isBlank()) {
            return;
        }
        String et = eventType == null || eventType.isBlank() ? "WAREHOUSE_CHANGED" : eventType;
        LocalDateTime ut = wh.getUpdatedTime() == null ? LocalDateTime.now() : wh.getUpdatedTime();
        String baseEventKey = et + ":" + wh.getWarehouseCode() + ":" + ut.toString();
        WarehouseEventRequest req = buildWarehouseEvent(wh, et, baseEventKey);
        enqueueIfAbsent(ACTION_TYPE_ERP_WAREHOUSE_EVENT, "ERP:" + baseEventKey, wh.getWarehouseCode(), req);
        enqueueIfAbsent(ACTION_TYPE_SCM_WAREHOUSE_EVENT, "SCM:" + baseEventKey, wh.getWarehouseCode(), req);
    }

    @Transactional
    public void enqueueWarehouseDeleted(String warehouseCode) {
        if (warehouseCode == null || warehouseCode.isBlank()) {
            return;
        }
        String baseEventKey = "WAREHOUSE_DELETED:" + warehouseCode + ":" + LocalDateTime.now().toString();
        WarehouseEventRequest req = new WarehouseEventRequest();
        req.setEventId(UUID.randomUUID().toString());
        req.setEventType("WAREHOUSE_DELETED");
        req.setEventKey(baseEventKey);
        req.setIdempotencyKey(baseEventKey);
        req.setPartitionKey(warehouseCode);
        req.setEventVersion(1);
        req.setProducer("wms-service");
        req.setEventTime(LocalDateTime.now());
        WarehouseEventRequest.WarehousePayload payload = new WarehouseEventRequest.WarehousePayload();
        payload.setWarehouseCode(warehouseCode);
        payload.setUpdatedTime(LocalDateTime.now());
        req.setWarehouse(payload);
        enqueueIfAbsent(ACTION_TYPE_ERP_WAREHOUSE_EVENT, "ERP:" + baseEventKey, warehouseCode, req);
        enqueueIfAbsent(ACTION_TYPE_SCM_WAREHOUSE_EVENT, "SCM:" + baseEventKey, warehouseCode, req);
    }

    @Transactional
    public void enqueueLocationCreatedOrUpdated(LocationEntity loc, String eventType) {
        if (loc == null || loc.getId() == null) {
            return;
        }
        if (loc.getLocationCode() == null || loc.getLocationCode().isBlank()) {
            return;
        }
        String et = eventType == null || eventType.isBlank() ? "LOCATION_CHANGED" : eventType;
        LocalDateTime ut = loc.getUpdatedTime() == null ? LocalDateTime.now() : loc.getUpdatedTime();
        String baseEventKey = et + ":" + loc.getLocationCode() + ":" + ut.toString();
        LocationEventRequest req = buildLocationEvent(loc, et, baseEventKey);
        enqueueIfAbsent(ACTION_TYPE_ERP_LOCATION_EVENT, "ERP:" + baseEventKey, loc.getLocationCode(), req);
        enqueueIfAbsent(ACTION_TYPE_SCM_LOCATION_EVENT, "SCM:" + baseEventKey, loc.getLocationCode(), req);
    }

    @Transactional
    public void enqueueLocationDeleted(String locationCode) {
        if (locationCode == null || locationCode.isBlank()) {
            return;
        }
        String baseEventKey = "LOCATION_DELETED:" + locationCode + ":" + LocalDateTime.now().toString();
        LocationEventRequest req = new LocationEventRequest();
        req.setEventId(UUID.randomUUID().toString());
        req.setEventType("LOCATION_DELETED");
        req.setEventKey(baseEventKey);
        req.setIdempotencyKey(baseEventKey);
        req.setPartitionKey(locationCode);
        req.setEventVersion(1);
        req.setProducer("wms-service");
        req.setEventTime(LocalDateTime.now());
        LocationEventRequest.LocationPayload payload = new LocationEventRequest.LocationPayload();
        payload.setLocationCode(locationCode);
        payload.setUpdatedTime(LocalDateTime.now());
        req.setLocation(payload);
        enqueueIfAbsent(ACTION_TYPE_ERP_LOCATION_EVENT, "ERP:" + baseEventKey, locationCode, req);
        enqueueIfAbsent(ACTION_TYPE_SCM_LOCATION_EVENT, "SCM:" + baseEventKey, locationCode, req);
    }

    private void enqueueIfAbsent(String actionType, String idempotencyKey, String refNo, Object req) {
        Optional<IntegrationTaskEntity> existing = integrationTaskRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return;
        }
        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setActionType(actionType);
        if (req instanceof WarehouseEventRequest) {
            WarehouseEventRequest r = (WarehouseEventRequest) req;
            task.setEventId(r.getEventId());
            task.setTraceId(r.getTraceId());
            task.setProducer(r.getProducer());
            task.setEventVersion(r.getEventVersion());
            task.setPartitionKey(r.getPartitionKey());
        } else if (req instanceof LocationEventRequest) {
            LocationEventRequest r = (LocationEventRequest) req;
            task.setEventId(r.getEventId());
            task.setTraceId(r.getTraceId());
            task.setProducer(r.getProducer());
            task.setEventVersion(r.getEventVersion());
            task.setPartitionKey(r.getPartitionKey());
        }
        task.setIdempotencyKey(idempotencyKey);
        task.setExternalRefNo(refNo);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setNextRetryAt(LocalDateTime.now());
        try {
            task.setRequestBody(objectMapper.writeValueAsString(req));
        } catch (Exception e) {
            task.setRequestBody(null);
        }
        integrationTaskRepository.save(task);
    }

    @Transactional
    public int trySendPendingBatch() {
        List<IntegrationTaskEntity> list = integrationTaskRepository.findTop50ByStatusInAndNextRetryAtBeforeOrderByNextRetryAtAsc(
                List.of("PENDING", "FAILED"),
                LocalDateTime.now()
        );
        int processed = 0;
        for (IntegrationTaskEntity task : list) {
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

        if (integrationTransportProperties.getTransport() == IntegrationTransportProperties.Transport.REDIS_STREAM) {
            if (ACTION_TYPE_SCM_WAREHOUSE_EVENT.equals(task.getActionType()) || ACTION_TYPE_SCM_LOCATION_EVENT.equals(task.getActionType())) {
                if (stringRedisTemplate == null) {
                    markFailed(task, "Redis不可用");
                    return;
                }
                String stream = ACTION_TYPE_SCM_WAREHOUSE_EVENT.equals(task.getActionType())
                        ? integrationTransportProperties.getRedis().getStreamWmsWarehouseEvents()
                        : integrationTransportProperties.getRedis().getStreamWmsLocationEvents();
                stringRedisTemplate.opsForStream().add(stream, Map.of(
                        "body", task.getRequestBody(),
                        "eventId", task.getEventId() == null ? "" : task.getEventId(),
                        "traceId", task.getTraceId() == null ? "" : task.getTraceId(),
                        "idempotencyKey", task.getIdempotencyKey() == null ? "" : task.getIdempotencyKey(),
                        "actionType", task.getActionType() == null ? "" : task.getActionType()
                ));
                task.setStatus("CONFIRMED");
                task.setLastError(null);
                task.setNextRetryAt(null);
                integrationTaskRepository.save(task);
                return;
            }
        }

        String url = resolveUrl(task.getActionType(), task.getRequestBody());
        if (url == null) {
            markFailed(task, "无法识别目标URL");
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
            task.setNextRetryAt(null);
            integrationTaskRepository.save(task);
        } catch (Exception ex) {
            markFailed(task, ex.getMessage());
        }
    }

    private String resolveUrl(String actionType, String requestBody) {
        if (actionType == null || actionType.isBlank()) {
            return null;
        }
        boolean isWarehouse = requestBody.contains("\"warehouse\"");
        if (ACTION_TYPE_ERP_WAREHOUSE_EVENT.equals(actionType)) {
            return erpBaseUrl + "/api/v1/erp/integration/wms/warehouse-events";
        }
        if (ACTION_TYPE_ERP_LOCATION_EVENT.equals(actionType)) {
            return erpBaseUrl + "/api/v1/erp/integration/wms/location-events";
        }
        if (ACTION_TYPE_SCM_WAREHOUSE_EVENT.equals(actionType)) {
            return scmBaseUrl + "/api/v1/scm/integration/wms/warehouse-events";
        }
        if (ACTION_TYPE_SCM_LOCATION_EVENT.equals(actionType)) {
            return scmBaseUrl + "/api/v1/scm/integration/wms/location-events";
        }
        if (isWarehouse) {
            return scmBaseUrl + "/api/v1/scm/integration/wms/warehouse-events";
        }
        return null;
    }

    private void markFailed(IntegrationTaskEntity task, String error) {
        int next = task.getRetryCount() == null ? 1 : task.getRetryCount() + 1;
        task.setRetryCount(next);
        task.setStatus("FAILED");
        task.setLastError(error);
        task.setNextRetryAt(LocalDateTime.now().plusSeconds(Math.min(600L, 5L * next)));
        integrationTaskRepository.save(task);
        log.warn("WMS主数据事件推送失败 eventId={} traceId={} actionType={} idempotencyKey={} retry={} error={}",
                task.getEventId(), task.getTraceId(), task.getActionType(), task.getIdempotencyKey(), next, error);
    }

    private WarehouseEventRequest buildWarehouseEvent(WarehouseEntity wh, String eventType, String eventKey) {
        WarehouseEventRequest req = new WarehouseEventRequest();
        req.setEventId(UUID.randomUUID().toString());
        req.setTraceId(req.getEventId());
        req.setEventType(eventType);
        req.setEventKey(eventKey);
        req.setIdempotencyKey(eventKey);
        req.setPartitionKey(wh.getWarehouseCode());
        req.setEventVersion(1);
        req.setProducer("wms-service");
        req.setEventTime(LocalDateTime.now());
        WarehouseEventRequest.WarehousePayload payload = new WarehouseEventRequest.WarehousePayload();
        payload.setWarehouseId(wh.getId());
        payload.setWarehouseCode(wh.getWarehouseCode());
        payload.setWarehouseName(wh.getWarehouseName());
        payload.setAddress(wh.getAddress());
        payload.setManager(wh.getManager());
        payload.setContact(wh.getContact());
        payload.setStatus(wh.getStatus());
        payload.setUpdatedTime(wh.getUpdatedTime());
        req.setWarehouse(payload);
        return req;
    }

    private LocationEventRequest buildLocationEvent(LocationEntity loc, String eventType, String eventKey) {
        LocationEventRequest req = new LocationEventRequest();
        req.setEventId(UUID.randomUUID().toString());
        req.setTraceId(req.getEventId());
        req.setEventType(eventType);
        req.setEventKey(eventKey);
        req.setIdempotencyKey(eventKey);
        req.setPartitionKey(loc.getLocationCode());
        req.setEventVersion(1);
        req.setProducer("wms-service");
        req.setEventTime(LocalDateTime.now());
        LocationEventRequest.LocationPayload payload = new LocationEventRequest.LocationPayload();
        payload.setLocationId(loc.getId());
        payload.setLocationCode(loc.getLocationCode());
        payload.setLocationName(loc.getLocationName());
        payload.setWarehouseCode(loc.getWarehouseCode());
        payload.setZoneCode(loc.getZoneCode());
        payload.setLocationTypeCode(loc.getLocationTypeCode());
        payload.setStatus(loc.getStatus());
        payload.setRemark(loc.getRemark());
        payload.setUpdatedTime(loc.getUpdatedTime());
        req.setLocation(payload);
        return req;
    }
}
