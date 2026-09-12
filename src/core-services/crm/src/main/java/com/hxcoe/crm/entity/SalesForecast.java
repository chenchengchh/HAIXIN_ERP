package com.hxcoe.crm.entity;

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
 * 销售预测实体类
 */
@Data
@Entity
@Table(name = "crm_sales_forecast")
@EntityListeners(AuditingEntityListener.class)
public class SalesForecast {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 预测周期（如 2026-Q1 或 2026-07）
     */
    private String period;

    /**
     * 负责人姓名
     */
    @Column(name = "owner_name")
    private String ownerName;

    /**
     * 目标金额
     */
    @Column(name = "target_amount")
    private BigDecimal targetAmount;

    /**
     * 预测金额
     */
    @Column(name = "forecast_amount")
    private BigDecimal forecastAmount;

    /**
     * 实际金额
     */
    @Column(name = "actual_amount")
    private BigDecimal actualAmount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Column(name = "is_deleted")
    private Integer isDeleted = 0;
}
