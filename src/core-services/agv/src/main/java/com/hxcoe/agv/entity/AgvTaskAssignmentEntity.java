package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_task_assignment")
public class AgvTaskAssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false, length = 64)
    private String taskId;

    @Column(name = "agv_code", nullable = false, length = 64)
    private String agvCode;

    @Column(name = "assigned_time")
    private LocalDateTime assignedTime;

    @Column(name = "assigned_by", length = 64)
    private String assignedBy;

    @Column(name = "assignment_status", length = 32)
    private String assignmentStatus;

    @PrePersist
    protected void onCreate() {
        if (assignedTime == null) assignedTime = LocalDateTime.now();
        if (assignedBy == null) assignedBy = "system";
        if (assignmentStatus == null || assignmentStatus.isBlank()) assignmentStatus = "assigned";
    }
}

