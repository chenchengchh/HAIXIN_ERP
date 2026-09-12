package com.hxcoe.eam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备层次结构实体：表示设备之间的父子组成关系（部件/BOM 结构）。
 *
 * <p>对应表 eam_asset_hierarchy，parent_id 指向父设备，child_id 指向子设备/部件所属资产，
 * component_type 描述部件类型（如主轴单元、进给系统），quantity 为所需数量。
 */
@Data
@Entity
@Table(name = "eam_asset_hierarchy")
public class AssetHierarchyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 父设备资产ID */
    @Column(name = "parent_id")
    private Long parentId;

    /** 子设备/部件资产ID */
    @Column(name = "child_id")
    private Long childId;

    /** 部件类型（如：主轴单元、进给系统） */
    @Column(name = "component_type")
    private String componentType;

    /** 数量 */
    private Integer quantity;

    /** 备注 */
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
