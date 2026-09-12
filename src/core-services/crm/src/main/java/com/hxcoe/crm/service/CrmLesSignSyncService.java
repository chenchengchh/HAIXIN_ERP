package com.hxcoe.crm.service;

import com.hxcoe.common.dto.les.LesSignCompletedEventDTO;
import com.hxcoe.crm.entity.SalesOrderEntity;
import com.hxcoe.crm.repository.SalesOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRM 侧 LES 签收回写处理服务。
 * <p>接收 LES 签收完成事件，按 crmOrderNo 更新销售订单状态为已签收（SIGNED），
 * 完成 LES→CRM 签收回流闭环。</p>
 */
@Slf4j
@Service
public class CrmLesSignSyncService {

    /** 签收后销售订单状态 */
    private static final String ORDER_STATUS_SIGNED = "SIGNED";

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    /**
     * 应用 LES 签收完成事件，更新销售订单状态。
     * <p>幂等：订单已为 SIGNED 状态时跳过；订单不存在时返回 false。</p>
     *
     * @param event LES 签收完成事件
     * @return true=处理成功，false=订单不存在
     */
    @Transactional
    public boolean applyLesSignCompleted(LesSignCompletedEventDTO event) {
        if (event == null || event.getSignVoucher() == null) {
            return false;
        }
        LesSignCompletedEventDTO.SignVoucherPayload payload = event.getSignVoucher();
        String crmOrderNo = payload.getCrmOrderNo();
        if (crmOrderNo == null || crmOrderNo.isBlank()) {
            log.warn("LES签收回流 CRM：crmOrderNo为空，跳过 eventId={}", event.getEventId());
            return false;
        }

        SalesOrderEntity order = salesOrderRepository.findByOrderNo(crmOrderNo);
        if (order == null) {
            log.warn("LES签收回流 CRM：销售订单不存在 crmOrderNo={} eventId={}", crmOrderNo, event.getEventId());
            return false;
        }

        // 幂等：已签收则跳过
        if (ORDER_STATUS_SIGNED.equalsIgnoreCase(order.getStatus())) {
            log.info("LES签收回流 CRM 命中幂等，订单已签收 crmOrderNo={} eventId={}", crmOrderNo, event.getEventId());
            return true;
        }

        order.setStatus(ORDER_STATUS_SIGNED);
        salesOrderRepository.save(order);
        log.info("LES签收回流 CRM 成功 crmOrderNo={} voucherId={} eventId={} onTimeStatus={}",
                crmOrderNo, payload.getSignVoucherId(), event.getEventId(), payload.getOnTimeStatus());
        return true;
    }
}
