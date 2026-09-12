package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "srm_score_card")
public class ScoreCardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long supplierId;
    private String supplierName;
    
    // YYYY-MM or YYYY-Q1
    private String period;
    
    private BigDecimal qualityScore;    // 质量得分
    private BigDecimal deliveryScore;   // 交期得分
    private BigDecimal costScore;       // 成本得分
    private BigDecimal serviceScore;    // 服务得分
    
    private BigDecimal totalScore;      // 总分
    
    private String grade;               // A, B, C, D
    
    private String comments;

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
