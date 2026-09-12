package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ResourceCapabilityEntity;
import com.hxcoe.aps.repository.ResourceCapabilityRepository;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/resource-capability")
@Tag(name = "资源能力管理", description = "资源能力管理相关接口")
public class ResourceCapabilityController {

    private static final Logger logger = LoggerFactory.getLogger(ResourceCapabilityController.class);

    @Autowired
    private ResourceCapabilityRepository resourceCapabilityRepository;

    @GetMapping
    @Operation(summary = "获取所有资源能力", description = "获取所有资源能力数据")
    public Result<List<ResourceCapabilityEntity>> getResourceCapabilityData() {
        logger.info("获取所有资源能力");
        List<ResourceCapabilityEntity> capabilityData = resourceCapabilityRepository.findAll();
        return Result.success(capabilityData);
    }

    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "根据资源ID查询能力", description = "根据资源ID获取资源能力")
    public Result<List<ResourceCapabilityEntity>> getResourceCapabilityByResourceId(@PathVariable Long resourceId) {
        logger.info("根据资源ID查询能力: resourceId={}", resourceId);
        List<ResourceCapabilityEntity> capabilityData = resourceCapabilityRepository.findByResourceId(resourceId);
        return Result.success(capabilityData);
    }

    @GetMapping("/type/{capabilityType}")
    @Operation(summary = "根据类型查询能力", description = "根据产能类型获取资源能力")
    public Result<List<ResourceCapabilityEntity>> getResourceCapabilityByType(@PathVariable String capabilityType) {
        logger.info("根据类型查询能力: capabilityType={}", capabilityType);
        List<ResourceCapabilityEntity> capabilityData = resourceCapabilityRepository.findByCapabilityType(capabilityType);
        return Result.success(capabilityData);
    }

    @PostMapping
    @Operation(summary = "创建资源能力", description = "创建新的资源能力")
    public Result<ResourceCapabilityEntity> createResourceCapability(@RequestBody ResourceCapabilityEntity capability) {
        logger.info("创建资源能力");
        ResourceCapabilityEntity createdCapability = resourceCapabilityRepository.save(capability);
        return Result.success(createdCapability);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新资源能力", description = "根据ID更新资源能力")
    public Result<ResourceCapabilityEntity> updateResourceCapability(
            @PathVariable Long id,
            @RequestBody ResourceCapabilityEntity capability) {
        logger.info("更新资源能力: id={}", id);
        capability.setId(id);
        ResourceCapabilityEntity updatedCapability = resourceCapabilityRepository.save(capability);
        return Result.success(updatedCapability);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除资源能力", description = "根据ID删除资源能力")
    public Result<Void> deleteResourceCapability(@PathVariable Long id) {
        logger.info("删除资源能力: id={}", id);
        resourceCapabilityRepository.deleteById(id);
        return Result.success();
    }
}
