package com.hxcoe.oa.service;

import com.hxcoe.oa.entity.DocumentVersionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 文档版本管理Service
 */
public interface DocumentVersionService {

    /**
     * 创建文档版本
     */
    DocumentVersionEntity createDocumentVersion(DocumentVersionEntity version);

    /**
     * 更新文档版本
     */
    DocumentVersionEntity updateDocumentVersion(Long id, DocumentVersionEntity version);

    /**
     * 删除文档版本
     */
    void deleteDocumentVersion(Long id);

    /**
     * 根据ID获取文档版本
     */
    DocumentVersionEntity getDocumentVersionById(Long id);

    /**
     * 获取文档版本列表
     */
    List<DocumentVersionEntity> getDocumentVersionList();

    /**
     * 分页获取文档版本列表
     */
    Page<DocumentVersionEntity> getDocumentVersionPage(Pageable pageable);

    /**
     * 根据文档ID获取文档版本列表
     */
    List<DocumentVersionEntity> getDocumentVersionByDocumentId(Long documentId);

    /**
     * 根据文档ID获取最新版本
     */
    DocumentVersionEntity getLatestVersionByDocumentId(Long documentId);

    /**
     * 根据文档ID和版本号获取文档版本
     */
    DocumentVersionEntity getDocumentVersionByDocumentIdAndVersion(Long documentId, Integer version);

    /**
     * 回滚文档到指定版本
     */
    DocumentVersionEntity rollbackDocumentVersion(Long id);
}