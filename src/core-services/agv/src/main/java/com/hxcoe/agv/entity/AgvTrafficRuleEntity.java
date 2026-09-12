package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_traffic_rule")
public class AgvTrafficRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 128)
    private String ruleName;

    @Column(name = "rule_type", length = 64)
    private String ruleType;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "condition_json", length = 2048)
    private String conditionJson;

    @Column(name = "action_json", length = 2048)
    private String actionJson;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createTime == null) createTime = now;
        updateTime = now;
        if (enabled == null) enabled = Boolean.TRUE;
        if (priority == null) priority = 1;
        if (ruleType == null) ruleType = "";
        if (conditionJson == null) conditionJson = "{}";
        if (actionJson == null) actionJson = "{}";
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

