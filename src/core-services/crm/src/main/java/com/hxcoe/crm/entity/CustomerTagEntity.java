package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 客户标签实体类
 */
@Entity
@Table(name = "crm_customer_tag")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTagEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 标签名称
     */
    @Column(name = "tag_name", nullable = false, length = 64)
    private String tagName;

    /**
     * 标签类型：system/custom
     */
    @Column(name = "tag_type", nullable = false, length = 32)
    private String tagType;

    /**
     * 标签分类：industry/behavior/value
     */
    @Column(name = "tag_category", nullable = false, length = 32)
    private String tagCategory;

    /**
     * 显示颜色
     */
    @Column(name = "color", length = 16)
    private String color;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
}