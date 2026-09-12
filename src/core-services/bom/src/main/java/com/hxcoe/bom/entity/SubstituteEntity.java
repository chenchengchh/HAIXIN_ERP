package com.hxcoe.bom.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 替代料实体类
 */
@Entity
@Table(name = "bom_substitute")
@Data
public class SubstituteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "main_material_id", nullable = false)
    private Long mainMaterialId;

    @Column(name = "sub_material_id", nullable = false)
    private Long subMaterialId;

    @Column(name = "bom_line_id")
    private Long bomLineId;

    @Column(name = "ratio", nullable = false)
    private Double ratio; // 替代比例

    @Column(name = "priority")
    private Integer priority; // 优先级

    @Column(name = "status", nullable = false)
    private Integer status; // 0:禁用 1:启用

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /** 主料编码（非持久化，查询时按mainMaterialId回填） */
    @Transient
    private String mainMaterialCode;

    /** 主料名称（非持久化，查询时按mainMaterialId回填） */
    @Transient
    private String mainMaterialName;

    /** 主料规格（非持久化，查询时按mainMaterialId回填） */
    @Transient
    private String mainMaterialSpec;

    /** 替代料编码（非持久化，查询时按subMaterialId回填） */
    @Transient
    private String subMaterialCode;

    /** 替代料名称（非持久化，查询时按subMaterialId回填） */
    @Transient
    private String subMaterialName;

    /** 替代料规格（非持久化，查询时按subMaterialId回填） */
    @Transient
    private String subMaterialSpec;

    /** 替代类型（非持久化）：1-全局替代 2-局部替代，按bomLineId是否有值推导 */
    @Transient
    private Integer substituteType;
}