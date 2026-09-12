package com.hxcoe.oa.repository;

import com.hxcoe.oa.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档Repository
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    /**
     * 根据分类ID查询文档列表
     */
    List<DocumentEntity> findByCategoryId(Long categoryId);

    /**
     * 根据创建人ID查询文档列表
     */
    List<DocumentEntity> findByCreatorId(Long creatorId);

    /**
     * 根据文档状态查询文档列表
     */
    List<DocumentEntity> findByStatus(Integer status);

    /**
     * 根据文档类型查询文档列表
     */
    List<DocumentEntity> findByType(String type);

    /**
     * 根据文档标题模糊查询
     */
    List<DocumentEntity> findByTitleContaining(String title);
}