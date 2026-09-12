package com.hxcoe.wms.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "wms_outbound_order")
public class OutboundOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", unique = true, nullable = false, length = 64)
    @JsonAlias({"outboundNo", "order_no"})
    private String orderNo;

    // SALES, PRODUCTION, TRANSFER
    @JsonAlias({"orderType"})
    private String type;
    
    // Source Document (e.g., Sales Order No)
    @JsonAlias({"source_no"})
    private String sourceNo;
    
    private String customerName;
    private String address;

    // 备注（创建/编辑时由前端传入）
    @Column(length = 500)
    private String remark;
    
    // CREATED, PICKING, PACKED, SHIPPED
    private String status;
    
    private Long waveId; // Wave ID if assigned

    @OneToMany(mappedBy = "outboundOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundOrderItemEntity> items;

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
