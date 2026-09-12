package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.BenefitConfigEntity;
import com.hxcoe.hr.service.BenefitConfigService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 福利配置Controller
 */
@RestController
@RequestMapping("/api/v1/hr/benefit-configs")
@Tag(name = "福利配置管理", description = "福利配置相关接口")
public class BenefitConfigController {

    private static final Logger logger = LoggerFactory.getLogger(BenefitConfigController.class);

    @Autowired
    private BenefitConfigService benefitConfigService;

    /**
     * 创建福利配置
     * @param benefitConfig 福利配置实体
     * @return 福利配置实体
     */
    @PostMapping
    @Operation(summary = "创建福利配置", description = "创建新的福利配置")
    public Result<BenefitConfigEntity> createBenefitConfig(@RequestBody BenefitConfigEntity benefitConfig) {
        logger.info("创建福利配置");
        BenefitConfigEntity createdConfig = benefitConfigService.createBenefitConfig(benefitConfig);
        return Result.success(createdConfig);
    }

    /**
     * 根据ID查询福利配置
     * @param id 福利配置ID
     * @return 福利配置实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询福利配置", description = "根据ID查询福利配置")
    public Result<BenefitConfigEntity> getBenefitConfigById(@PathVariable Long id) {
        logger.info("查询福利配置: id={}", id);
        BenefitConfigEntity config = benefitConfigService.getBenefitConfigById(id);
        return Result.success(config);
    }

    /**
     * 更新福利配置
     * @param id 福利配置ID
     * @param benefitConfig 福利配置实体
     * @return 福利配置实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新福利配置", description = "根据ID更新福利配置")
    public Result<BenefitConfigEntity> updateBenefitConfig(@PathVariable Long id, @RequestBody BenefitConfigEntity benefitConfig) {
        logger.info("更新福利配置: id={}", id);
        BenefitConfigEntity updatedConfig = benefitConfigService.updateBenefitConfig(id, benefitConfig);
        return Result.success(updatedConfig);
    }

    /**
     * 删除福利配置
     * @param id 福利配置ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除福利配置", description = "根据ID删除福利配置")
    public Result<Void> deleteBenefitConfig(@PathVariable Long id) {
        logger.info("删除福利配置: id={}", id);
        benefitConfigService.deleteBenefitConfig(id);
        return Result.success();
    }

    /**
     * 查询所有福利配置
     * @return 福利配置列表
     */
    @GetMapping
    @Operation(summary = "查询所有福利配置", description = "获取所有福利配置列表")
    public Result<List<BenefitConfigEntity>> getAllBenefitConfigs() {
        logger.info("查询所有福利配置");
        List<BenefitConfigEntity> benefitConfigs = benefitConfigService.getAllBenefitConfigs();
        return Result.success(benefitConfigs);
    }

    /**
     * 分页查询福利配置
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 福利配置分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询福利配置", description = "分页查询福利配置")
    public Result<org.springframework.data.domain.Page<BenefitConfigEntity>> getBenefitConfigsByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        logger.info("分页查询福利配置: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        org.springframework.data.domain.Page<BenefitConfigEntity> benefitConfigs = benefitConfigService.getBenefitConfigsByPage(pageable);
        return Result.success(benefitConfigs);
    }
}
