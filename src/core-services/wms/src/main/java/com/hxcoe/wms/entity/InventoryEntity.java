package com.hxcoe.wms.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_inventory")
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long warehouseId;
    private String warehouseCode;
    
    private String locationCode; // 库位

    private String materialCode;
    private String materialName;
    
    private BigDecimal quantity;
    private String unit;

    private String batchNo; // 批次号

    private String status; // 库存状态：NORMAL正常/FROZEN冻结/CHECKED已盘点
    private String remark; // 备注（冻结/调整原因等）

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
