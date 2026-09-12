package com.hxcoe.hr.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 岗位DTO
 */
@Data
public class PositionDTO {
    private Long id;
    private String name;
    private String positionCode;
    private String level;
    private String description;
    private String status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String createdBy;
    private String updatedBy;
    private String remark;
    private Long departmentId;
}