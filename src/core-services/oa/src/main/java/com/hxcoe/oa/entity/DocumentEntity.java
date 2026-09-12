package com.hxcoe.oa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 文档主表实体类
 */
@Data
@Entity
@Table(name = "oa_document")
@EntityListeners(AuditingEntityListener.class)
public class DocumentEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文档标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 文档描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 分类ID
     */
    @Column(nullable = false)
    private Long categoryId;

    /**
     * 分类名称
     */
    @Column(nullable = false, length = 100)
    private String categoryName;

    /**
     * 文档类型
     */
    @Column(nullable = false, length = 20)
    private String type;

    /**
     * 文档状态：0-草稿，1-已发布，2-已归档
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 创建人ID
     */
    @Column(nullable = false)
    private Long creatorId;

    /**
     * 创建人名称
     */
    @Column(nullable = false, length = 50)
    private String creatorName;

    /**
     * 当前版本号
     */
    @Column(length = 20)
    private String currentVersion;

    /**
     * 创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    private LocalDateTime updateTime;
}