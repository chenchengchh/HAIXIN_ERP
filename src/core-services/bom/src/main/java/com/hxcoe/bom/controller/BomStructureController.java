package com.hxcoe.bom.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.bom.entity.BomHeaderEntity;
import com.hxcoe.bom.entity.BomLineEntity;
import com.hxcoe.bom.service.BomStructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * BOM结构管理控制器
 */
@RestController
@RequestMapping({"/bom/structure", "/api/v1/bom/structure"})
public class BomStructureController {

    @Autowired
    private BomStructureService bomStructureService;

    /**
     * 创建BOM头
     */
    @PostMapping
    public ApiResponse<BomHeaderEntity> createBomHeader(@RequestBody BomHeaderEntity bomHeader) {
        return ResultAdapter.fromResult(bomStructureService.createBomHeader(bomHeader));
    }

    /**
     * 更新BOM头
     */
    @PutMapping("/{id}")
    public ApiResponse<BomHeaderEntity> updateBomHeader(@PathVariable("id") Long id, @RequestBody BomHeaderEntity bomHeader) {
        return ResultAdapter.fromResult(bomStructureService.updateBomHeader(id, bomHeader));
    }

    /**
     * 删除BOM头
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBomHeader(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(bomStructureService.deleteBomHeader(id));
    }

    /**
     * 获取BOM头详情
     */
    @GetMapping("/header/{id}")
    public ApiResponse<BomHeaderEntity> getBomHeaderById(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(bomStructureService.getBomHeaderById(id));
    }

    /**
     * 根据物料ID获取BOM列表
     */
    @GetMapping("/material/{materialId}")
    public ApiResponse<List<BomHeaderEntity>> getBomHeadersByMaterialId(@PathVariable("materialId") Long materialId) {
        return ResultAdapter.fromResult(bomStructureService.getBomHeadersByMaterialId(materialId));
    }

    /**
     * 获取物料的默认BOM
     */
    @GetMapping("/material/{materialId}/default")
    public ApiResponse<BomHeaderEntity> getDefaultBomByMaterialId(@PathVariable("materialId") Long materialId) {
        return ResultAdapter.fromResult(bomStructureService.getDefaultBomByMaterialId(materialId));
    }

    /**
     * 获取BOM树
     */
    @GetMapping("/tree/{productId}")
    public ApiResponse<Map<String, Object>> getBomTree(
            @PathVariable("productId") Long productId,
            @RequestParam(name = "version", required = false) String version) {
        return ResultAdapter.fromResult(bomStructureService.getBomTree(productId, version));
    }

    /**
     * 物料反查
     */
    @GetMapping("/where-used/{materialId}")
    public ApiResponse<Map<String, Object>> getWhereUsed(@PathVariable("materialId") Long materialId) {
        return ResultAdapter.fromResult(bomStructureService.getWhereUsed(materialId));
    }

    /**
     * 添加BOM子项
     */
    @PostMapping("/{headerId}/lines")
    public ApiResponse<BomLineEntity> addBomLine(@PathVariable("headerId") Long headerId, @RequestBody BomLineEntity bomLine) {
        bomLine.setHeaderId(headerId);
        return ResultAdapter.fromResult(bomStructureService.addBomLine(bomLine));
    }

    /**
     * 更新BOM子项
     */
    @PutMapping("/lines/{lineId}")
    public ApiResponse<BomLineEntity> updateBomLine(@PathVariable("lineId") Long lineId, @RequestBody BomLineEntity bomLine) {
        return ResultAdapter.fromResult(bomStructureService.updateBomLine(lineId, bomLine));
    }

    /**
     * 删除BOM子项
     */
    @DeleteMapping("/lines/{lineId}")
    public ApiResponse<Void> deleteBomLine(@PathVariable("lineId") Long lineId) {
        return ResultAdapter.fromResult(bomStructureService.deleteBomLine(lineId));
    }

    /**
     * 根据BOM头ID获取BOM明细
     */
    @GetMapping("/header/{headerId}/lines")
    public ApiResponse<List<BomLineEntity>> getBomLinesByHeaderId(@PathVariable("headerId") Long headerId) {
        return ResultAdapter.fromResult(bomStructureService.getBomLinesByHeaderId(headerId));
    }

    @PutMapping("/{headerId}/lines/bulk")
    public ApiResponse<List<BomLineEntity>> replaceBomLines(
            @PathVariable("headerId") Long headerId,
            @RequestBody List<BomLineEntity> lines) {
        return ResultAdapter.fromResult(bomStructureService.replaceBomLines(headerId, lines));
    }
    
    /**
     * 比较两个BOM版本
     */
    @GetMapping("/compare")
    public ApiResponse<Map<String, Object>> compareBoms(
            @RequestParam(name = "id1") Long id1,
            @RequestParam(name = "id2") Long id2) {
        return ResultAdapter.fromResult(bomStructureService.compareBoms(id1, id2));
    }
}
