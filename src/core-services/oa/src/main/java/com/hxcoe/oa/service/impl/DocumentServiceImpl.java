package com.hxcoe.oa.service.impl;

import com.hxcoe.oa.entity.DocumentEntity;
import com.hxcoe.oa.repository.DocumentRepository;
import com.hxcoe.oa.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文档管理ServiceImpl
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    /**
     * 创建文档
     */
    @Override
    public DocumentEntity createDocument(DocumentEntity document) {
        return documentRepository.save(document);
    }

    /**
     * 更新文档
     */
    @Override
    public DocumentEntity updateDocument(Long id, DocumentEntity document) {
        DocumentEntity existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        document.setId(id);
        return documentRepository.save(document);
    }

    /**
     * 删除文档
     */
    @Override
    public void deleteDocument(Long id) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        documentRepository.delete(document);
    }

    /**
     * 根据ID获取文档
     */
    @Override
    public DocumentEntity getDocumentById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
    }

    /**
     * 获取文档列表
     */
    @Override
    public List<DocumentEntity> getDocumentList() {
        return documentRepository.findAll();
    }

    /**
     * 分页获取文档列表
     */
    @Override
    public Page<DocumentEntity> getDocumentPage(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    /**
     * 根据分类ID获取文档列表
     */
    @Override
    public List<DocumentEntity> getDocumentByCategoryId(Long categoryId) {
        return documentRepository.findByCategoryId(categoryId);
    }

    /**
     * 根据状态获取文档列表
     */
    @Override
    public List<DocumentEntity> getDocumentByStatus(Integer status) {
        return documentRepository.findByStatus(status);
    }

    /**
     * 根据创建者ID获取文档列表
     */
    @Override
    public List<DocumentEntity> getDocumentByCreatorId(Long creatorId) {
        return documentRepository.findByCreatorId(creatorId);
    }

    /**
     * 发布文档
     */
    @Override
    public DocumentEntity publishDocument(Long id) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        document.setStatus(1); // 1-已发布
        return documentRepository.save(document);
    }

    /**
     * 撤销文档
     */
    @Override
    public DocumentEntity revokeDocument(Long id) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        document.setStatus(0); // 0-草稿
        return documentRepository.save(document);
    }

    /**
     * 归档文档
     */
    @Override
    public DocumentEntity archiveDocument(Long id) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        document.setStatus(2); // 2-已归档
        return documentRepository.save(document);
    }
}