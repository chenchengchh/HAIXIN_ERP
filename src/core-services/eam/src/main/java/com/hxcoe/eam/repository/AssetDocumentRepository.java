package com.hxcoe.eam.repository;

import com.hxcoe.eam.entity.AssetDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 设备文档 Repository：提供 eam_asset_document 表的基础 CRUD。
 */
@Repository
public interface AssetDocumentRepository extends JpaRepository<AssetDocumentEntity, Long> {
}
