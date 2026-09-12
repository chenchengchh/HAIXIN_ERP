package com.hxcoe.bom.repository;

import com.hxcoe.bom.entity.BomHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * BOM头表Repository接口
 */
public interface BomHeaderRepository extends JpaRepository<BomHeaderEntity, Long>, JpaSpecificationExecutor<BomHeaderEntity> {
    
    /**
     * 根据物料ID查询BOM头表
     */
    List<BomHeaderEntity> findByMaterialId(Long materialId);
    
    /**
     * 根据物料ID和版本查询BOM头表
     */
    Optional<BomHeaderEntity> findByMaterialIdAndVersion(Long materialId, String version);
    
    /**
     * 根据物料ID查询默认版本的BOM
     */
    Optional<BomHeaderEntity> findByMaterialIdAndIsDefaultTrue(Long materialId);

    List<BomHeaderEntity> findByBomCodeOrderByCreatedTimeDesc(String bomCode);
}
