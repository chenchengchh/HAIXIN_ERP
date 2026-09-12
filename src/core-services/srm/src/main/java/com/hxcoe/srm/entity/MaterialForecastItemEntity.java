package com.hxcoe.srm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "srm_material_forecast_item")
public class MaterialForecastItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long materialId;
    private String materialName;
    private String specification;

    private Integer quantity;
    private String unit;

    private LocalDateTime expectedDeliveryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forecast_id")
    @JsonIgnore
    private MaterialForecastEntity forecast;
}
