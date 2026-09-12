package com.hxcoe.common.dto.les;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * LES 签收完成事件 DTO（LES → ERP/CRM）。
 * <p>LES 创建签收凭证后通过 Outbox 推送，ERP/CRM 接收后回写订单状态，
 * 完成 LES 签收回流的次级闭环。</p>
 * <p>注意：跨服务事件契约固定使用小驼峰命名，通过 @JsonNaming 显式声明，
 * 避免受各服务全局 Jackson 命名策略（如 ERP 的 SNAKE_CASE）影响。</p>
 */
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class LesSignCompletedEventDTO implements Serializable {

    /** 事件 ID（UUID） */
    private String eventId;

    /** 链路追踪 ID */
    private String traceId;

    /** 事件业务键（LES_SIGN_COMPLETED:{voucherId}） */
    private String eventKey;

    /** 幂等键（与 eventKey 一致，消费端据此去重） */
    private String idempotencyKey;

    /** 生产者（les-service） */
    private String producer;

    /** 事件版本 */
    private Integer eventVersion;

    /** 分区键（按 erpOrderNo 或 planId） */
    private String partitionKey;

    /** 事件发生时间 */
    private LocalDateTime eventTime;

    /** 签收凭证载荷 */
    private SignVoucherPayload signVoucher;

    /**
     * 签收凭证载荷。
     */
    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class SignVoucherPayload implements Serializable {
        /** 签收凭证 ID */
        private Long signVoucherId;
        /** 配送计划 ID */
        private Long planId;
        /** ERP 关联订单号（业务键） */
        private String erpOrderNo;
        /** CRM 关联订单号（业务键） */
        private String crmOrderNo;
        /** 客户签收人 */
        private String customerSign;
        /** 到货时间 */
        private LocalDateTime arrivalTime;
        /** 准时状态（1-准时, 2-提前, 3-延误） */
        private Integer onTimeStatus;
        /** 凭证状态（signed/exception） */
        private String status;
        /** 签收时间 */
        private LocalDateTime signedTime;
    }
}
