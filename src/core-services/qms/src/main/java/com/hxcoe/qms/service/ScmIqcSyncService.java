package com.hxcoe.qms.service;

import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.client.ScmIntegrationClient;
import com.hxcoe.qms.client.dto.scm.IqcCompletedRequest;
import com.hxcoe.qms.entity.IntegrationOutboxEntity;
import com.hxcoe.qms.entity.QualityInspectionEntity;
import com.hxcoe.qms.repository.IntegrationOutboxRepository;
import com.hxcoe.qms.repository.QualityInspectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ScmIqcSyncService {

    public static final String EVENT_IQC_COMPLETED = "QMS_IQC_COMPLETED";

    @Autowired
    private IntegrationOutboxRepository outboxRepository;

    @Autowired
    private QualityInspectionRepository inspectionRepository;

    @Autowired(required = false)
    private ScmIntegrationClient scmIntegrationClient;

    @Transactional
    public void enqueueIqcCompletedIfAbsent(QualityInspectionEntity inspection) {
        if (inspection == null || inspection.getId() == null) return;
        if (inspection.getInspectionCode() == null || inspection.getInspectionCode().isBlank()) return;
        outboxRepository.findByEventTypeAndRefNo(EVENT_IQC_COMPLETED, inspection.getInspectionCode()).orElseGet(() -> {
            IntegrationOutboxEntity e = new IntegrationOutboxEntity();
            e.setEventType(EVENT_IQC_COMPLETED);
            e.setRefNo(inspection.getInspectionCode());
            e.setEntityId(inspection.getId());
            e.setEventId(UUID.randomUUID().toString());
            e.setTraceId(e.getEventId());
            e.setProducer("qms-service");
            e.setEventVersion(1);
            e.setPartitionKey(inspection.getInspectionCode());
            e.setIdempotencyKey(EVENT_IQC_COMPLETED + ":" + inspection.getInspectionCode());
            e.setStatus("PENDING");
            e.setRetryCount(0);
            e.setNextRetryAt(LocalDateTime.now());
            return outboxRepository.save(e);
        });
    }

    @Transactional
    public void trySendOne(IntegrationOutboxEntity outbox) {
        if (outbox == null) return;
        if (scmIntegrationClient == null) {
            markFailed(outbox, "SCM集成客户端不可用");
            return;
        }
        Optional<QualityInspectionEntity> optional = inspectionRepository.findById(outbox.getEntityId());
        if (optional.isEmpty()) {
            markFailed(outbox, "质检单不存在");
            return;
        }
        QualityInspectionEntity inspection = optional.get();
        if (inspection.getSourceNo() == null || inspection.getSourceNo().isBlank()) {
            markFailed(outbox, "poNo缺失");
            return;
        }
        if (inspection.getInspectionResult() == null || inspection.getInspectionResult().isBlank()) {
            markFailed(outbox, "result缺失");
            return;
        }

        IqcCompletedRequest req = new IqcCompletedRequest();
        req.setPoNo(inspection.getSourceNo());
        req.setQcNo(inspection.getInspectionCode());
        req.setResult(inspection.getInspectionResult());

        try {
            Result<Map<String, Object>> res = scmIntegrationClient.iqcCompleted(req);
            if (res == null || !ResponseStatusAdapter.isSuccess(res.getCode())) {
                markFailed(outbox, res == null ? "SCM返回为空" : ("SCM失败:" + res.getMessage()));
                return;
            }
            outbox.setStatus("SENT");
            outbox.setLastError(null);
            outbox.setNextRetryAt(null);
            outboxRepository.save(outbox);
        } catch (Exception ex) {
            markFailed(outbox, ex.getMessage());
        }
    }

    @Transactional
    public int trySendPendingBatch() {
        List<IntegrationOutboxEntity> list = outboxRepository.findTop50ByStatusInAndNextRetryAtBeforeOrderByNextRetryAtAsc(
                List.of("PENDING", "FAILED"),
                LocalDateTime.now()
        );
        int processed = 0;
        for (IntegrationOutboxEntity e : list) {
            if ("SENT".equalsIgnoreCase(e.getStatus())) continue;
            trySendOne(e);
            processed++;
        }
        return processed;
    }

    private void markFailed(IntegrationOutboxEntity outbox, String error) {
        int next = outbox.getRetryCount() == null ? 1 : outbox.getRetryCount() + 1;
        outbox.setRetryCount(next);
        outbox.setStatus("FAILED");
        outbox.setLastError(error);
        outbox.setNextRetryAt(LocalDateTime.now().plusSeconds(Math.min(600L, 5L * next)));
        outboxRepository.save(outbox);
        log.warn("QMS->SCM回传失败 eventId={} traceId={} eventType={} refNo={} retry={} error={}",
                outbox.getEventId(), outbox.getTraceId(), outbox.getEventType(), outbox.getRefNo(), next, error);
    }
}
