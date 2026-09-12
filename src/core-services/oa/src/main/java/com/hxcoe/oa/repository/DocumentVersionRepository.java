package com.hxcoe.oa.repository;

import com.hxcoe.oa.entity.DocumentVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档版本Repository
 */
@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersionEntity, Long> {

    /**
     * 根据文档ID查询版本列表
     */
    List<DocumentVersionEntity> findByDocumentId(Long documentId);

    /**
     * 根据文档ID和是否为当前版本查询
     */
    DocumentVersionEntity findByDocumentIdAndIsCurrent(Long documentId, Integer isCurrent);

    /**
     * 根据文档ID和版本号查询
     */
    DocumentVersionEntity findByDocumentIdAndVersion(Long documentId, String version);
}