package com.hxcoe.wms.service;

import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.Result;
import com.hxcoe.wms.client.QmsIntegrationClient;
import com.hxcoe.wms.client.dto.QmsReceiptTriggerRequest;
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

/**
 * WMS 收货完成 → QMS IQC 触发的 Outbox 同步服务。
 * <p>复用 {@code wms_integration_outbox} 表，通过 {@code eventType=WMS_ASN_RECEIVED_QMS}
 * 与 SCM 收货回写事件（WMS_ASN_RECEIVED）区分，互不干扰。</p>
 */
@Slf4j
@Service
public class QmsReceiptTriggerSyncService {

    /** Outbox 事件类型：WMS 收货完成触发 QMS 来料检验 */
    public static final String EVENT_ASN_RECEIVED_QMS = "WMS_ASN_RECEIVED_QMS";

    @Autowired
    private IntegrationOutboxRepository outboxRepository;

    @Autowired
    private AsnRepository asnRepository;

    @Autowired(required = false)
    private QmsIntegrationClient qmsIntegrationClient;

    /**
     * 收货完成后幂等入队 QMS 触发事件。
     * <p>同一 ASN 单号只入队一次，避免重复触发 IQC。</p>
     *
     * @param asn 收货完成的 ASN 实体
     */
    @Transactional
    public void enqueueQmsTriggerIfAbsent(AsnEntity asn) {
        if (asn == null || asn.getId() == null) {
            return;
        }
        if (asn.getAsnNo() == null || asn.getAsnNo().isBlank()) {
            return;
        }
        outboxRepository.findByEventTypeAndRefNo(EVENT_ASN_RECEIVED_QMS, asn.getAsnNo()).orElseGet(() -> {
            IntegrationOutboxEntity e = new IntegrationOutboxEntity();
            e.setEventType(EVENT_ASN_RECEIVED_QMS);
            e.setRefNo(asn.getAsnNo());
            e.setEntityId(asn.getId());
            e.setEventId(UUID.randomUUID().toString());
            e.setTraceId(e.getEventId());
            e.setProducer("wms-service");
            e.setEventVersion(1);
            e.setPartitionKey(asn.getAsnNo());
            e.setIdempotencyKey(EVENT_ASN_RECEIVED_QMS + ":" + asn.getAsnNo());
            e.setStatus("PENDING");
            e.setRetryCount(0);
            e.setNextRetryAt(LocalDateTime.now());
            return outboxRepository.save(e);
        });
    }

    /**
     * 发送单条 Outbox 事件到 QMS。
     *
     * @param outbox Outbox 任务实体
     */
    @Transactional
    public void trySendOne(IntegrationOutboxEntity outbox) {
        if (outbox == null) {
            return;
        }
        if (qmsIntegrationClient == null) {
            markFailed(outbox, "QMS集成客户端不可用");
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

        // 构造收货明细行（仅保留有送检数量的物料）
        List<QmsReceiptTriggerRequest.QmsReceiptLine> lines = new ArrayList<>();
        if (asn.getItems() != null) {
            for (AsnItemEntity it : asn.getItems()) {
                if (it == null || it.getMaterialCode() == null || it.getMaterialCode().isBlank()) {
                    continue;
                }
                BigDecimal qty = it.getReceivedQuantity() == null ? BigDecimal.ZERO : it.getReceivedQuantity();
                if (qty.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                QmsReceiptTriggerRequest.QmsReceiptLine line = new QmsReceiptTriggerRequest.QmsReceiptLine();
                line.setMaterialCode(it.getMaterialCode());
                line.setMaterialName(it.getMaterialName());
                line.setBatchNo(it.getBatchNo());
                line.setQty(qty);
                lines.add(line);
            }
        }
        if (lines.isEmpty()) {
            markFailed(outbox, "收货明细为空");
            return;
        }

        QmsReceiptTriggerRequest req = new QmsReceiptTriggerRequest();
        req.setPoNo(poNo);
        req.setReceiptNo(asn.getAsnNo());
        req.setLines(lines);

        try {
            Result<Map<String, Object>> res = qmsIntegrationClient.receiptTriggered(req);
            if (res == null || !ResponseStatusAdapter.isSuccess(res.getCode())) {
                String msg = res == null ? "QMS返回为空" : ("QMS失败:" + res.getMessage());
                markFailed(outbox, msg);
                return;
            }
            outbox.setStatus("SENT");
            outbox.setLastError(null);
            outbox.setNextRetryAt(null);
            outboxRepository.save(outbox);
            log.info("WMS->QMS IQC触发成功 eventId={} asnNo={} poNo={} lineCount={}",
                    outbox.getEventId(), asn.getAsnNo(), poNo, lines.size());
        } catch (Exception ex) {
            markFailed(outbox, ex.getMessage());
        }
    }

    /**
     * 批量扫描 PENDING/FAILED 任务并发送，仅处理 QMS 触发事件类型。
     *
     * @return 本轮处理条数
     */
    @Transactional
    public int trySendPendingBatch() {
        List<IntegrationOutboxEntity> list = outboxRepository.findTop50ByStatusInAndNextRetryAtBeforeOrderByNextRetryAtAsc(
                List.of("PENDING", "FAILED"),
                LocalDateTime.now()
        );
        int processed = 0;
        for (IntegrationOutboxEntity e : list) {
            // 仅处理 QMS 触发事件，SCM 回写由 ScmReceiptSyncService 处理
            if (!EVENT_ASN_RECEIVED_QMS.equals(e.getEventType())) {
                continue;
            }
            if ("SENT".equalsIgnoreCase(e.getStatus())) {
                continue;
            }
            trySendOne(e);
            processed++;
        }
        return processed;
    }

    /**
     * 标记任务失败并设置退避重试时间。
     *
     * @param outbox Outbox 任务实体
     * @param error  错误信息
     */
    private void markFailed(IntegrationOutboxEntity outbox, String error) {
        int next = outbox.getRetryCount() == null ? 1 : outbox.getRetryCount() + 1;
        outbox.setRetryCount(next);
        outbox.setStatus("FAILED");
        outbox.setLastError(error);
        outbox.setNextRetryAt(LocalDateTime.now().plusSeconds(Math.min(600L, 5L * next)));
        outboxRepository.save(outbox);
        log.warn("WMS->QMS IQC触发失败 eventId={} traceId={} eventType={} refNo={} retry={} error={}",
                outbox.getEventId(), outbox.getTraceId(), outbox.getEventType(), outbox.getRefNo(), next, error);
    }
}
