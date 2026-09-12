package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ResourceLoadEntity;
import com.hxcoe.aps.entity.ResourceEntity;
import com.hxcoe.aps.service.ResourceLoadService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/resource-load")
@Tag(name = "资源负载管理", description = "资源负载管理相关接口")
public class ResourceLoadController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceLoadController.class);

    @Autowired
    private ResourceLoadService resourceLoadService;

    @GetMapping
    @Operation(summary = "获取资源负载数据", description = "获取资源负载数据")
    public Result<List<ResourceLoadEntity>> getResourceLoadData(
            @RequestParam(name = "planId", required = false) Long planId,
            @RequestParam(name = "resourceIds", required = false) List<Long> resourceIds,
            @RequestParam(name = "startTime", required = false) String startTime,
            @RequestParam(name = "endTime", required = false) String endTime,
            @RequestParam(name = "timeScale", required = false) String timeScale) {
        logger.info("获取资源负载数据: planId={}, resourceIds={}, startTime={}, endTime={}, timeScale={}", planId, resourceIds, startTime, endTime, timeScale);
        List<ResourceLoadEntity> loadData = resourceLoadService.getResourceLoadData(planId, resourceIds, startTime, endTime, timeScale);
        return Result.success(loadData);
    }

    @GetMapping("/resources")
    @Operation(summary = "获取资源列表", description = "获取所有资源列表")
    public Result<List<ResourceEntity>> getResources() {
        logger.info("获取资源列表");
        List<ResourceEntity> resources = resourceLoadService.getResources();
        return Result.success(resources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询资源负载", description = "根据ID查询资源负载")
    public Result<ResourceLoadEntity> getResourceLoadById(@PathVariable Long id) {
        logger.info("查询资源负载: id={}", id);
        ResourceLoadEntity load = resourceLoadService.getResourceLoadById(id);
        return Result.success(load);
    }

    @PostMapping
    @Operation(summary = "创建资源负载", description = "创建新的资源负载")
    public Result<ResourceLoadEntity> createResourceLoad(@RequestBody ResourceLoadEntity load) {
        logger.info("创建资源负载");
        ResourceLoadEntity createdLoad = resourceLoadService.createResourceLoad(load);
        return Result.success(createdLoad);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新资源负载", description = "根据ID更新资源负载")
    public Result<ResourceLoadEntity> updateResourceLoad(
            @PathVariable Long id,
            @RequestBody ResourceLoadEntity load) {
        logger.info("更新资源负载: id={}", id);
        ResourceLoadEntity updatedLoad = resourceLoadService.updateResourceLoad(id, load);
        return Result.success(updatedLoad);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除资源负载", description = "根据ID删除资源负载")
    public Result<Void> deleteResourceLoad(@PathVariable Long id) {
        logger.info("删除资源负载: id={}", id);
        resourceLoadService.deleteResourceLoad(id);
        return Result.success();
    }
}
