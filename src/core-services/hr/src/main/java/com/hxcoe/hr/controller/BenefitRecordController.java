package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.BenefitRecordEntity;
import com.hxcoe.hr.service.BenefitRecordService;
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
 * 福利发放记录Controller
 */
@RestController
@RequestMapping("/api/v1/hr/benefit-records")
@Tag(name = "福利发放管理", description = "福利发放记录相关接口")
public class BenefitRecordController {

    private static final Logger logger = LoggerFactory.getLogger(BenefitRecordController.class);

    @Autowired
    private BenefitRecordService benefitRecordService;

    /**
     * 创建福利发放记录
     * @param benefitRecord 福利发放记录实体
     * @return 福利发放记录实体
     */
    @PostMapping
    @Operation(summary = "创建福利发放记录", description = "创建新的福利发放记录")
    public Result<BenefitRecordEntity> createBenefitRecord(@RequestBody BenefitRecordEntity benefitRecord) {
        logger.info("创建福利发放记录");
        BenefitRecordEntity createdRecord = benefitRecordService.createBenefitRecord(benefitRecord);
        return Result.success(createdRecord);
    }

    /**
     * 根据ID查询福利发放记录
     * @param id 福利发放记录ID
     * @return 福利发放记录实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询福利发放记录", description = "根据ID查询福利发放记录")
    public Result<BenefitRecordEntity> getBenefitRecordById(@PathVariable Long id) {
        logger.info("查询福利发放记录: id={}", id);
        BenefitRecordEntity record = benefitRecordService.getBenefitRecordById(id);
        return Result.success(record);
    }

    /**
     * 更新福利发放记录
     * @param id 福利发放记录ID
     * @param benefitRecord 福利发放记录实体
     * @return 福利发放记录实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新福利发放记录", description = "根据ID更新福利发放记录")
    public Result<BenefitRecordEntity> updateBenefitRecord(@PathVariable Long id, @RequestBody BenefitRecordEntity benefitRecord) {
        logger.info("更新福利发放记录: id={}", id);
        BenefitRecordEntity updatedRecord = benefitRecordService.updateBenefitRecord(id, benefitRecord);
        return Result.success(updatedRecord);
    }

    /**
     * 删除福利发放记录
     * @param id 福利发放记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除福利发放记录", description = "根据ID删除福利发放记录")
    public Result<Void> deleteBenefitRecord(@PathVariable Long id) {
        logger.info("删除福利发放记录: id={}", id);
        benefitRecordService.deleteBenefitRecord(id);
        return Result.success();
    }

    /**
     * 查询所有福利发放记录
     * @return 福利发放记录列表
     */
    @GetMapping
    @Operation(summary = "查询所有福利发放记录", description = "获取所有福利发放记录列表")
    public Result<List<BenefitRecordEntity>> getAllBenefitRecords() {
        logger.info("查询所有福利发放记录");
        List<BenefitRecordEntity> benefitRecords = benefitRecordService.getAllBenefitRecords();
        return Result.success(benefitRecords);
    }

    /**
     * 分页查询福利发放记录
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 福利发放记录分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询福利发放记录", description = "分页查询福利发放记录")
    public Result<org.springframework.data.domain.Page<BenefitRecordEntity>> getBenefitRecordsByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        logger.info("分页查询福利发放记录: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        org.springframework.data.domain.Page<BenefitRecordEntity> benefitRecords = benefitRecordService.getBenefitRecordsByPage(pageable);
        return Result.success(benefitRecords);
    }

    /**
     * 按福利配置发放福利
     * @param benefitId 福利配置ID
     * @return 生成的发放记录条数
     */
    @PostMapping("/distribute")
    @Operation(summary = "发放福利", description = "为指定福利配置的所有在职员工生成发放记录")
    public Result<Integer> distributeByBenefitId(@RequestParam Long benefitId) {
        logger.info("发放福利: benefitId={}", benefitId);
        int count = benefitRecordService.distributeByBenefitId(benefitId);
        return Result.success(count);
    }
}
