package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪资记录实体类
 */
@Data
@Entity
@Table(name = "hr_payroll_record")
@EntityListeners(AuditingEntityListener.class)
public class PayrollRecordEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工ID
     */
    @Column(name = "employee_id")
    private Long employeeId;

    /**
     * 月份（格式：yyyy-MM）
     */
    @Column(name = "month", length = 7)
    private String month;

    /**
     * 基本工资
     */
    @Column(name = "basic_salary", precision = 10, scale = 2)
    private BigDecimal basicSalary;

    /**
     * 绩效工资
     */
    @Column(name = "performance_salary", precision = 10, scale = 2)
    private BigDecimal performanceSalary;

    /**
     * 奖金
     */
    @Column(name = "bonus", precision = 10, scale = 2)
    private BigDecimal bonus;

    /**
     * 补贴
     */
    @Column(name = "allowance", precision = 10, scale = 2)
    private BigDecimal allowance;

    /**
     * 扣减
     */
    @Column(name = "deduction", precision = 10, scale = 2)
    private BigDecimal deduction;

    /**
     * 实发工资
     */
    @Column(name = "actual_salary", precision = 10, scale = 2)
    private BigDecimal actualSalary;

    /**
     * 状态（0：未发放，1：已发放）
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}