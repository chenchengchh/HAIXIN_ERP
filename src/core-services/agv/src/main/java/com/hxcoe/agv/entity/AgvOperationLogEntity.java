package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_operation_log")
public class AgvOperationLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_type", length = 64)
    private String logType;

    @Column(name = "agv_code", length = 64)
    private String agvCode;

    @Column(name = "ref_id", length = 64)
    private String refId;

    @Column(name = "content", length = 2048)
    private String content;

    @Column(name = "operator", length = 64)
    private String operator;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) createTime = LocalDateTime.now();
        if (logType == null) logType = "";
        if (agvCode == null) agvCode = "";
        if (refId == null) refId = "";
        if (content == null) content = "";
        if (operator == null) operator = "system";
    }
}

