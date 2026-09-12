package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.TrainingPlanEntity;
import com.hxcoe.hr.service.TrainingPlanService;
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
 * 培训计划Controller
 */
@RestController
@RequestMapping("/api/v1/hr/training-plans")
@Tag(name = "培训计划管理", description = "培训计划管理相关接口")
public class TrainingPlanController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingPlanController.class);

    @Autowired
    private TrainingPlanService trainingPlanService;

    /**
     * 查询全部培训计划
     * @return 培训计划列表
     */
    @GetMapping
    @Operation(summary = "查询全部培训计划", description = "查询全部培训计划列表")
    public Result<List<TrainingPlanEntity>> getAllTrainingPlans() {
        logger.info("查询全部培训计划");
        List<TrainingPlanEntity> trainingPlans = trainingPlanService.getAllTrainingPlans();
        return Result.success(trainingPlans);
    }

    /**
     * 分页查询培训计划
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 培训计划分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询培训计划", description = "分页查询培训计划")
    public Result<Page<TrainingPlanEntity>> getTrainingPlansByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        logger.info("分页查询培训计划: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TrainingPlanEntity> trainingPlans = trainingPlanService.getTrainingPlansByPage(pageable);
        return Result.success(trainingPlans);
    }

    /**
     * 根据ID查询培训计划
     * @param id 培训计划ID
     * @return 培训计划实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询培训计划", description = "根据ID查询培训计划")
    public Result<TrainingPlanEntity> getTrainingPlanById(@PathVariable Long id) {
        logger.info("查询培训计划: id={}", id);
        TrainingPlanEntity trainingPlan = trainingPlanService.getTrainingPlanById(id);
        return Result.success(trainingPlan);
    }

    /**
     * 创建培训计划
     * @param trainingPlan 培训计划实体
     * @return 培训计划实体
     */
    @PostMapping
    @Operation(summary = "创建培训计划", description = "创建新的培训计划")
    public Result<TrainingPlanEntity> createTrainingPlan(@RequestBody TrainingPlanEntity trainingPlan) {
        logger.info("创建培训计划");
        TrainingPlanEntity createdPlan = trainingPlanService.createTrainingPlan(trainingPlan);
        return Result.success(createdPlan);
    }

    /**
     * 更新培训计划
     * @param id 培训计划ID
     * @param trainingPlan 培训计划实体
     * @return 培训计划实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新培训计划", description = "根据ID更新培训计划")
    public Result<TrainingPlanEntity> updateTrainingPlan(@PathVariable Long id, @RequestBody TrainingPlanEntity trainingPlan) {
        logger.info("更新培训计划: id={}", id);
        TrainingPlanEntity updatedPlan = trainingPlanService.updateTrainingPlan(id, trainingPlan);
        return Result.success(updatedPlan);
    }

    /**
     * 删除培训计划
     * @param id 培训计划ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除培训计划", description = "根据ID删除培训计划")
    public Result<Void> deleteTrainingPlan(@PathVariable Long id) {
        logger.info("删除培训计划: id={}", id);
        trainingPlanService.deleteTrainingPlan(id);
        return Result.success();
    }
}
