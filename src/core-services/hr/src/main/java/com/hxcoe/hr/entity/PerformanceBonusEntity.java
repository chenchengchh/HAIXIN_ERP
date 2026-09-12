package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 绩效奖金实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_performance_bonus")
public class PerformanceBonusEntity {

    /**
     * 绩效奖金ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工ID
     */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /**
     * 考核周期（如2023年度）
     */
    @Column(name = "appraisal_period", length = 20)
    private String appraisalPeriod;

    /**
     * 绩效得分
     */
    @Column(name = "performance_score")
    private BigDecimal performanceScore;

    /**
     * 绩效等级（A/B/C/D）
     */
    @Column(name = "performance_level", length = 2)
    private String performanceLevel;

    /**
     * 奖金金额
     */
    @Column(name = "bonus_amount")
    private BigDecimal bonusAmount;

    /**
     * 状态：0待审批 1已批准 2已发放
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 评价人ID
     */
    @Column(name = "evaluator_id")
    private Long evaluatorId;

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
