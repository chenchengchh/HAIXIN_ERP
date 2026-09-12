package com.hxcoe.oa.service;

import com.hxcoe.oa.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 文档管理Service
 */
public interface DocumentService {

    /**
     * 创建文档
     */
    DocumentEntity createDocument(DocumentEntity document);

    /**
     * 更新文档
     */
    DocumentEntity updateDocument(Long id, DocumentEntity document);

    /**
     * 删除文档
     */
    void deleteDocument(Long id);

    /**
     * 根据ID获取文档
     */
    DocumentEntity getDocumentById(Long id);

    /**
     * 获取文档列表
     */
    List<DocumentEntity> getDocumentList();

    /**
     * 分页获取文档列表
     */
    Page<DocumentEntity> getDocumentPage(Pageable pageable);

    /**
     * 根据分类ID获取文档列表
     */
    List<DocumentEntity> getDocumentByCategoryId(Long categoryId);

    /**
     * 根据状态获取文档列表
     */
    List<DocumentEntity> getDocumentByStatus(Integer status);

    /**
     * 根据创建者ID获取文档列表
     */
    List<DocumentEntity> getDocumentByCreatorId(Long creatorId);

    /**
     * 发布文档
     */
    DocumentEntity publishDocument(Long id);

    /**
     * 撤销文档
     */
    DocumentEntity revokeDocument(Long id);

    /**
     * 归档文档
     */
    DocumentEntity archiveDocument(Long id);
}