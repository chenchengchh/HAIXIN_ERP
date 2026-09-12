package com.hxcoe.common.dto.mes;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ERP→MES 创建工单请求 DTO（强类型契约）。
 *
 * <p>用于 ERP 生产单创建后通过 Outbox 异步事件触发 MES 工单创建。
 * 字段采用小驼峰命名，与前端约定一致；序列化后通过 Feign/Rabbit/Redis 传输。</p>
 * <p>注意：通过 @JsonNaming 显式锁定小驼峰命名，
 * 避免受各服务全局 Jackson 命名策略（如 ERP 的 SNAKE_CASE）影响。</p>
 */
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class WorkOrderCreateRequestDTO {

    /** 事件ID（UUID，用于链路追踪） */
    private String eventId;

    /** 链路ID */
    private String traceId;

    /** 事件类型（如 production.created.v1） */
    private String eventType;

    /** 事件键（业务幂等键） */
    private String eventKey;

    /** 幂等键（与 eventKey 一致，Outbox 表用） */
    private String idempotencyKey;

    /** 分区键（按 erpProductionNo） */
    private String partitionKey;

    /** 事件版本 */
    private Integer eventVersion;

    /** 生产者（erp-service） */
    private String producer;

    /** 事件时间 */
    private LocalDateTime eventTime;

    /** 工单载荷 */
    private WorkOrderPayload workOrder;

    /**
     * 工单载荷内部类。
     */
    @Data
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public static class WorkOrderPayload {

        /** ERP 生产单号（跨服务业务键，MES 侧落 erp_production_no 字段） */
        private String erpProductionNo;

        /** 工单号（MES 侧生成，如 WO-{erpProductionNo}；为空时由 MES 生成） */
        private String workOrderNo;

        /** 产品编码 */
        private String productCode;

        /** 产品名称 */
        private String productName;

        /** 计划数量 */
        private BigDecimal planQuantity;

        /** 车间 */
        private String workshop;

        /** 生产线 */
        private String productionLine;

        /** 计划开始时间 */
        private LocalDateTime planStartTime;

        /** 计划结束时间 */
        private LocalDateTime planEndTime;

        /** 来源系统（erp-service） */
        private String sourceSystem;
    }
}
