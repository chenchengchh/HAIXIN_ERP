package com.hxcoe.wms.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_inventory_transaction")
public class InventoryTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // IN, OUT, ADJUST, MOVE
    private String type;
    
    private String warehouseCode;
    private String locationCode;
    
    private String materialCode;
    private String materialName;
    
    private BigDecimal quantity;
    private String unit;
    
    private String batchNo;
    
    // Source Document (e.g., ASN No, Outbound Order No)
    private String sourceNo;
    
    private LocalDateTime transactionTime;
    private String operator;

    @PrePersist
    protected void onCreate() {
        transactionTime = LocalDateTime.now();
    }
}
