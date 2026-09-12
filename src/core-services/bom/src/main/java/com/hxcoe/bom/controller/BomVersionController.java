package com.hxcoe.bom.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.bom.entity.BomHeaderEntity;
import com.hxcoe.bom.repository.BomHeaderRepository;
import com.hxcoe.bom.service.BomStructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BOM版本管理控制器
 */
@RestController
@RequestMapping({"/bom/version", "/api/v1/bom/version"})
public class BomVersionController {

    @Autowired
    private BomStructureService bomStructureService;

    @Autowired
    private BomHeaderRepository bomHeaderRepository;

    /**
     * 获取BOM版本列表
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getBomVersions(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "bomCode", required = false) String bomCode,
            @RequestParam(name = "productName", required = false) String productName,
            @RequestParam(name = "status", required = false) Integer status) {
        // 返回分页版本列表
        return ResultAdapter.fromResult(bomStructureService.getBomVersionsPage(page, size, bomCode, productName, status));
    }

    /**
     * 激活BOM版本
     */
    @PutMapping("/{id}/activate")
    public ApiResponse<BomHeaderEntity> activateVersion(@PathVariable("id") Long id) {
        BomHeaderEntity bomHeader = bomStructureService.getBomHeaderById(id).getData();
        if (bomHeader == null) {
            return ApiResponse.error(404, "BOM版本不存在");
        }
        
        bomHeader.setStatus(1); // 激活状态
        return ResultAdapter.fromResult(bomStructureService.updateBomHeader(id, bomHeader));
    }

    /**
     * 失效BOM版本
     */
    @PutMapping("/{id}/deactivate")
    public ApiResponse<BomHeaderEntity> deactivateVersion(@PathVariable("id") Long id) {
        BomHeaderEntity bomHeader = bomStructureService.getBomHeaderById(id).getData();
        if (bomHeader == null) {
            return ApiResponse.error(404, "BOM版本不存在");
        }
        
        bomHeader.setStatus(2); // 历史状态
        return ResultAdapter.fromResult(bomStructureService.updateBomHeader(id, bomHeader));
    }

    /**
     * 设置为默认版本
     */
    @PutMapping("/{id}/set-default")
    public ApiResponse<BomHeaderEntity> setDefaultVersion(@PathVariable("id") Long id) {
        BomHeaderEntity bomHeader = bomStructureService.getBomHeaderById(id).getData();
        if (bomHeader == null) {
            return ApiResponse.error(404, "BOM版本不存在");
        }
        
        bomHeader.setIsDefault(true);
        return ResultAdapter.fromResult(bomStructureService.updateBomHeader(id, bomHeader));
    }
    
    /**
     * 获取BOM版本详情
     */
    @GetMapping("/{id}")
    public ApiResponse<BomHeaderEntity> getBomVersionDetail(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(bomStructureService.getBomHeaderById(id));
    }
    
    /**
     * 获取BOM版本变更历史
     */
    @GetMapping("/{id}/change-history")
    public ApiResponse<List<Map<String, Object>>> getBomChangeHistory(@PathVariable("id") Long id) {
        BomHeaderEntity bomHeader = bomStructureService.getBomHeaderById(id).getData();
        if (bomHeader == null) {
            return ApiResponse.error(404, "BOM版本不存在");
        }
        List<BomHeaderEntity> versions = bomHeaderRepository.findByBomCodeOrderByCreatedTimeDesc(bomHeader.getBomCode());
        List<Map<String, Object>> history = new ArrayList<>();
        for (BomHeaderEntity v : versions) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", v.getId());
            item.put("version", v.getVersion());
            item.put("changeReason", "版本变更");
            item.put("changedBy", v.getUpdatedBy() != null ? v.getUpdatedBy() : v.getCreatedBy());
            item.put("changeTime", v.getUpdatedTime() != null ? v.getUpdatedTime() : v.getCreatedTime());
            item.put("changeContent", v.getRemark());
            item.put("bomVersionId", id);
            history.add(item);
        }
        return ApiResponse.success(history);
    }
    
    /**
     * 创建BOM版本
     */
    @PostMapping
    public ApiResponse<BomHeaderEntity> createBomVersion(@RequestBody BomHeaderEntity bomHeader) {
        return ResultAdapter.fromResult(bomStructureService.createBomHeader(bomHeader));
    }
    
    /**
     * 更新BOM版本
     */
    @PutMapping("/{id}")
    public ApiResponse<BomHeaderEntity> updateBomVersion(@PathVariable("id") Long id, @RequestBody BomHeaderEntity bomHeader) {
        return ResultAdapter.fromResult(bomStructureService.updateBomHeader(id, bomHeader));
    }
}
