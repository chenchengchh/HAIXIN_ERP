package com.hxcoe.scm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scm_purchase_order")
@Data
public class PurchaseOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_no", unique = true, nullable = false, length = 50)
    private String orderNo;
    
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;
    
    @Column(name = "supplier_code", length = 50, nullable = false)
    private String supplierCode;
    
    @Column(name = "supplier_name", length = 100, nullable = false)
    private String supplierName;
    
    @Column(name = "purchase_type", nullable = false)
    private Integer purchaseType;
    
    @Column(name = "order_status", nullable = false)
    private Integer orderStatus;
    
    @Column(name = "order_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal orderAmount;
    
    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
    
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
    
    @Column(name = "remark", length = 500)
    private String remark;
}