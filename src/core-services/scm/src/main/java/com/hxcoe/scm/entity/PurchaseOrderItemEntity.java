package com.hxcoe.scm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scm_purchase_order_item")
@Data
public class PurchaseOrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "order_no", length = 50, nullable = false)
    private String orderNo;
    
    @Column(name = "material_code", nullable = false, length = 50)
    private String materialCode;
    
    @Column(name = "material_name", nullable = false, length = 100)
    private String materialName;
    
    @Column(name = "material_spec", length = 100)
    private String materialSpec;
    
    @Column(name = "unit", length = 20)
    private String unit;
    
    @Column(name = "quantity", nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;
    
    @Column(name = "unit_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "received_quantity", precision = 18, scale = 4)
    private BigDecimal receivedQuantity;
    
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