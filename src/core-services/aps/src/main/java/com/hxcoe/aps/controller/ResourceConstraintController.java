package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ResourceConstraintEntity;
import com.hxcoe.aps.service.ResourceConstraintService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/resource-constraints")
@Tag(name = "资源约束管理", description = "资源约束管理相关接口")
public class ResourceConstraintController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceConstraintController.class);

    @Autowired
    private ResourceConstraintService resourceConstraintService;

    @GetMapping
    @Operation(summary = "获取所有资源约束", description = "获取所有资源约束列表")
    public Result<List<ResourceConstraintEntity>> getResourceConstraints() {
        logger.info("获取所有资源约束");
        List<ResourceConstraintEntity> constraints = resourceConstraintService.getAllResourceConstraints();
        return Result.success(constraints);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询资源约束", description = "根据ID查询资源约束")
    public Result<ResourceConstraintEntity> getResourceConstraintById(@PathVariable Long id) {
        logger.info("查询资源约束: id={}", id);
        return resourceConstraintService.getResourceConstraintById(id)
                .map(Result::success)
                .orElse(Result.fail("Resource constraint not found"));
    }

    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "根据资源ID查询约束", description = "根据资源ID查询资源约束")
    public Result<List<ResourceConstraintEntity>> getResourceConstraintsByResourceId(@PathVariable Long resourceId) {
        logger.info("根据资源ID查询约束: resourceId={}", resourceId);
        List<ResourceConstraintEntity> constraints = resourceConstraintService.getResourceConstraintsByResourceId(resourceId);
        return Result.success(constraints);
    }

    @PostMapping
    @Operation(summary = "创建资源约束", description = "创建新的资源约束")
    public Result<ResourceConstraintEntity> createResourceConstraint(@RequestBody ResourceConstraintEntity constraint) {
        logger.info("创建资源约束");
        ResourceConstraintEntity createdConstraint = resourceConstraintService.createResourceConstraint(constraint);
        return Result.success(createdConstraint);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新资源约束", description = "根据ID更新资源约束")
    public Result<ResourceConstraintEntity> updateResourceConstraint(
            @PathVariable Long id,
            @RequestBody ResourceConstraintEntity constraint) {
        logger.info("更新资源约束: id={}", id);
        ResourceConstraintEntity updatedConstraint = resourceConstraintService.updateResourceConstraint(id, constraint);
        return Result.success(updatedConstraint);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除资源约束", description = "根据ID删除资源约束")
    public Result<Void> deleteResourceConstraint(@PathVariable Long id) {
        logger.info("删除资源约束: id={}", id);
        resourceConstraintService.deleteResourceConstraint(id);
        return Result.success();
    }
}
