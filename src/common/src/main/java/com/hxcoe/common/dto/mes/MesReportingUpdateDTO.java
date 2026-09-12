package com.hxcoe.common.dto.mes;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ERP→MES 更新报工请求 DTO。
 *
 * <p>对应 MES 接口 PUT /api/v1/mes/reporting/{id}。</p>
 * <p>注意：跨服务契约固定使用小驼峰命名，通过 @JsonNaming 显式声明，
 * 避免受各服务全局 Jackson 命名策略影响。</p>
 */
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class MesReportingUpdateDTO {

    /** 报工单号 */
    private String reportNo;

    /** 工单号 */
    private String workOrderNo;

    /** 操作员姓名 */
    private String operatorName;

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
