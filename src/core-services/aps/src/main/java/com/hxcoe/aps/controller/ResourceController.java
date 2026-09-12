package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ResourceEntity;
import com.hxcoe.aps.service.ResourceService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/resources")
@Tag(name = "资源管理", description = "资源管理相关接口")
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private ResourceService resourceService;

    @GetMapping
    @Operation(summary = "获取资源列表", description = "获取所有资源列表")
    public Result<List<ResourceEntity>> getResources(@RequestParam(value = "type", required = false) String type, 
                                                  @RequestParam(value = "status", required = false) String status) {
        logger.info("获取资源列表: type={}, status={}", type, status);
        List<ResourceEntity> resources = resourceService.getAllResources();
        return Result.success(resources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询资源", description = "根据ID查询资源")
    public Result<ResourceEntity> getResourceById(@PathVariable("id") Long id) {
        logger.info("查询资源: id={}", id);
        ResourceEntity resource = resourceService.getResourceById(id);
        return Result.success(resource);
    }
}
