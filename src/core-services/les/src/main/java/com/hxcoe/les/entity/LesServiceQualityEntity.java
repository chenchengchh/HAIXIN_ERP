package com.hxcoe.les.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "les_service_quality")
@Data
public class LesServiceQualityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "sign_success_rate")
    private Double signSuccessRate;

    @Column(name = "cargo_integrity_rate")
    private Double cargoIntegrityRate;

    @Column(name = "on_time_rate")
    private Double onTimeRate;

    @Column(name = "customer_satisfaction")
    private Double customerSatisfaction;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
