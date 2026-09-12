package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户分类实体类
 */
@Entity
@Table(name = "crm_customer_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCategoryEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分类名称
     */
    @Column(name = "category_name", nullable = false, length = 64)
    private String categoryName;

    /**
     * 分类编码
     */
    @Column(name = "category_code", nullable = false, length = 64, unique = true)
    private String categoryCode;

    /**
     * 父分类ID（0表示根分类）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 分类描述
     */
    @Column(name = "description", length = 256)
    private String description;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 状态：1启用 0停用
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 持久化前自动填充时间戳
     */
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新前自动刷新时间戳
     */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
