package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_path_plan")
public class AgvPathPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false, unique = true, length = 64)
    private String planId;

    @Column(name = "task_id", length = 64)
    private String taskId;

    @Column(name = "agv_code", length = 64)
    private String agvCode;

    @Column(name = "start_point", length = 128)
    private String startPoint;

    @Column(name = "end_point", length = 128)
    private String endPoint;

    @Column(name = "distance", precision = 19, scale = 4)
    private BigDecimal distance;

    @Column(name = "estimated_time", precision = 19, scale = 4)
    private BigDecimal estimatedTime;

    @Column(name = "algorithm", length = 64)
    private String algorithm;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) createTime = LocalDateTime.now();
        if (status == null || status.isBlank()) status = "planned";
        if (algorithm == null) algorithm = "";
        if (distance == null) distance = BigDecimal.ZERO;
        if (estimatedTime == null) estimatedTime = BigDecimal.ZERO;
        if (taskId == null) taskId = "";
        if (agvCode == null) agvCode = "";
        if (startPoint == null) startPoint = "";
        if (endPoint == null) endPoint = "";
    }
}

