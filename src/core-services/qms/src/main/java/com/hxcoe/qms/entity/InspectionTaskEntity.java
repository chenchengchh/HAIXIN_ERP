package com.hxcoe.qms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 检验任务实体
 */
@Entity
@Table(name = "qms_inspection_task")
@Data
public class InspectionTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_no", nullable = false, unique = true, length = 64)
    private String taskNo;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "plan_name", length = 128)
    private String planName;

    @Column(name = "material_code", length = 64)
    private String materialCode;

    @Column(name = "material_name", length = 128)
    private String materialName;

    @Column(name = "batch_no", length = 64)
    private String batchNo;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "task_type", length = 32)
    private String taskType;

    @Column(name = "assignee", length = 64)
    private String assignee;

    @Column(name = "assign_time")
    private LocalDateTime assignTime;

    @Column(name = "due_time")
    private LocalDateTime dueTime;

    @Column(name = "status", length = 16)
    private String status;

    @Column(name = "inspection_result", length = 16)
    private String inspectionResult;

    @Column(name = "task_name", length = 128)
    private String taskName;

    @Column(name = "plan_no", length = 64)
    private String planNo;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "actual_finish_time")
    private LocalDateTime actualFinishTime;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
