package com.hxcoe.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_location")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String warehouseCode;

    @Column(length = 64)
    private String zoneCode;

    @Column(unique = true, nullable = false, length = 64)
    private String locationCode;

    @Column(length = 128)
    private String locationName;

    @Column(length = 64)
    private String locationTypeCode;

    @Column(length = 8)
    private String status;

    @Column(length = 512)
    private String remark;

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
        if (remark == null) {
            remark = "";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

