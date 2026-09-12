package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商机实体类
 */
@Data
@Entity
@Table(name = "crm_opportunity")
@EntityListeners(AuditingEntityListener.class)
public class Opportunity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 商机编号
     */
    @Column(name = "opportunity_no")
    private String opportunityNo;

    /**
     * 商机名称
     */
    @Column(name = "opportunity_name")
    private String opportunityName;

    /**
     * 客户ID
     */
    @Column(name = "customer_id")
    private Long customerId;

    /**
     * 客户名称
     */
    @Column(name = "customer_name")
    private String customerName;

    /**
     * 来源线索ID
     */
    @Column(name = "lead_id")
    private Long leadId;

    /**
     * 阶段：initial/需求确认/方案报价/谈判/成交/失败
     */
    private String stage;

    /**
     * 赢单概率（%）
     */
    @Column(name = "win_probability")
    private Integer winProbability;

    /**
     * 预计金额
     */
    @Column(name = "estimated_amount")
    private BigDecimal estimatedAmount;

    /**
     * 实际金额
     */
    @Column(name = "actual_amount")
    private BigDecimal actualAmount;

    /**
     * 预计成交日期
     */
    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    /**
     * 实际成交日期
     */
    @Column(name = "actual_close_date")
    private LocalDate actualCloseDate;

    /**
     * 负责人ID
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * 负责人姓名
     */
    @Column(name = "owner_name")
    private String ownerName;

    /**
     * 失败原因
     */
    @Column(name = "loss_reason")
    private String lossReason;

    /**
     * 状态：ongoing/won/lost
     */
    private String status;

    /**
     * 竞争对手
     */
    private String competitors;

    /**
     * 关联产品（JSON）
     */
    private String products;

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
