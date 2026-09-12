package com.hxcoe.qms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 不合格品处理实体
 */
@Entity
@Table(name = "qms_nc_disposal")
@Data
public class NcDisposalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "registration_id")
    private Long registrationId;

    @Column(name = "registration_no", length = 64)
    private String registrationNo;

    @Column(name = "disposal_plan", length = 32)
    private String disposalPlan;

    @Column(name = "disposal_description", length = 1000)
    private String disposalDescription;

    @Column(name = "handler", length = 64)
    private String handler;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "disposal_status", length = 16)
    private String disposalStatus;

    @Column(name = "process_result", length = 1000)
    private String processResult;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
