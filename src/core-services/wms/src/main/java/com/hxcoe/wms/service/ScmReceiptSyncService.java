package com.hxcoe.wms.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.wms.client.ScmIntegrationClient;
import com.hxcoe.wms.client.dto.ScmReceiptCompletedRequest;
import com.hxcoe.wms.entity.AsnEntity;
import com.hxcoe.wms.entity.AsnItemEntity;
import com.hxcoe.wms.entity.IntegrationOutboxEntity;
import com.hxcoe.wms.repository.AsnRepository;
import com.hxcoe.wms.repository.IntegrationOutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ScmReceiptSyncService {

    public static final String EVENT_ASN_RECEIVED = "WMS_ASN_RECEIVED";

    @Autowired
    private IntegrationOutboxRepository outboxRepository;

    @Autowired
    private AsnRepository asnRepository;

    @Autowired(required = false)
    private ScmIntegrationClient scmIntegrationClient;

    @Transactional
    public void enqueueAsnReceivedIfAbsent(AsnEntity asn) {
        if (asn == null || asn.getId() == null) return;
        if (asn.getAsnNo() == null || asn.getAsnNo().isBlank()) return;
        outboxRepository.findByEventTypeAndRefNo(EVENT_ASN_RECEIVED, asn.getAsnNo()).orElseGet(() -> {
            IntegrationOutboxEntity e = new IntegrationOutboxEntity();
            e.setEventType(EVENT_ASN_RECEIVED);
            e.setRefNo(asn.getAsnNo());
            e.setEntityId(asn.getId());
            e.setEventId(UUID.randomUUID().toString());
            e.setTraceId(e.getEventId());
            e.setProducer("wms-service");
            e.setEventVersion(1);
            e.setPartitionKey(asn.getAsnNo());
            e.setIdempotencyKey(EVENT_ASN_RECEIVED + ":" + asn.getAsnNo());
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
        Optional<AsnEntity> optional = asnRepository.findById(outbox.getEntityId());
        if (optional.isEmpty()) {
            markFailed(outbox, "ASN不存在");
            return;
        }
        AsnEntity asn = optional.get();
        String poNo = asn.getDeliveryNoteNo();
        if (poNo == null || poNo.isBlank()) {
            markFailed(outbox, "poNo缺失");
            return;
        }

        ScmReceiptCompletedRequest req = new ScmReceiptCompletedRequest();
        req.setPoNo(poNo);
        req.setReceiptNo(asn.getAsnNo());
        List<ScmReceiptCompletedRequest.ScmReceiptLine> lines = new ArrayList<>();
        if (asn.getItems() != null) {
            for (AsnItemEntity it : asn.getItems()) {
                if (it == null || it.getMaterialCode() == null || it.getMaterialCode().isBlank()) continue;
                BigDecimal qty = it.getReceivedQuantity() == null ? BigDecimal.ZERO : it.getReceivedQuantity();
                if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;
                ScmReceiptCompletedRequest.ScmReceiptLine line = new ScmReceiptCompletedRequest.ScmReceiptLine();
                line.setMaterialCode(it.getMaterialCode());
                line.setQty(qty);
                lines.add(line);
            }
        }
        if (lines.isEmpty()) {
            markFailed(outbox, "收货明细为空");
            return;
        }
        req.setLines(lines);

        try {
            Result<Map<String, Object>> res = scmIntegrationClient.receiptCompleted(req);
            if (!isRemoteSuccess(res)) {
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
            if (!EVENT_ASN_RECEIVED.equals(e.getEventType())) continue;
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
        log.warn("WMS->SCM回传失败 eventId={} traceId={} eventType={} refNo={} retry={} error={}",
                outbox.getEventId(), outbox.getTraceId(), outbox.getEventType(), outbox.getRefNo(), next, error);
    }

    private boolean isRemoteSuccess(Result<?> result) {
        Integer code = result == null ? null : result.getCode();
        return code != null && (code == 0 || code == 200);
    }
}
