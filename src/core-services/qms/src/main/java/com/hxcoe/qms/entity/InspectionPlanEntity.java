package com.hxcoe.qms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 检验计划实体
 */
@Entity
@Table(name = "qms_inspection_plan")
@Data
public class InspectionPlanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_no", nullable = false, unique = true, length = 64)
    private String planNo;

    @Column(name = "plan_name", length = 128)
    private String planName;

    @Column(name = "plan_type", length = 32)
    private String planType;

    @Column(name = "material_code", length = 64)
    private String materialCode;

    @Column(name = "material_name", length = 128)
    private String materialName;

    @Column(name = "batch_size")
    private Integer batchSize;

    @Column(name = "sampling_rule", length = 128)
    private String samplingRule;

    @Column(name = "inspection_standard_id")
    private Long inspectionStandardId;

    @Column(name = "inspection_standard_no", length = 64)
    private String standardNo;

    @Column(name = "sampling_plan", length = 128)
    private String samplingPlan;

    @Column(name = "inspection_frequency", length = 128)
    private String inspectionFrequency;

    @Column(name = "inspection_department", length = 64)
    private String inspectionDepartment;

    @Column(name = "inspection_person", length = 64)
    private String inspectionPerson;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "status", length = 16)
    private String status;

    @Column(name = "creator", length = 64)
    private String creator;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
