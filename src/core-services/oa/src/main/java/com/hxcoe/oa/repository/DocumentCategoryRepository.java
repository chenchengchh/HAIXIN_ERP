package com.hxcoe.oa.repository;

import com.hxcoe.oa.entity.DocumentCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档分类Repository
 */
@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategoryEntity, Long> {

    /**
     * 根据父分类ID查询子分类列表
     */
    List<DocumentCategoryEntity> findByParentId(Long parentId);

    /**
     * 根据分类名称查询
     */
    DocumentCategoryEntity findByName(String name);

    /**
     * 根据分类状态查询
     */
    List<DocumentCategoryEntity> findByStatus(Integer status);
}