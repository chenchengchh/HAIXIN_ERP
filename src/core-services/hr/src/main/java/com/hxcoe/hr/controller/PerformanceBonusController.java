package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.PerformanceBonusEntity;
import com.hxcoe.hr.service.PerformanceBonusService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 绩效奖金Controller
 */
@RestController
@RequestMapping("/api/v1/hr/performance-bonuses")
@Tag(name = "绩效奖金管理", description = "绩效奖金相关接口")
public class PerformanceBonusController {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceBonusController.class);

    @Autowired
    private PerformanceBonusService performanceBonusService;

    /**
     * 创建绩效奖金
     * @param bonus 绩效奖金实体
     * @return 绩效奖金实体
     */
    @PostMapping
    @Operation(summary = "创建绩效奖金", description = "创建新的绩效奖金记录")
    public Result<PerformanceBonusEntity> createBonus(@RequestBody PerformanceBonusEntity bonus) {
        logger.info("创建绩效奖金");
        PerformanceBonusEntity createdBonus = performanceBonusService.createBonus(bonus);
        return Result.success(createdBonus);
    }

    /**
     * 根据ID查询绩效奖金
     * @param id 绩效奖金ID
     * @return 绩效奖金实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询绩效奖金", description = "根据ID查询绩效奖金记录")
    public Result<PerformanceBonusEntity> getBonusById(@PathVariable Long id) {
        logger.info("查询绩效奖金: id={}", id);
        PerformanceBonusEntity bonus = performanceBonusService.getBonusById(id);
        return Result.success(bonus);
    }

    /**
     * 更新绩效奖金
     * @param id 绩效奖金ID
     * @param bonus 绩效奖金实体
     * @return 绩效奖金实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新绩效奖金", description = "根据ID更新绩效奖金记录")
    public Result<PerformanceBonusEntity> updateBonus(@PathVariable Long id, @RequestBody PerformanceBonusEntity bonus) {
        logger.info("更新绩效奖金: id={}", id);
        PerformanceBonusEntity updatedBonus = performanceBonusService.updateBonus(id, bonus);
        return Result.success(updatedBonus);
    }

    /**
     * 删除绩效奖金
     * @param id 绩效奖金ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除绩效奖金", description = "根据ID删除绩效奖金记录")
    public Result<Void> deleteBonus(@PathVariable Long id) {
        logger.info("删除绩效奖金: id={}", id);
        performanceBonusService.deleteBonus(id);
        return Result.success();
    }

    /**
     * 查询全部绩效奖金
     * @return 绩效奖金列表
     */
    @GetMapping
    @Operation(summary = "查询全部绩效奖金", description = "查询全部绩效奖金记录")
    public Result<List<PerformanceBonusEntity>> getAllBonuses() {
        logger.info("查询全部绩效奖金");
        List<PerformanceBonusEntity> bonuses = performanceBonusService.getAllBonuses();
        return Result.success(bonuses);
    }

    /**
     * 分页查询绩效奖金
     * @param appraisalPeriod 考核周期（可空，为空时查询全部）
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 绩效奖金分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询绩效奖金", description = "分页查询绩效奖金记录，支持按考核周期过滤")
    public Result<Page<PerformanceBonusEntity>> getBonusesByPage(
            @RequestParam(name = "appraisalPeriod", required = false) String appraisalPeriod,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        logger.info("分页查询绩效奖金: appraisalPeriod={}, page={}, size={}", appraisalPeriod, page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<PerformanceBonusEntity> bonuses = performanceBonusService.getBonusesByPage(appraisalPeriod, pageable);
        return Result.success(bonuses);
    }

    /**
     * 审批绩效奖金
     * @param id 绩效奖金ID
     * @param status 状态（0待审批 1已批准 2已发放）
     * @return 更新后的绩效奖金实体
     */
    @PutMapping("/{id}/approve")
    @Operation(summary = "审批绩效奖金", description = "根据ID更新绩效奖金审批状态")
    public Result<PerformanceBonusEntity> approve(@PathVariable Long id, @RequestParam(name = "status") Integer status) {
        logger.info("审批绩效奖金: id={}, status={}", id, status);
        PerformanceBonusEntity updatedBonus = performanceBonusService.approve(id, status);
        return Result.success(updatedBonus);
    }
}
