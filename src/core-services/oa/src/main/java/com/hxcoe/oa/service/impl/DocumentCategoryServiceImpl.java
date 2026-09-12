package com.hxcoe.oa.service.impl;

import com.hxcoe.oa.entity.DocumentCategoryEntity;
import com.hxcoe.oa.repository.DocumentCategoryRepository;
import com.hxcoe.oa.service.DocumentCategoryService;
import com.hxcoe.oa.util.OaSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文档分类管理ServiceImpl
 */
@Service
public class DocumentCategoryServiceImpl implements DocumentCategoryService {

    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;

    /**
     * 创建文档分类。
     * <p>层级未传时自动推导：无父分类为0，有父分类为父层级+1；
     * 创建人未传时自动从当前登录上下文填充；状态缺省为启用。</p>
     */
    @Override
    public DocumentCategoryEntity createDocumentCategory(DocumentCategoryEntity category) {
        if (category.getLevel() == null) {
            int level = 0;
            if (category.getParentId() != null && category.getParentId() > 0) {
                DocumentCategoryEntity parent = documentCategoryRepository.findById(category.getParentId()).orElse(null);
                if (parent != null) {
                    level = (parent.getLevel() != null ? parent.getLevel() : 0) + 1;
                    if (category.getParentName() == null) {
                        category.setParentName(parent.getName());
                    }
                }
            }
            category.setLevel(level);
        }
        if (category.getCreatorId() == null) {
            category.setCreatorId(OaSecurityUtils.getCurrentUserId());
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        return documentCategoryRepository.save(category);
    }

    /**
     * 更新文档分类
     */
    @Override
    public DocumentCategoryEntity updateDocumentCategory(Long id, DocumentCategoryEntity category) {
        DocumentCategoryEntity existingCategory = documentCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档分类不存在"));
        category.setId(id);
        return documentCategoryRepository.save(category);
    }

    /**
     * 删除文档分类
     */
    @Override
    public void deleteDocumentCategory(Long id) {
        DocumentCategoryEntity category = documentCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档分类不存在"));
        documentCategoryRepository.delete(category);
    }

    /**
     * 根据ID获取文档分类
     */
    @Override
    public DocumentCategoryEntity getDocumentCategoryById(Long id) {
        return documentCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文档分类不存在"));
    }

    /**
     * 获取文档分类列表
     */
    @Override
    public List<DocumentCategoryEntity> getDocumentCategoryList() {
        return documentCategoryRepository.findAll();
    }

    /**
     * 分页获取文档分类列表
     */
    @Override
    public Page<DocumentCategoryEntity> getDocumentCategoryPage(Pageable pageable) {
        return documentCategoryRepository.findAll(pageable);
    }

    /**
     * 根据父分类ID获取文档分类列表
     */
    @Override
    public List<DocumentCategoryEntity> getDocumentCategoryByParentId(Long parentId) {
        return documentCategoryRepository.findByParentId(parentId);
    }

    /**
     * 根据状态获取文档分类列表
     */
    @Override
    public List<DocumentCategoryEntity> getDocumentCategoryByStatus(Integer status) {
        return documentCategoryRepository.findByStatus(status);
    }
}