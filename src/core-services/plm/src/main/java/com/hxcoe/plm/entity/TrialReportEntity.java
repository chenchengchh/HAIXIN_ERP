package com.hxcoe.plm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plm_trial_report")
@Data
public class TrialReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "report_code", length = 64, unique = true)
    private String reportCode;

    @Column(name = "report_title", nullable = false, length = 256)
    private String reportTitle;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "trial_qty")
    private Integer trialQty;

    @Column(name = "pass_qty")
    private Integer passQty;

    @Column(name = "yield_rate")
    private Double yieldRate;

    @Column(name = "create_user", length = 64)
    private String createUser;

    @Column(name = "create_time")
    private LocalDate createTime;

    @Column(name = "approve_user", length = 64)
    private String approveUser;

    @Column(name = "approve_time")
    private LocalDate approveTime;

    @Lob
    @Column(name = "main_issues_json", columnDefinition = "TEXT")
    private String mainIssuesJson;

    @Lob
    @Column(name = "improvements_json", columnDefinition = "TEXT")
    private String improvementsJson;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
