package com.hxcoe.common.dto.mes;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 工单状态更新请求 DTO。
 *
 * <p>用于 ERP 或外部系统更新 MES 工单状态，替代原先的弱类型 Object body。</p>
 * <p>注意：跨服务契约固定使用小驼峰命名，通过 @JsonNaming 显式声明，
 * 避免受各服务全局 Jackson 命名策略影响。</p>
 */
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class WorkOrderStatusUpdateDTO {

    /** 目标状态（CREATED/STARTED/PAUSED/COMPLETED/CANCELLED） */
    private String status;

    /** 实际数量（完工时回填） */
    private BigDecimal actualQuantity;

    /** 操作员姓名 */
    private String operatorName;

    /** 备注 */
    private String remark;
}
