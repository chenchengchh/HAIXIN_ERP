package com.hxcoe.bom.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * BOM明细表实体类
 */
@Entity
@Table(name = "bom_line")
@Data
public class BomLineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "header_id", nullable = false)
    private Long headerId;

    @Column(name = "parent_material_id")
    private Long parentMaterialId;

    @Column(name = "child_material_id", nullable = false)
    private Long childMaterialId;

    @Column(name = "child_material_code", length = 50)
    private String childMaterialCode;

    @Column(name = "child_material_name", length = 100)
    private String childMaterialName;

    @Column(name = "quantity", nullable = false)
    private Double quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "scrap_rate")
    private Double scrapRate;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "usage_type", length = 50)
    private String usageType;

    @Column(name = "remark", length = 500)
    private String remark;
}