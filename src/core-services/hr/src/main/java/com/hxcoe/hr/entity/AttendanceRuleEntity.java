package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 考勤规则实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_attendance_rule")
public class AttendanceRuleEntity {

    /**
     * 考勤规则ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规则名称
     */
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    /**
     * 上班时间（HH:mm）
     */
    @Column(name = "work_start_time", length = 5)
    private String workStartTime;

    /**
     * 下班时间（HH:mm）
     */
    @Column(name = "work_end_time", length = 5)
    private String workEndTime;

    /**
     * 迟到容忍分钟数
     */
    @Column(name = "late_tolerance")
    private Integer lateTolerance;

    /**
     * 早退容忍分钟数
     */
    @Column(name = "early_leave_tolerance")
    private Integer earlyLeaveTolerance;

    /**
     * 适用部门名称（逗号分隔）
     */
    @Column(name = "applicable_departments", length = 500)
    private String applicableDepartments;

    /**
     * 状态：0禁用 1启用
     */
    @Column(name = "status")
    private Integer status;

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
