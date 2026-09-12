package com.hxcoe.srm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "srm_supplier_credit", uniqueConstraints = {@UniqueConstraint(columnNames = {"supplier_id"})})
public class SupplierCreditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    private String supplierName;

    private Integer creditScore;
    private String creditLevel;
    private String riskLevel;

    private LocalDate evaluationDate;
    private LocalDate nextEvaluationDate;

    @Column(columnDefinition = "TEXT")
    private String evaluationComment;

    @Column(columnDefinition = "TEXT")
    private String riskDescription;

    @Column(columnDefinition = "TEXT")
    private String improvementSuggestion;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

