package com.hxcoe.eam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eam_asset_kpi", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"asset_id", "record_month"})
})
public class AssetKpiEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Column(name = "record_month", nullable = false, length = 7)
    private String recordMonth; // YYYY-MM

    @Column(name = "oee_value")
    private BigDecimal oeeValue;

    @Column(name = "mtbf_hours")
    private BigDecimal mtbfHours;

    @Column(name = "mttr_hours")
    private BigDecimal mttrHours;

    @Column(name = "maintenance_cost")
    private BigDecimal maintenanceCost;

    @Column(name = "downtime_minutes")
    private Integer downtimeMinutes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
