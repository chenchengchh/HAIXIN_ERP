package com.hxcoe.hr.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门DTO
 */
@Data
public class DepartmentDTO {
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