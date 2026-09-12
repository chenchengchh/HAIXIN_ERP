package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_fault_alert")
public class AgvFaultAlertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_level", length = 32)
    private String alertLevel;

    @Column(name = "alert_type", length = 64)
    private String alertType;

    @Column(name = "agv_code", length = 64)
    private String agvCode;

    @Column(name = "title", length = 256)
    private String title;

    @Column(name = "message", length = 1024)
    private String message;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    @Column(name = "handle_result", length = 1024)
    private String handleResult;

    @Column(name = "operator", length = 64)
    private String operator;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) createTime = LocalDateTime.now();
        if (status == null || status.isBlank()) status = "open";
        if (alertLevel == null) alertLevel = "info";
        if (alertType == null) alertType = "";
        if (agvCode == null) agvCode = "";
        if (title == null) title = "";
        if (message == null) message = "";
        if (handleResult == null) handleResult = "";
        if (operator == null) operator = "";
    }
}

