package com.hxcoe.qms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 不合格品登记实体
 */
@Entity
@Table(name = "qms_nc_registration")
@Data
public class NcRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_no", nullable = false, unique = true, length = 64)
    private String registrationNo;

    @Column(name = "material_code", length = 64)
    private String materialCode;

    @Column(name = "material_name", length = 128)
    private String materialName;

    @Column(name = "batch_no", length = 64)
    private String batchNo;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "defect_type", length = 32)
    private String defectType;

    @Column(name = "defect_level", length = 16)
    private String defectLevel;

    @Column(name = "defect_description", length = 1000)
    private String defectDescription;

    @Column(name = "discovery_department", length = 64)
    private String discoveryDepartment;

    @Column(name = "discovery_person", length = 64)
    private String discoveryPerson;

    @Column(name = "discovery_time")
    private LocalDateTime discoveryTime;

    @Column(name = "discovery_location", length = 128)
    private String discoveryLocation;

    @Column(name = "status", length = 16)
    private String status;

    @Column(name = "registrant", length = 64)
    private String registrant;

    @Column(name = "registration_time")
    private LocalDateTime registrationTime;

    @Column(name = "review_status", length = 32)
    private String reviewStatus;

    @Column(name = "disposal_status", length = 32)
    private String disposalStatus;

    @Column(name = "tracking_status", length = 32)
    private String trackingStatus;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
