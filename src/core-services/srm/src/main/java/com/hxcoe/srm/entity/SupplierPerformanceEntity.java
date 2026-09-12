package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "srm_supplier_performance")
public class SupplierPerformanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long supplierId;
    
    // 2023-Q3, 2023-10
    private String period;
    
    private BigDecimal qualityScore;
    private BigDecimal deliveryScore;
    private BigDecimal priceScore;
    private BigDecimal serviceScore;
    
    private BigDecimal totalScore;

    // A, B, C, D
    private String level;

    /**
     * 累计IQC检验批次数（QMS回传累计）
     */
    private Integer totalInspections;

    /**
     * 累计IQC合格批次数（QMS回传累计）
     */
    private Integer passedInspections;

    /**
     * IQC检验合格率（%），由累计合格批次/累计检验批次重算
     */
    private BigDecimal passRate;
    
    private String evaluator;
    private LocalDateTime evaluateTime;

    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}
