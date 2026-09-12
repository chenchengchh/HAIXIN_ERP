package com.hxcoe.crm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单实体类
 */
@Entity
@Table(name = "crm_sales_order")
@Data
@EntityListeners(AuditingEntityListener.class)
public class SalesOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 订单编号
     */
    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    /**
     * 客户ID
     */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * 客户名称
     */
    @Column(name = "customer_name", length = 128)
    private String customerName;

    /**
     * 关联商机ID
     */
    @Column(name = "opportunity_id")
    private Long opportunityId;

    /**
     * 订单总额
     */
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 折扣金额
     */
    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    /**
     * 最终金额
     */
    @Column(name = "final_amount", precision = 18, scale = 2)
    private BigDecimal finalAmount;

    /**
     * 币种
     */
    @Column(name = "currency", length = 10)
    private String currency;

    /**
     * 订单日期
     */
    @Column(name = "order_date")
    private LocalDate orderDate;

    /**
     * 交付日期
     */
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    /**
     * 付款条款
     */
    @Column(name = "payment_terms", length = 256)
    private String paymentTerms;

    /**
     * 收货地址
     */
    @Column(name = "delivery_address", length = 512)
    private String deliveryAddress;

    /**
     * 销售人员ID
     */
    @Column(name = "sales_person_id")
    private Long salesPersonId;

    /**
     * 状态: draft, submitted, approved, in_progress, completed, cancelled
     */
    @Column(name = "status", length = 32)
    private String status;

    /**
     * 审批状态
     */
    @Column(name = "approval_status", length = 32)
    private String approvalStatus;

    /**
     * 发货状态: pending, shipped
     */
    @Column(name = "delivery_status", length = 32)
    private String deliveryStatus;

    /**
     * 发货时间
     */
    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 1000)
    private String remark;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 订单明细
     */
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<SalesOrderItemEntity> items;
}
