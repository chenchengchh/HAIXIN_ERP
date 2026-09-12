package com.hxcoe.bom.repository;

import com.hxcoe.bom.entity.BomLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * BOM明细表Repository接口
 */
public interface BomLineRepository extends JpaRepository<BomLineEntity, Long>, JpaSpecificationExecutor<BomLineEntity> {
    
    /**
     * 根据BOM头ID查询BOM明细
     */
    List<BomLineEntity> findByHeaderId(Long headerId);

    void deleteByHeaderId(Long headerId);
    
    /**
     * 根据父物料ID查询BOM明细
     */
    List<BomLineEntity> findByParentMaterialId(Long parentMaterialId);
    
    /**
     * 根据子物料ID查询BOM明细
     */
    List<BomLineEntity> findByChildMaterialId(Long childMaterialId);
    
    /**
     * 根据BOM头ID和层级查询BOM明细
     */
    List<BomLineEntity> findByHeaderIdAndLevel(Long headerId, Integer level);
}
