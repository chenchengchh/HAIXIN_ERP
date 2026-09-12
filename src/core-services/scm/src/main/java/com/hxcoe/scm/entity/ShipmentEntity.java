package com.hxcoe.scm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scm_shipment")
public class ShipmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shipmentNo;

    private String relatedType;

    private Long relatedId;

    private String relatedNo;

    private Long supplierId;

    private String supplierName;

    private String origin;

    private String destination;

    private String transportMode;

    private LocalDateTime eta;

    private LocalDateTime arrivedTime;

    private String status;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (status == null || status.isBlank()) {
            status = "CREATED";
        }
        if (shipmentNo == null || shipmentNo.isBlank()) {
            shipmentNo = "SHP-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
