package com.hxcoe.crm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售订单明细实体类
 */
@Entity
@Table(name = "crm_sales_order_item")
@Data
public class SalesOrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联销售订单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    @JsonBackReference
    private SalesOrderEntity salesOrder;

    /**
     * 产品ID
     */
    @Column(name = "product_id")
    private Long productId;

    /**
     * 产品编码
     */
    @Column(name = "product_code", length = 64)
    private String productCode;

    /**
     * 产品名称
     */
    @Column(name = "product_name", length = 128)
    private String productName;

    /**
     * 数量
     */
    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    /**
     * 单价
     */
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;

    /**
     * 折扣(%)
     */
    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    /**
     * 小计
     */
    @Column(name = "subtotal", precision = 18, scale = 2)
    private BigDecimal subtotal;

    /**
     * 单位
     */
    @Column(name = "unit", length = 16)
    private String unit;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 256)
    private String remark;
}
