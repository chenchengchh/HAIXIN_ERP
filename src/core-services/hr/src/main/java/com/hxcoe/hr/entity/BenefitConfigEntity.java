package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 福利配置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_benefit_config")
public class BenefitConfigEntity {

    /**
     * 福利配置ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 福利名称
     */
    @Column(name = "benefit_name", nullable = false, length = 100)
    private String benefitName;

    /**
     * 福利类型：0节日福利 1生日福利 2健康体检 3补贴
     */
    @Column(name = "benefit_type")
    private Integer benefitType;

    /**
     * 标准金额
     */
    @Column(name = "standard_amount")
    private BigDecimal standardAmount;

    /**
     * 发放频率：0一次性 1每月 2每年
     */
    @Column(name = "frequency")
    private Integer frequency;

    /**
     * 状态：0停用 1启用
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 福利描述
     */
    @Column(name = "description", length = 500)
    private String description;

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
