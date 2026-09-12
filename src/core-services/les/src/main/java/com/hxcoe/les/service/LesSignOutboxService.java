package com.hxcoe.les.service;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.dto.les.LesSignCompletedEventDTO;
import com.hxcoe.les.client.CrmSignClient;
import com.hxcoe.les.client.ErpSignClient;
import com.hxcoe.les.entity.IntegrationOutboxEntity;
import com.hxcoe.les.entity.LesSignVoucherEntity;
import com.hxcoe.les.repository.IntegrationOutboxRepository;
import com.hxcoe.les.repository.LesSignVoucherRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * LES 签收完成事件 Outbox 服务。
 * <p>签收凭证创建后幂等入队 ERP/CRM 两条事件，由重试任务异步投递，
 * 回写 ERP/CRM 订单状态，完成 LES 签收回流闭环。</p>
 */
@Slf4j
@Service
public class LesSignOutboxService {

    /** 事件类型：LES 签收完成回写 ERP */
    public static final String EVENT_SIGN_COMPLETED_ERP = "LES_SIGN_COMPLETED_ERP";
    /** 事件类型：LES 签收完成回写 CRM */
    public static final String EVENT_SIGN_COMPLETED_CRM = "LES_SIGN_COMPLETED_CRM";

    @Autowired
    private IntegrationOutboxRepository outboxRepository;

    @Autowired
    private LesSignVoucherRepository signVoucherRepository;

    @Autowired(required = false)
    private ErpSignClient erpSignClient;

    @Autowired(required = false)
    private CrmSignClient crmSignClient;

    /**
     * 签收凭证创建后幂等入队 ERP/CRM 回写事件。
     * <p>同一凭证对同一下游只入队一次；erpOrderNo/crmOrderNo 为空时跳过对应下游。</p>
     *
     * @param voucher 签收凭证实体
     */
    @Transactional
    public void enqueueSignCompletedIfAbsent(LesSignVoucherEntity voucher) {
        if (voucher == null || voucher.getId() == null) {
            return;
        }
        String refNo = String.valueOf(voucher.getId());

        // ERP 回写事件（关联了 ERP 订单号才入队）
        if (voucher.getErpOrderNo() != null && !voucher.getErpOrderNo().isBlank()) {
            enqueueIfAbsent(EVENT_SIGN_COMPLETED_ERP, refNo, voucher.getId(), voucher.getErpOrderNo());
        }
        // CRM 回写事件（关联了 CRM 订单号才入队）
        if (voucher.getCrmOrderNo() != null && !voucher.getCrmOrderNo().isBlank()) {
            enqueueIfAbsent(EVENT_SIGN_COMPLETED_CRM, refNo, voucher.getId(), voucher.getCrmOrderNo());
        }
    }

    /**
     * 幂等入队单条 Outbox 事件。
     *
     * @param eventType    事件类型
     * @param refNo        业务引用号
     * @param entityId     实体 ID
     * @param partitionKey 分区键
     */
    private void enqueueIfAbsent(String eventType, String refNo, Long entityId, String partitionKey) {
        outboxRepository.findByEventTypeAndRefNo(eventType, refNo).orElseGet(() -> {
            IntegrationOutboxEntity e = new IntegrationOutboxEntity();
            e.setEventType(eventType);
            e.setRefNo(refNo);
            e.setEntityId(entityId);
            String eventId = UUID.randomUUID().toString();
            e.setEventId(eventId);
            e.setTraceId(eventId);
            e.setProducer("les-service");
            e.setEventVersion(1);
            e.setPartitionKey(partitionKey);
            e.setIdempotencyKey(eventType + ":" + refNo);
            e.setStatus("PENDING");
            e.setRetryCount(0);
            e.setNextRetryAt(LocalDateTime.now());
            return outboxRepository.save(e);
        });
    }

