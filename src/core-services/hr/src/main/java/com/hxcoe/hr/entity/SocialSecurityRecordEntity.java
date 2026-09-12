package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 社保公积金记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_social_security_record")
public class SocialSecurityRecordEntity {

    /**
     * 社保公积金记录ID
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
     * 缴费月份（YYYY-MM）
     */
    @Column(name = "insurance_month", nullable = false, length = 7)
    private String insuranceMonth;

    /**
     * 缴费基数
     */
    @Column(name = "base_amount")
    private BigDecimal baseAmount;

    /**
     * 养老保险-公司缴费金额
     */
    @Column(name = "pension_company")
    private BigDecimal pensionCompany;

    /**
     * 养老保险-个人缴费金额
     */
    @Column(name = "pension_personal")
    private BigDecimal pensionPersonal;

    /**
     * 医疗保险-公司缴费金额
     */
    @Column(name = "medical_company")
    private BigDecimal medicalCompany;

    /**
     * 医疗保险-个人缴费金额
     */
    @Column(name = "medical_personal")
    private BigDecimal medicalPersonal;

    /**
     * 失业保险-公司缴费金额
     */
    @Column(name = "unemployment_company")
    private BigDecimal unemploymentCompany;

    /**
     * 失业保险-个人缴费金额
     */
    @Column(name = "unemployment_personal")
    private BigDecimal unemploymentPersonal;

    /**
     * 公积金-公司缴费金额
     */
    @Column(name = "housing_fund_company")
    private BigDecimal housingFundCompany;

    /**
     * 公积金-个人缴费金额
     */
    @Column(name = "housing_fund_personal")
    private BigDecimal housingFundPersonal;

    /**
     * 状态：0未申报 1已申报
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
