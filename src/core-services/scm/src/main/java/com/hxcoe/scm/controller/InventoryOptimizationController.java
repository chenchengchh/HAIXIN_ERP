package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.InventoryStrategyEntity;
import com.hxcoe.scm.service.InventoryOptimizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/scm/inventory-optimization", "/api/scm/inventory-optimization"})
public class InventoryOptimizationController {

    @Autowired
    private InventoryOptimizationService optimizationService;

    @PostMapping("/calculate")
    public ApiResponse<Void> calculateStrategies() {
        optimizationService.calculateAllStrategies();
        return success("请求成功", null);
    }

    @PostMapping("/strategies")
    public ApiResponse<InventoryStrategyEntity> createStrategy(@RequestBody InventoryStrategyEntity strategy) {
        return success("创建成功", optimizationService.createStrategy(strategy));
    }

    @GetMapping("/strategies")
    public ApiResponse<PageResult<InventoryStrategyEntity>> getStrategies(
            @RequestParam(required = false) String materialCode,
            @RequestParam(required = false) String abcClass,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return success("查询成功", optimizationService.getStrategies(materialCode, abcClass, PageRequest.of(page - 1, size)));
    }

    @GetMapping("/strategies/{id}")
    public ApiResponse<InventoryStrategyEntity> getStrategy(@PathVariable Long id) {
        InventoryStrategyEntity entity = optimizationService.getStrategy(id);
        if (entity == null) {
            return notFound("策略不存在");
        }
        return success("查询成功", entity);
    }

    @PutMapping("/strategies/{id}")
    public ApiResponse<InventoryStrategyEntity> updateStrategy(@PathVariable Long id, @RequestBody InventoryStrategyEntity strategy) {
        InventoryStrategyEntity updated = optimizationService.updateStrategy(id, strategy);
        if (updated == null) {
            return notFound("策略不存在");
        }
        return success("更新成功", updated);
    }

    @DeleteMapping("/strategies/{id}")
    public ApiResponse<Void> deleteStrategy(@PathVariable Long id) {
        optimizationService.deleteStrategy(id);
        return success("请求成功", null);
    }

    @PutMapping("/strategies/{id}/publish")
    public ApiResponse<InventoryStrategyEntity> publishStrategy(@PathVariable Long id) {
        InventoryStrategyEntity entity = optimizationService.publishStrategy(id);
        if (entity == null) {
            return notFound("策略不存在");
        }
        return success("发布成功", entity);
    }

    @PutMapping("/strategies/{id}/disable")
    public ApiResponse<InventoryStrategyEntity> disableStrategy(@PathVariable Long id) {
        InventoryStrategyEntity entity = optimizationService.disableStrategy(id);
        if (entity == null) {
            return notFound("策略不存在");
        }
        return success("停用成功", entity);
    }

    @PostMapping("/replenishments/generate")
    public ApiResponse<List<Map<String, Object>>> generateReplenishments() {
        return success("生成成功", optimizationService.generateReplenishments());
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
