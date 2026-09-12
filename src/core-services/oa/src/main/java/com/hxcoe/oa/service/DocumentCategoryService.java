package com.hxcoe.oa.service;

import com.hxcoe.oa.entity.DocumentCategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 文档分类管理Service
 */
public interface DocumentCategoryService {

    /**
     * 创建文档分类
     */
    DocumentCategoryEntity createDocumentCategory(DocumentCategoryEntity category);

    /**
     * 更新文档分类
     */
    DocumentCategoryEntity updateDocumentCategory(Long id, DocumentCategoryEntity category);

    /**
     * 删除文档分类
     */
    void deleteDocumentCategory(Long id);

    /**
     * 根据ID获取文档分类
     */
    DocumentCategoryEntity getDocumentCategoryById(Long id);

    /**
     * 获取文档分类列表
     */
    List<DocumentCategoryEntity> getDocumentCategoryList();

    /**
     * 分页获取文档分类列表
     */
    Page<DocumentCategoryEntity> getDocumentCategoryPage(Pageable pageable);

    /**
     * 根据父分类ID获取文档分类列表
     */
    List<DocumentCategoryEntity> getDocumentCategoryByParentId(Long parentId);

    /**
     * 根据状态获取文档分类列表
     */
    List<DocumentCategoryEntity> getDocumentCategoryByStatus(Integer status);
}