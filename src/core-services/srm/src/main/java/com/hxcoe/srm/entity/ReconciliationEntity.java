package com.hxcoe.srm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "srm_reconciliation")
public class ReconciliationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reconciliation_no", unique = true, nullable = false)
    private String reconciliationNo;

    private Long supplierId;
    private String supplierName;

    private String reconciliationPeriod;

    private BigDecimal totalAmount;

    private LocalDate startDate;
    private LocalDate endDate;

    private String status;

    private String confirmStatus;

    @Column(columnDefinition = "TEXT")
    private String remark;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (status == null || status.isBlank()) status = "PENDING";
        if (confirmStatus == null || confirmStatus.isBlank()) confirmStatus = "PENDING";
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

