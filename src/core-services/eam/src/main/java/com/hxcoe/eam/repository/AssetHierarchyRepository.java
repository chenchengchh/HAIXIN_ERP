package com.hxcoe.eam.repository;

import com.hxcoe.eam.entity.AssetHierarchyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 设备层次结构 Repository：提供 eam_asset_hierarchy 表的基础 CRUD。
 */
@Repository
public interface AssetHierarchyRepository extends JpaRepository<AssetHierarchyEntity, Long> {
}
