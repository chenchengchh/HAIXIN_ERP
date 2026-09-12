package com.hxcoe.srm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "srm_material_forecast")
public class MaterialForecastEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "forecast_no", unique = true, nullable = false)
    private String forecastNo;

    private Long supplierId;
    private String supplierName;

    private Long orderId;
    private String orderNo;

    private LocalDateTime expectedArrivalDate;
    private LocalDateTime actualArrivalDate;

    private Integer totalQuantity;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String logisticsInfo;

    @Column(columnDefinition = "TEXT")
    private String remark;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @OneToMany(mappedBy = "forecast", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialForecastItemEntity> items;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (status == null || status.isBlank()) {
            status = "PENDING";
        }
        if (totalQuantity == null) {
            totalQuantity = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

