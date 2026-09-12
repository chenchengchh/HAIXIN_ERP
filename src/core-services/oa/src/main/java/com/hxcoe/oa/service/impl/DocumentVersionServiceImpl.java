package com.hxcoe.oa.service.impl;

import com.hxcoe.oa.entity.DocumentVersionEntity;
import com.hxcoe.oa.repository.DocumentVersionRepository;
import com.hxcoe.oa.service.DocumentVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文档版本管理ServiceImpl
 */
@Service
public class DocumentVersionServiceImpl implements DocumentVersionService {

    @Autowired
    private DocumentVersionRepository documentVersionRepository;

    /**
     * 创建文档版本
     */
    @Override
    public DocumentVersionEntity createDocumentVersion(DocumentVersionEntity version) {
        return documentVersionRepository.save(version);
    }

    /**
     * 更新文档版本
     */
    @Override
    public DocumentVersionEntity updateDocumentVersion(Long id, DocumentVersionEntity version) {
        DocumentVersionEntity existingVersion = documentVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档版本不存在"));
        version.setId(id);
        return documentVersionRepository.save(version);
    }

    /**
     * 删除文档版本
     */
    @Override
    public void deleteDocumentVersion(Long id) {
        DocumentVersionEntity version = documentVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档版本不存在"));
        documentVersionRepository.delete(version);
    }

    /**
     * 根据ID获取文档版本
     */
    @Override
    public DocumentVersionEntity getDocumentVersionById(Long id) {
        return documentVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档版本不存在"));
    }

    /**
     * 获取文档版本列表
     */
    @Override
    public List<DocumentVersionEntity> getDocumentVersionList() {
        return documentVersionRepository.findAll();
    }

    /**
     * 分页获取文档版本列表
     */
    @Override
    public Page<DocumentVersionEntity> getDocumentVersionPage(Pageable pageable) {
        return documentVersionRepository.findAll(pageable);
    }

    /**
     * 根据文档ID获取文档版本列表
     */
    @Override
    public List<DocumentVersionEntity> getDocumentVersionByDocumentId(Long documentId) {
        return documentVersionRepository.findByDocumentId(documentId);
    }

    /**
     * 根据文档ID获取最新版本
     */
    @Override
    public DocumentVersionEntity getLatestVersionByDocumentId(Long documentId) {
        List<DocumentVersionEntity> versions = documentVersionRepository.findByDocumentId(documentId);
        if (versions.isEmpty()) {
            throw new RuntimeException("文档版本不存在");
        }
        // 简单返回第一个版本，实际应该按版本号排序
        return versions.get(0);
    }

    /**
     * 根据文档ID和版本号获取文档版本
     */
    @Override
    public DocumentVersionEntity getDocumentVersionByDocumentIdAndVersion(Long documentId, Integer version) {
        // 将Integer转换为String类型
        DocumentVersionEntity versionEntity = documentVersionRepository.findByDocumentIdAndVersion(documentId, version.toString());
        if (versionEntity == null) {
            throw new RuntimeException("文档版本不存在");
        }
        return versionEntity;
    }

    /**
     * 回滚文档到指定版本
     */
    @Override
    public DocumentVersionEntity rollbackDocumentVersion(Long id) {
        DocumentVersionEntity version = documentVersionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档版本不存在"));
        // 这里可以添加回滚逻辑，例如更新文档的当前版本等
        return version;
    }
}