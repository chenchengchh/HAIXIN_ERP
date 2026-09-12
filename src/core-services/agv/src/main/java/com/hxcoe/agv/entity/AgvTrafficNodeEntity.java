package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_traffic_node")
public class AgvTrafficNodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_code", nullable = false, unique = true, length = 128)
    private String nodeCode;

    @Column(name = "node_name", length = 128)
    private String nodeName;

    @Column(name = "area_code", length = 64)
    private String areaCode;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "locked_by_agv_code", length = 64)
    private String lockedByAgvCode;

    @Column(name = "locked_time")
    private LocalDateTime lockedTime;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdTime == null) createdTime = now;
        updatedTime = now;
        if (status == null || status.isBlank()) status = "normal";
        if (nodeName == null) nodeName = nodeCode;
        if (areaCode == null) areaCode = "";
        if (lockedByAgvCode == null) lockedByAgvCode = "";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

