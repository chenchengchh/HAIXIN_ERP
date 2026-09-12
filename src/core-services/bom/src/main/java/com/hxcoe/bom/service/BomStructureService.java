package com.hxcoe.bom.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.bom.entity.BomHeaderEntity;
import com.hxcoe.bom.entity.BomLineEntity;

import java.util.List;
import java.util.Map;

/**
 * BOM结构管理Service接口
 */
public interface BomStructureService {
    /**
     * 创建BOM头
     */
    Result<BomHeaderEntity> createBomHeader(BomHeaderEntity bomHeader);
    
    /**
     * 更新BOM头
     */
    Result<BomHeaderEntity> updateBomHeader(Long id, BomHeaderEntity bomHeader);
    
    /**
     * 删除BOM头
     */
    Result<Void> deleteBomHeader(Long id);
    
    /**
     * 获取BOM头详情
     */
    Result<BomHeaderEntity> getBomHeaderById(Long id);
    
    /**
     * 根据物料ID获取BOM列表
     */
    Result<List<BomHeaderEntity>> getBomHeadersByMaterialId(Long materialId);
    
    /**
     * 分页获取BOM版本列表
     */
    Result<Map<String, Object>> getBomVersionsPage(Integer page, Integer size, String bomCode, String productName, Integer status);
    
    /**
     * 获取物料的默认BOM
     */
    Result<BomHeaderEntity> getDefaultBomByMaterialId(Long materialId);
    
    /**
     * 添加BOM子项
     */
    Result<BomLineEntity> addBomLine(BomLineEntity bomLine);
    
    /**
     * 更新BOM子项
     */
    Result<BomLineEntity> updateBomLine(Long id, BomLineEntity bomLine);
    
    /**
     * 删除BOM子项
     */
    Result<Void> deleteBomLine(Long id);
    
    /**
     * 获取BOM结构树
     */
    Result<Map<String, Object>> getBomTree(Long materialId, String version);
    
    /**
     * 根据BOM头ID获取BOM子项
     */
    Result<List<BomLineEntity>> getBomLinesByHeaderId(Long headerId);

    Result<List<BomLineEntity>> replaceBomLines(Long headerId, List<BomLineEntity> lines);
    
    /**
     * 物料反查
     */
    Result<Map<String, Object>> getWhereUsed(Long materialId);
    
    /**
     * 比较两个BOM版本
     */
    Result<Map<String, Object>> compareBoms(Long id1, Long id2);
}
