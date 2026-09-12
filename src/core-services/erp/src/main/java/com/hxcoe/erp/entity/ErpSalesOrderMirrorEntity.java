package com.hxcoe.erp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ERP销售订单镜像实体类（O2C核心链路）
 * 用于接收并落库 CRM 已确认的销售订单，支撑后续应收与生产/出库计划生成
 */
@Data
@Entity
@Table(name = "erp_sales_order_mirror")
public class ErpSalesOrderMirrorEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * CRM订单编号（幂等键，唯一）
     */
    @Column(name = "order_no", nullable = false, unique = true, length = 32)
    private String orderNo;

    /**
     * 客户ID
     */
    @Column(name = "customer_id")
    private Long customerId;

    /**
     * 客户名称
     */
    @Column(name = "customer_name", length = 128)
    private String customerName;

    /**
     * 订单总额
     */
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

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
     * 订单状态（如 confirmed）
     */
    @Column(name = "status", length = 32)
    private String status;

    /**
     * 最近接收时间
     */
    @Column(name = "receive_time", nullable = false)
    private LocalDateTime receiveTime;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
}