    /**
     * 发送单条 Outbox 事件到对应下游（ERP/CRM）。
     *
     * @param outbox Outbox 任务实体
     */
    @Transactional
    public void trySendOne(IntegrationOutboxEntity outbox) {
        if (outbox == null) {
            return;
        }
        Optional<LesSignVoucherEntity> optional = signVoucherRepository.findById(outbox.getEntityId());
        if (optional.isEmpty()) {
            markFailed(outbox, "签收凭证不存在");
            return;
        }
        LesSignVoucherEntity voucher = optional.get();
        LesSignCompletedEventDTO event = buildEvent(outbox, voucher);

        String eventType = outbox.getEventType();
        try {
            boolean success;
            if (EVENT_SIGN_COMPLETED_ERP.equals(eventType)) {
                if (erpSignClient == null) {
                    markFailed(outbox, "ERP签收客户端不可用");
                    return;
                }
                ApiResponse<java.util.Map<String, Object>> res = erpSignClient.signCompleted(event);
                success = res != null && ResponseStatusAdapter.isSuccess(res.getCode());
                if (success) {
                    voucher.setErpSyncStatus("SENT");
                    signVoucherRepository.save(voucher);
                } else {
                    markFailed(outbox, res == null ? "ERP返回为空" : ("ERP失败:" + res.getMessage()));
                    return;
                }
            } else if (EVENT_SIGN_COMPLETED_CRM.equals(eventType)) {
                if (crmSignClient == null) {
                    markFailed(outbox, "CRM签收客户端不可用");
                    return;
                }
                ApiResponse<java.util.Map<String, Object>> res = crmSignClient.signCompleted(event);
                success = res != null && ResponseStatusAdapter.isSuccess(res.getCode());
                if (success) {
                    voucher.setCrmSyncStatus("SENT");
                    signVoucherRepository.save(voucher);
                } else {
                    markFailed(outbox, res == null ? "CRM返回为空" : ("CRM失败:" + res.getMessage()));
                    return;
                }
            } else {
                markFailed(outbox, "未知eventType: " + eventType);
                return;
            }
            outbox.setStatus("SENT");
            outbox.setLastError(null);
            outbox.setNextRetryAt(null);
            outboxRepository.save(outbox);
            log.info("LES->{} 签收回写成功 eventId={} voucherId={} orderNo={}",
                    eventType, outbox.getEventId(), voucher.getId(), outbox.getPartitionKey());
        } catch (Exception ex) {
            markFailed(outbox, ex.getMessage());
        }
    }

    /**
     * 批量扫描 PENDING/FAILED 任务并发送。
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
            if ("SENT".equalsIgnoreCase(e.getStatus())) {
                continue;
            }
            trySendOne(e);
            processed++;
        }
        return processed;
    }

    /**
     * 构造签收完成事件 DTO。
     *
     * @param outbox  Outbox 任务
     * @param voucher 签收凭证
     * @return 事件 DTO
     */
    private LesSignCompletedEventDTO buildEvent(IntegrationOutboxEntity outbox, LesSignVoucherEntity voucher) {
        LesSignCompletedEventDTO event = new LesSignCompletedEventDTO();
        event.setEventId(outbox.getEventId());
        event.setTraceId(outbox.getTraceId());
        event.setEventKey(outbox.getEventType() + ":" + outbox.getRefNo());
        event.setIdempotencyKey(outbox.getIdempotencyKey());
        event.setProducer(outbox.getProducer());
        event.setEventVersion(outbox.getEventVersion());
        event.setPartitionKey(outbox.getPartitionKey());
        event.setEventTime(voucher.getCreateTime() == null ? LocalDateTime.now() : voucher.getCreateTime());

        LesSignCompletedEventDTO.SignVoucherPayload payload = new LesSignCompletedEventDTO.SignVoucherPayload();
        payload.setSignVoucherId(voucher.getId());
        payload.setPlanId(voucher.getPlanId());
        payload.setErpOrderNo(voucher.getErpOrderNo());
        payload.setCrmOrderNo(voucher.getCrmOrderNo());
        payload.setCustomerSign(voucher.getCustomerSign());
        payload.setArrivalTime(voucher.getArrivalTime());
        payload.setOnTimeStatus(voucher.getOnTimeStatus());
        payload.setStatus(voucher.getStatus());
        payload.setSignedTime(voucher.getCreateTime());
        event.setSignVoucher(payload);
        return event;
    }

    /**
     * 标记任务失败并设置退避重试时间。
     *
     * @param outbox Outbox 任务
     * @param error  错误信息
     */
    private void markFailed(IntegrationOutboxEntity outbox, String error) {
        int next = outbox.getRetryCount() == null ? 1 : outbox.getRetryCount() + 1;
        outbox.setRetryCount(next);
        outbox.setStatus("FAILED");
        outbox.setLastError(error);
        outbox.setNextRetryAt(LocalDateTime.now().plusSeconds(Math.min(600L, 5L * next)));
        outboxRepository.save(outbox);
        log.warn("LES签收回写失败 eventId={} traceId={} eventType={} refNo={} retry={} error={}",
                outbox.getEventId(), outbox.getTraceId(), outbox.getEventType(), outbox.getRefNo(), next, error);
    }
}
