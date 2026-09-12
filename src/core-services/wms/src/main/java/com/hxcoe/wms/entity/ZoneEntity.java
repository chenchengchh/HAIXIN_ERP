package com.hxcoe.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_zone")
public class ZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String warehouseCode;

    @Column(unique = true, nullable = false, length = 64)
    private String zoneCode;

    @Column(length = 128)
    private String zoneName;

    @Column(length = 16)
    private String zoneType;

    @Column(length = 512)
    private String description;

    @Column(length = 8)
    private String status;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdTime = now;
        updatedTime = now;
        if (status == null || status.isBlank()) {
            status = "1";
        }
        if (zoneType == null || zoneType.isBlank()) {
            zoneType = "1";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

