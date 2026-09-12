package com.hxcoe.common.dto.mes;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MES→ERP/SCM 工单完工事件 DTO。
 *
 * <p>MES 工单完工时通过 Outbox 异步推送，ERP 收到后回写生产单状态，
 * SCM 收到后落完工事实表，闭环生产链。</p>
 * <p>注意：跨服务事件契约固定使用小驼峰命名，通过 @JsonNaming 显式声明，
 * 避免受各服务全局 Jackson 命名策略（如 ERP 的 SNAKE_CASE）影响。</p>
 */
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class WorkOrderCompletionEventDTO {

    /** 事件ID（UUID） */
    private String eventId;

    /** 链路ID */
    private String traceId;

    /** 事件类型（如 workorder.completed.v1） */
    private String eventType;

    /** 事件键 */
    private String eventKey;

    /** 幂等键 */
    private String idempotencyKey;

    /** 分区键（按 erpProductionNo） */
    private String partitionKey;

    /** 事件版本 */
    private Integer eventVersion;

    /** 生产者（mes-service） */
    private String producer;

    /** 事件时间 */
    private LocalDateTime eventTime;

    /** 完工载荷 */
    private CompletionPayload completion;

    /**
     * 完工载荷内部类。
     */
    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class CompletionPayload {

        /** 工单号 */
        private String workOrderNo;

        /** ERP 生产单号（跨服务业务键） */
        private String erpProductionNo;

        /** 产品编码 */
        private String productCode;

        /** 产品名称 */
        private String productName;

        /** 计划数量 */
        private BigDecimal planQuantity;

        /** 实际完工数量 */
        private BigDecimal actualQuantity;

        /** 工单状态（COMPLETED） */
        private String status;

        /** 完工时间 */
        private LocalDateTime completedTime;
    }
}
