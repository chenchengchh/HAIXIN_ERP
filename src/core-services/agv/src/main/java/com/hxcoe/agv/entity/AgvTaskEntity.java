package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_task")
public class AgvTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false, unique = true, length = 64)
    private String taskId;

    @Column(name = "task_no", unique = true, length = 64)
    private String taskNo;

    @Column(name = "task_type", length = 32)
    private String type;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "agv_code", length = 64)
    private String agvCode;

    @Column(name = "start_point", length = 128)
    private String startPoint;

    @Column(name = "end_point", length = 128)
    private String endPoint;

    @Column(name = "payload", length = 1024)
    private String payload;

    @Column(name = "track_status", length = 64)
    private String trackStatus;

    @Column(name = "progress")
    private Integer progress;

    @Column(name = "estimated_completion")
    private LocalDateTime estimatedCompletion;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "remark", length = 512)
    private String remark;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) createTime = now;
        if (status == null || status.isBlank()) status = "pending";
        if (trackStatus == null) trackStatus = "";
        if (progress == null) progress = 0;
        if (payload == null) payload = "";
        if (agvCode == null) agvCode = "";
        if (remark == null) remark = "";
    }
}

