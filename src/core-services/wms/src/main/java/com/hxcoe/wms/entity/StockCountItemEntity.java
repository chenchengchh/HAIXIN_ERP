package com.hxcoe.wms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_stock_count_item")
public class StockCountItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    @JsonIgnore
    private StockCountJobEntity job;

    private String locationCode;
    private String materialCode;
    private String materialName;
    private String batchNo;
    private String unit;

    private BigDecimal sysQty;
    private BigDecimal countQty;
    private BigDecimal diffQty;

    private LocalDateTime scanTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdTime = now;
        updatedTime = now;
        if (sysQty == null) sysQty = BigDecimal.ZERO;
        if (countQty == null) countQty = BigDecimal.ZERO;
        if (diffQty == null) diffQty = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

