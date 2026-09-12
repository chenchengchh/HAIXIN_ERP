package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "srm_purchase_order")
public class PurchaseOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", unique = true, nullable = false)
    private String orderNo;

    private Long supplierId;

    @Column(name = "supplier_code")
    private String supplierCode;
    private String supplierName;
    
    private BigDecimal totalAmount;
    private String currency;
    
    // CREATED, APPROVED, SENT, CONFIRMED, PARTIAL_RECEIVED, COMPLETED, CANCELLED
    private String status;
    
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    
    private String remarks;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItemEntity> items;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
