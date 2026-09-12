package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 绩效目标实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_performance_objective")
public class PerformanceObjectiveEntity {

    /**
     * 绩效目标ID
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
     * 目标内容
     */
    @Column(name = "objective_content", length = 500)
    private String objectiveContent;

    /**
     * 目标值
     */
    @Column(name = "target_value", length = 200)
    private String targetValue;

    /**
     * 权重
     */
    @Column(name = "weight")
    private Double weight;

    /**
     * 开始日期
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * 状态
     */
    @Column(name = "status", length = 20)
    private String status;

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