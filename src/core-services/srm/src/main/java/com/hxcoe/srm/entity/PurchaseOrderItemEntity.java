package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "srm_purchase_order_item")
public class PurchaseOrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id")
    @JsonIgnore
    private PurchaseOrderEntity purchaseOrder;

    private String materialCode;
    private String materialName;
    
    private BigDecimal quantity;
    private String unit;
    
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    
    private BigDecimal receivedQuantity;
}
