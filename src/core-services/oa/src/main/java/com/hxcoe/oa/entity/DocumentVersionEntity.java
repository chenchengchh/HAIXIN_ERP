package com.hxcoe.oa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 文档版本实体类
 */
@Data
@Entity
@Table(name = "oa_document_version")
@EntityListeners(AuditingEntityListener.class)
public class DocumentVersionEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文档ID
     */
    @Column(nullable = false)
    private Long documentId;

    /**
     * 版本号
     */
    @Column(nullable = false, length = 20)
    private String version;

    /**
     * 版本名称
     */
    @Column(nullable = false, length = 100)
    private String versionName;

    /**
     * 版本描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 文件名
     */
    @Column(nullable = false, length = 200)
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件路径
     */
    @Column(nullable = false, length = 500)
    private String filePath;

    /**
     * 文件哈希值
     */
    @Column(length = 100)
    private String fileHash;

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
     * 是否为当前版本：0-否，1-是
     */
    @Column(nullable = false)
    private Integer isCurrent;

    /**
     * 创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;
}