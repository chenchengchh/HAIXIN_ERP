package com.hxcoe.qms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 质量异常处置实体
 */
@Entity
@Table(name = "qms_anomaly_disposal")
@Data
public class AnomalyDisposalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "report_no", length = 64)
    private String reportNo;

    @Column(name = "disposal_plan", length = 2000)
    private String disposalPlan;

    @Column(name = "responsible_person", length = 64)
    private String responsiblePerson;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "actual_completion_date")
    private LocalDate actualCompletionDate;

    @Column(name = "disposal_status", length = 16)
    private String disposalStatus;

    @Column(name = "disposal_result", length = 2000)
    private String disposalResult;

    @Column(name = "verifier", length = 64)
    private String verifier;

    @Column(name = "verification_time")
    private LocalDateTime verificationTime;

    @Column(name = "verification_result", length = 16)
    private String verificationResult;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
