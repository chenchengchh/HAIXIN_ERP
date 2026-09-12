package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_collaboration_log")
public class AgvCollaborationLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_code", length = 64)
    private String logCode;

    @Column(name = "strategy_id", length = 64)
    private String strategyId;

    @Column(name = "agv_code", length = 64)
    private String agvCode;

    @Column(name = "content", length = 2048)
    private String content;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) createTime = LocalDateTime.now();
        if (logCode == null) logCode = "";
        if (strategyId == null) strategyId = "";
        if (agvCode == null) agvCode = "";
        if (content == null) content = "";
    }
}

