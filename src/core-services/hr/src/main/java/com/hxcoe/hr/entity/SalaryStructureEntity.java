package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 薪酬结构实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_salary_structure")
public class SalaryStructureEntity {

    /**
     * 薪酬结构ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 名称
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 基本工资
     */
    @Column(name = "basic_salary", precision = 10, scale = 2)
    private BigDecimal basicSalary;

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
     * 生效日期
     */
    @Column(name = "effective_date")
    private LocalDate effectiveDate;

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