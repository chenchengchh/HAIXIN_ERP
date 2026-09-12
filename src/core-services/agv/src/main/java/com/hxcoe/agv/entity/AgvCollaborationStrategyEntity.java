package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_collaboration_strategy")
public class AgvCollaborationStrategyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "strategy_id", nullable = false, unique = true, length = 64)
    private String strategyId;

    @Column(name = "strategy_name", length = 128)
    private String strategyName;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "strategy_config_json", length = 2048)
    private String strategyConfigJson;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) createTime = now;
        updateTime = now;
        if (strategyName == null) strategyName = "";
        if (description == null) description = "";
        if (strategyConfigJson == null) strategyConfigJson = "{}";
        if (status == null || status.isBlank()) status = "inactive";
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

