package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 知识库文章实体类
 */
@Data
@Entity
@Table(name = "crm_knowledge_article")
@EntityListeners(AuditingEntityListener.class)
public class KnowledgeArticle {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章分类
     */
    private String category;

    /**
     * 标签（多个以逗号分隔）
     */
    private String tags;

    /**
     * 文章内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 作者
     */
    private String author;

    /**
     * 浏览次数
     */
    private Integer views = 0;

    /**
     * 状态：PUBLISHED-已发布，DRAFT-草稿，OFFLINE-已下线
     */
    private String status = "PUBLISHED";

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Column(name = "is_deleted")
    private Integer isDeleted = 0;
}
