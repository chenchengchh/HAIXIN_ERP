package com.hxcoe.common.dto.scm;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SCM 接收 MES 完工事实请求 DTO。
 *
 * <p>对应 SCM 接口 POST /api/v1/scm/integration/mes/completion。
 * SCM 收到后落 scm_production_completion_fact 表，并可选推送 ERP 闭环生产成本。</p>
 * <p>注意：跨服务事件契约固定使用小驼峰命名，通过 @JsonNaming 显式声明，
 * 避免受各服务全局 Jackson 命名策略影响。</p>
 */
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class MesCompletionFactDTO {

    /** 事件ID（幂等用） */
    private String eventId;

    /** 事件键 */
    private String eventKey;

    /** 幂等键 */
    private String idempotencyKey;

    /** ERP 生产单号（跨服务业务键） */
    private String erpProductionNo;

    /** MES 工单号 */
    private String workOrderNo;

    /** 产品编码 */
    private String productCode;

    /** 产品名称 */
    private String productName;

    /** 计划数量 */
    private BigDecimal planQuantity;

    /** 实际完工数量 */
    private BigDecimal completedQuantity;

    /** 完工时间 */
    private LocalDateTime completedTime;

    /** 来源系统（mes-service） */
    private String sourceSystem;
}
