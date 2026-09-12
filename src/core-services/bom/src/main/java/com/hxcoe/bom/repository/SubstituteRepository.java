package com.hxcoe.bom.repository;

import com.hxcoe.bom.entity.SubstituteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 替代料Repository接口
 */
public interface SubstituteRepository extends JpaRepository<SubstituteEntity, Long>, JpaSpecificationExecutor<SubstituteEntity> {
    
    /**
     * 根据主物料ID查询替代料
     */
    List<SubstituteEntity> findByMainMaterialId(Long mainMaterialId);
    
    /**
     * 根据子物料ID查询替代料
     */
    List<SubstituteEntity> findBySubMaterialId(Long subMaterialId);
    
    /**
     * 根据BOM行ID查询替代料
     */
    List<SubstituteEntity> findByBomLineId(Long bomLineId);
    
    /**
     * 根据主物料ID和BOM行ID查询替代料
     */
    List<SubstituteEntity> findByMainMaterialIdAndBomLineId(Long mainMaterialId, Long bomLineId);
}