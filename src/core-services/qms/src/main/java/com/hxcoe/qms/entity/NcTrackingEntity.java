package com.hxcoe.qms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 不合格品追踪实体
 */
@Entity
@Table(name = "qms_nc_tracking")
@Data
public class NcTrackingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "disposal_id")
    private Long disposalId;

    @Column(name = "registration_id")
    private Long registrationId;

    @Column(name = "registration_no", length = 64)
    private String registrationNo;

    @Column(name = "disposal_plan", length = 32)
    private String disposalPlan;

    @Column(name = "disposal_status", length = 16)
    private String disposalStatus;

    @Column(name = "tracking_content", length = 1000)
    private String trackingContent;

    @Column(name = "tracking_status", length = 16)
    private String trackingStatus;

    @Column(name = "tracker", length = 64)
    private String tracker;

    @Column(name = "tracking_time")
    private LocalDateTime trackingTime;

    @Column(name = "effectiveness", length = 16)
    private String effectiveness;

    @Column(name = "improvement_suggestions", length = 1000)
    private String improvementSuggestions;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
