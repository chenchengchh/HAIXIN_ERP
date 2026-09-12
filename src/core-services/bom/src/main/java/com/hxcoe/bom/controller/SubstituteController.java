package com.hxcoe.bom.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.bom.entity.SubstituteEntity;
import com.hxcoe.bom.service.SubstituteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 替代料管理控制器
 */
@RestController
@RequestMapping({"/bom/substitute", "/api/v1/bom/substitute"})
public class SubstituteController {

    @Autowired
    private SubstituteService substituteService;

    /**
     * 创建替代关系
     */
    @PostMapping
    public ApiResponse<SubstituteEntity> createSubstitute(@RequestBody SubstituteEntity substitute) {
        return ResultAdapter.fromResult(substituteService.createSubstitute(substitute));
    }

    /**
     * 更新替代关系
     */
    @PutMapping("/{id}")
    public ApiResponse<SubstituteEntity> updateSubstitute(@PathVariable("id") Long id, @RequestBody SubstituteEntity substitute) {
        return ResultAdapter.fromResult(substituteService.updateSubstitute(id, substitute));
    }

    /**
     * 删除替代关系
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSubstitute(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(substituteService.deleteSubstitute(id));
    }

    /**
     * 获取替代关系详情
     */
    @GetMapping("/{id}")
    public ApiResponse<SubstituteEntity> getSubstituteById(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(substituteService.getSubstituteById(id));
    }

    /**
     * 查询某物料在特定上下文的可用替代料
     */
    @GetMapping("/query/{materialId}")
    public ApiResponse<List<SubstituteEntity>> getSubstitutesByMaterial(
            @PathVariable("materialId") Long materialId, 
            @RequestParam(name = "bomLineId", required = false) Long bomLineId) {
        if (bomLineId != null) {
            return ResultAdapter.fromResult(substituteService.getSubstitutesByMaterialAndBomLine(materialId, bomLineId));
        } else {
            return ResultAdapter.fromResult(substituteService.getSubstitutesByMainMaterialId(materialId));
        }
    }

    /**
     * 获取物料的替代关系列表
     */
    @GetMapping
    public ApiResponse<PageResult<SubstituteEntity>> getSubstituteList(
            @RequestParam(name = "mainMaterialId", required = false) Long mainMaterialId,
            @RequestParam(name = "mainMaterialCode", required = false) String mainMaterialCode,
            @RequestParam(name = "subMaterialCode", required = false) String subMaterialCode,
            @RequestParam(name = "bomLineId", required = false) Long bomLineId,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "substituteType", required = false) Integer substituteType,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return ResultAdapter.fromResult(
                substituteService.getSubstitutesPage(mainMaterialId, mainMaterialCode, subMaterialCode, bomLineId, status, substituteType, page, size)
        );
    }

    /**
     * 启用替代料
     */
    @PutMapping("/{id}/enable")
    public ApiResponse<SubstituteEntity> enableSubstitute(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(substituteService.enableSubstitute(id));
    }

    /**
     * 禁用替代料
     */
    @PutMapping("/{id}/disable")
    public ApiResponse<SubstituteEntity> disableSubstitute(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(substituteService.disableSubstitute(id));
    }
}
