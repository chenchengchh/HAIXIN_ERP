package com.hxcoe.mes.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "mes_work_order_material")
@Data
public class WorkOrderMaterialEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_order_id")
    private Long workOrderId;
    
    @Column(name = "material_code")
    private String materialCode;

    @Column(name = "material_name")
    private String materialName;

    @Column(name = "required_quantity")
    private BigDecimal requiredQuantity; // 需求数量

    @Column(name = "consumed_quantity")
    private BigDecimal consumedQuantity; // 已领数量
    
    @Column(name = "unit")
    private String unit;
}
