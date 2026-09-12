package com.hxcoe.qms.dto.inspection;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检验标准DTO
 */
@Data
public class InspectionStandardDTO {

    private Long id;

    private String standardNo;

    private String materialCode;

    private String materialName;

    private String version;

    private String aqlLevel;

    private String status;

    private List<InspectionItemDTO> inspectionItems;

    private String creator;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
