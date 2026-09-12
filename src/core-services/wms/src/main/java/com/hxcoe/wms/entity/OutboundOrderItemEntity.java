package com.hxcoe.wms.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "wms_outbound_order_item")
public class OutboundOrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_order_id")
    @JsonIgnore
    private OutboundOrderEntity outboundOrder;

    private String materialCode;
    private String materialName;
    
    @JsonAlias("planQuantity")
    private BigDecimal quantity;
    private String unit;
    
    // Assigned from Inventory
    private String locationCode;
    private String batchNo;
}
