package com.hxcoe.bom.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hxcoe.bom.converter.JsonObjectConverter;

@Entity
@Table(name = "bom_material")
@Data
public class MaterialEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_code", nullable = false, unique = true, length = 50)
    private String materialCode;

    @Column(name = "material_name", nullable = false, length = 100)
    private String materialName;

    @Column(name = "material_spec", length = 200)
    private String materialSpec;

    @Column(name = "material_type", length = 50)
    private String materialType;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "category_name", length = 100)
    private String categoryName;

    @Column(name = "unit_price", precision = 18, scale = 6)
    private BigDecimal unitPrice;

    @Column(name = "attr_json", columnDefinition = "LONGTEXT")
    @Convert(converter = JsonObjectConverter.class)
    private Object attrJson;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "remark", length = 500)
    private String remark;
}
