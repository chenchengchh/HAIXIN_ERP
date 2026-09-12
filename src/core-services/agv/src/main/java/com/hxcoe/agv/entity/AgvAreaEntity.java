package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_area")
public class AgvAreaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_id", nullable = false, unique = true, length = 64)
    private String areaId;

    @Column(name = "area_name", length = 128)
    private String areaName;

    @Column(name = "area_type", length = 64)
    private String areaType;

    @Column(name = "polygon_json", length = 4096)
    private String polygonJson;

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
        if (areaName == null) areaName = "";
        if (areaType == null) areaType = "";
        if (polygonJson == null) polygonJson = "{}";
        if (status == null || status.isBlank()) status = "active";
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

