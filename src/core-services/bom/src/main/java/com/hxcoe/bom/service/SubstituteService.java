package com.hxcoe.bom.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.bom.entity.SubstituteEntity;

import java.util.List;

/**
 * 替代料管理Service接口
 */
public interface SubstituteService {
    /**
     * 创建替代关系
     */
    Result<SubstituteEntity> createSubstitute(SubstituteEntity substitute);
    
    /**
     * 更新替代关系
     */
    Result<SubstituteEntity> updateSubstitute(Long id, SubstituteEntity substitute);
    
    /**
     * 删除替代关系
     */
    Result<Void> deleteSubstitute(Long id);
    
    /**
     * 获取替代关系详情
     */
    Result<SubstituteEntity> getSubstituteById(Long id);
    
    /**
     * 根据主物料ID查询替代料
     */
    Result<List<SubstituteEntity>> getSubstitutesByMainMaterialId(Long mainMaterialId);
    
    /**
     * 根据BOM行ID查询替代料
     */
    Result<List<SubstituteEntity>> getSubstitutesByBomLineId(Long bomLineId);
    
    /**
     * 查询物料在特定BOM行的替代料
     */
    Result<List<SubstituteEntity>> getSubstitutesByMaterialAndBomLine(Long mainMaterialId, Long bomLineId);

    /**
     * 分页查询替代关系（支持按替代类型过滤：1-全局 2-局部）
     */
    Result<PageResult<SubstituteEntity>> getSubstitutesPage(
            Long mainMaterialId,
            String mainMaterialCode,
            String subMaterialCode,
            Long bomLineId,
            Integer status,
            Integer substituteType,
            Integer page,
            Integer size);
    
    /**
     * 启用替代料
     */
    Result<SubstituteEntity> enableSubstitute(Long id);
    
    /**
     * 禁用替代料
     */
    Result<SubstituteEntity> disableSubstitute(Long id);
}
