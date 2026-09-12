package com.hxcoe.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_location_type")
public class LocationTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String typeCode;

    @Column(length = 128)
    private String typeName;

    @Column(length = 512)
    private String typeDesc;

    private BigDecimal maxWeight;

    private Boolean mixFlag;

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
        if (mixFlag == null) {
            mixFlag = Boolean.TRUE;
        }
        if (maxWeight == null) {
            maxWeight = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

