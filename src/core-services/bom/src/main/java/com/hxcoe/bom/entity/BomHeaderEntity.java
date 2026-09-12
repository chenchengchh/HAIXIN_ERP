package com.hxcoe.bom.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * BOM头表实体类
 * 唯一约束：(bom_code, version) 组合唯一，支持同一BOM编码多版本并行
 */
@Entity
@Table(name = "bom_header", uniqueConstraints = {
        @UniqueConstraint(name = "uk_bom_header_code_version", columnNames = {"bom_code", "version"})
}, indexes = {
        @Index(name = "idx_bom_header_bom_code", columnList = "bom_code"),
        @Index(name = "idx_bom_header_material_id", columnList = "material_id"),
        @Index(name = "idx_bom_header_status_default", columnList = "status, is_default")
})
@Data
public class BomHeaderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "material_code", length = 50)
    private String materialCode;

    @Column(name = "material_name", length = 100)
    private String materialName;

    @Column(name = "bom_code", length = 50)
    private String bomCode;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "type")
    private Integer type; // 1:EBOM 2:MBOM 3:PBOM

    @Column(name = "status")
    private Integer status; // 0:Draft 1:Active 2:History

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}