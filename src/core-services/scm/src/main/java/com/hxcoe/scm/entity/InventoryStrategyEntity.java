package com.hxcoe.scm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scm_inventory_strategy")
public class InventoryStrategyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String materialCode;
    private String materialName;
    
    // ABC分类
    private String abcClass;
    
    private BigDecimal safetyStock;
    private BigDecimal reorderPoint;
    private BigDecimal eoq; // 经济订货批量
    
    private BigDecimal minStock;
    private BigDecimal maxStock;
    
    private BigDecimal serviceLevelTarget;
    
    private String status;
    
    private LocalDateTime createdTime;

    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
