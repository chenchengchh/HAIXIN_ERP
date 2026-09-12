package com.hxcoe.qms.dto.inspection;

import lombok.Data;

/**
 * 检验结果项目DTO
 */
@Data
public class InspectionResultItemDTO {

    private String itemId;

    private String itemName;

    private String specification;

    private String actualValue;

    private String result;

    private String remark;
}
