package com.hxcoe.plm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plm_task")
@Data
public class ProjectTaskEntity {
    @Id
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "task_name", nullable = false, length = 128)
    private String taskName;

    @Column(name = "assignee", length = 64)
    private String assignee;

    @Column(name = "progress")
    private Integer progress;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "task_type", length = 32)
    private String taskType;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
