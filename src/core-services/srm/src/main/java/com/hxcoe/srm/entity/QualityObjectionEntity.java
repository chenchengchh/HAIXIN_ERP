package com.hxcoe.srm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "srm_quality_objection")
public class QualityObjectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "objection_no", unique = true, nullable = false)
    private String objectionNo;

    private Long supplierId;
    private String supplierName;

    private Long orderId;
    private String orderNo;

    private LocalDateTime objectionDate;

    private Long materialId;
    private String materialName;

    private String objectionType;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer quantity;

    private BigDecimal lossAmount;

    @Column(columnDefinition = "TEXT")
    private String processingResult;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String remark;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (status == null || status.isBlank()) status = "PENDING";
        if (lossAmount == null) lossAmount = BigDecimal.ZERO;
        if (quantity == null) quantity = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

