package com.hxcoe.common.dto.mes;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ERP→MES 创建报工请求 DTO。
 *
 * <p>对应 MES 接口 POST /api/v1/mes/reporting/manual。</p>
 * <p>注意：跨服务契约固定使用小驼峰命名，通过 @JsonNaming 显式声明，
 * 避免受各服务全局 Jackson 命名策略影响。</p>
 */
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class MesReportingCreateDTO {

    /** 报工单号（为空时由 MES 生成） */
    private String reportNo;

    /** 工单号（必填） */
    private String workOrderNo;

    /** ERP 生产单号（跨服务关联键） */
    private String erpProductionNo;

    /** 工序名称 */
    private String stepName;

    /** 工位名称 */
    private String workstationName;

    /** 操作员姓名 */
    private String operatorName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 合格数量 */
    private Integer goodQty;

    /** 报废数量 */
    private Integer scrapQty;

    /** 返工数量 */
    private Integer reworkQty;

    /** 工时 */
    private Double workingHours;

    /** 机时 */
    private Double machineHours;

    /** 状态 */
    private String status;

    /** 备注 */
    private String remark;
}
