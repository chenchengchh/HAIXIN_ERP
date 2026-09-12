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
 * 调薪记录实体类
 */
@Data
@Entity
@Table(name = "hr_salary_adjustment")
@EntityListeners(AuditingEntityListener.class)
public class SalaryAdjustmentEntity {

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
     * 原薪资
     */
    @Column(name = "old_salary", precision = 10, scale = 2)
    private BigDecimal oldSalary;

    /**
     * 新薪资
     */
    @Column(name = "new_salary", precision = 10, scale = 2)
    private BigDecimal newSalary;

    /**
     * 调薪原因
     */
    @Column(name = "reason")
    private String reason;

    /**
     * 调薪日期
     */
    @Column(name = "adjustment_date")
    private LocalDateTime adjustmentDate;

    /**
     * 状态（0：申请中，1：已通过，2：已拒绝）
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