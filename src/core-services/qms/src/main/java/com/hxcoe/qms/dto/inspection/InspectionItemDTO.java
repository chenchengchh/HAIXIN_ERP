package com.hxcoe.qms.dto.inspection;

import lombok.Data;

/**
 * 检验项目DTO
 */
@Data
public class InspectionItemDTO {

    private Long id;

    private String itemName;

    private String itemType;

    private String specification;

    private String tolerance;

    private String testMethod;

    private String testEquipment;
}
