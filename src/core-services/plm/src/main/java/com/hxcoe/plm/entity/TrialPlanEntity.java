package com.hxcoe.plm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plm_trial_plan")
@Data
public class TrialPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "plan_code", length = 64, unique = true)
    private String planCode;

    @Column(name = "plan_name", nullable = false, length = 128)
    private String planName;

    @Column(name = "product_code", length = 64)
    private String productCode;

    @Column(name = "product_name", length = 128)
    private String productName;

    @Column(name = "version", length = 32)
    private String version;

    @Column(name = "trial_type", length = 64)
    private String trialType;

    @Column(name = "trial_qty")
    private Integer trialQty;

    @Lob
    @Column(name = "departments_json", columnDefinition = "TEXT")
    private String departmentsJson;

    @Lob
    @Column(name = "stages_json", columnDefinition = "TEXT")
    private String stagesJson;

    @Column(name = "responsible_person", length = 64)
    private String responsiblePerson;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
