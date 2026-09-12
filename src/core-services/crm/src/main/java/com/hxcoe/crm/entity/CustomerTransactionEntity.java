package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 客户交易记录实体类
 */
@Entity
@Table(name = "crm_customer_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTransactionEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客户ID
     */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * 订单编号
     */
    @Column(name = "order_no", nullable = false, length = 64)
    private String orderNo;

    /**
     * 交易金额
     */
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /**
     * 成交日期
     */
    @Column(name = "deal_date", nullable = false)
    private LocalDate dealDate;

    /**
     * 产品信息（JSON）
     */
    @Column(name = "product_info", length = 1024)
    private String productInfo;

    /**
     * 状态：pending/completed/cancelled
     */
    @Column(name = "status", nullable = false, length = 32)
    private String status;
}