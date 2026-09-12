package com.hxcoe.oa.integration.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HrDepartmentDTO {

    private Long id;
    private String name;
    private String departmentCode;
    private String description;
    private Long parentId;
    private String parentName;
    private Long managerId;
    private String managerName;
    private Integer level;
    private Integer sort;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
