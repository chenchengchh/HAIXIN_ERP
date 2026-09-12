package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 绩效评估实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_performance_appraisal")
public class PerformanceAppraisalEntity {

    /**
     * 绩效评估ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工ID
     */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private EmployeeEntity employee;

    /**
     * 评估周期
     */
    @Column(name = "appraisal_period", length = 20)
    private String appraisalPeriod;

    /**
     * 目标完成分数
     */
    @Column(name = "objective_score")
    private Double objectiveScore;

    /**
     * 能力分数
     */
    @Column(name = "competency_score")
    private Double competencyScore;

    /**
     * 总分
     */
    @Column(name = "total_score")
    private Double totalScore;

    /**
     * 评估状态
     */
    @Column(name = "appraisal_status", length = 20)
    private String appraisalStatus;

    /**
     * 评估人ID
     */
    @ManyToOne
    @JoinColumn(name = "appraiser_id")
    private EmployeeEntity appraiser;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 自动设置创建时间
     */
    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
    }

    /**
     * 自动更新时间
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
}