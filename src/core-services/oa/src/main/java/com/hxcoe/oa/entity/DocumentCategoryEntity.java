package com.hxcoe.oa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 文档分类实体类
 */
@Data
@Entity
@Table(name = "oa_document_category")
@EntityListeners(AuditingEntityListener.class)
public class DocumentCategoryEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分类名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 分类描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 父分类名称
     */
    @Column(length = 100)
    private String parentName;

    /**
     * 分类层级
     */
    @Column(nullable = false)
    private Integer level;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 创建人ID
     */
    @Column(nullable = false)
    private Long creatorId;

    /**
     * 创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    @LastModifiedDate
    private LocalDateTime updateTime;
}