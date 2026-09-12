package com.hxcoe.scm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scm_demand_forecast")
public class DemandForecastEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productCode;
    private String productName;
    private String region;
    
    // YYYY-MM
    private String period;

    private Long versionId;

    private Integer versionNo;
    
    private String historySales; // JSON string for history
    
    private BigDecimal baselineForecast;
    private BigDecimal promotionAdjustment;
    private BigDecimal seasonalAdjustment;
    private BigDecimal manualAdjustment;
    
    private BigDecimal finalForecast;
    
    private String status; // DRAFT, APPROVED

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
