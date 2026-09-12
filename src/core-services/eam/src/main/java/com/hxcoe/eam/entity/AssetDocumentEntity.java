package com.hxcoe.eam.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 设备文档实体：存储设备相关文档（说明书、图纸、合格证等）的元数据及文件内容。
 *
 * <p>对应表 eam_asset_document，文件内容以 Base64 字符串形式存储（content 字段，
 * 带 "base64:" 前缀，与 EMS 报表导出历史存储约定一致），列表查询时通过
 * {@link #getContent()} 上的 {@link JsonIgnore} 避免大字段序列化，下载时走专用端点。
 */
@Data
@Entity
@Table(name = "eam_asset_document")
public class AssetDocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 文档名称 */
    private String name;

    /** 原始文件名（含扩展名，下载时使用） */
    @Column(name = "file_name")
    private String fileName;

    /** 关联设备资产ID */
    @Column(name = "asset_id")
    private Long assetId;

    /** 关联设备名称（冗余，便于列表展示） */
    @Column(name = "asset_name")
    private String assetName;

    /** 文档类型（如：操作手册、维修图纸、合格证） */
    private String type;

    /** 文档大小展示文本（如：2.5MB） */
    private String size;

    /** 状态：active-启用，inactive-禁用 */
    private String status;

    /** 上传人 */
    @Column(name = "uploaded_by")
    private String uploadedBy;

    /**
     * 文件内容（Base64，带 "base64:" 前缀）。
     * WRITE_ONLY：允许前端上传时传入，列表/详情序列化时不输出，避免响应体过大；
     * 下载走专用端点 /documents/{id}/download。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
