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
 * 销售目标实体类
 */
@Data
@Entity
@Table(name = "crm_sales_target")
@EntityListeners(AuditingEntityListener.class)
public class SalesTarget {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 负责人姓名
     */
    @Column(name = "owner_name")
    private String ownerName;

    /**
     * 目标周期（如 2026-Q1 或 2026-07）
     */
    private String period;

    /**
     * 目标金额
     */
    @Column(name = "target_amount")
    private BigDecimal targetAmount;

    /**
     * 已达成金额
     */
    @Column(name = "achieved_amount")
    private BigDecimal achievedAmount;

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
