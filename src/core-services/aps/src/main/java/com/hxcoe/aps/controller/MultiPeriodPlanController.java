package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.MultiPeriodPlanEntity;
import com.hxcoe.aps.entity.MultiPeriodPlanItemEntity;
import com.hxcoe.aps.service.MultiPeriodPlanService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/aps/multi-period-plans")
@Tag(name = "多周期计划", description = "多周期计划管理相关接口")
public class MultiPeriodPlanController {

    private static final Logger logger = LoggerFactory.getLogger(MultiPeriodPlanController.class);

    @Autowired
    private MultiPeriodPlanService multiPeriodPlanService;

    /**
     * 获取多周期计划列表（按创建时间倒序）
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "获取多周期计划列表", description = "获取所有多周期计划，按创建时间倒序")
    public Result<List<MultiPeriodPlanEntity>> getAllPlans() {
        logger.info("获取多周期计划列表");
        return Result.success(multiPeriodPlanService.getAllPlans());
    }

    /**
     * 获取多周期计划详情（含周期片段明细）
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "获取多周期计划详情", description = "获取多周期计划基本信息与周期片段明细")
    public Result<Map<String, Object>> getPlanDetail(@PathVariable Long id) {
        logger.info("获取多周期计划详情: id={}", id);
        try {
            MultiPeriodPlanEntity plan = multiPeriodPlanService.getPlanById(id);
            List<MultiPeriodPlanItemEntity> items = multiPeriodPlanService.getPlanItems(id);
            Map<String, Object> detail = new HashMap<>();
            detail.put("plan", plan);
            detail.put("items", items);
            return Result.success(detail);
        } catch (Exception e) {
            logger.error("获取多周期计划详情失败: id={}", id, e);
            return Result.error("获取多周期计划详情失败: " + e.getMessage());
        }
    }

    /**
     * 生成多周期计划：将源生产计划按周期类型拆分为多个周期片段
     */
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "生成多周期计划", description = "将源生产计划按周期类型与周期数量拆分生成多周期计划")
    public Result<Map<String, Object>> generatePlan(@RequestBody Map<String, Object> request) {
        try {
            Long sourcePlanId = Long.valueOf(String.valueOf(request.get("planId")));
            String periodType = request.get("periodType") != null ? String.valueOf(request.get("periodType")) : "weekly";
            Integer periodCount = request.get("periodCount") != null
                    ? Integer.valueOf(String.valueOf(request.get("periodCount"))) : 1;
            LocalDate startDate = LocalDate.parse(String.valueOf(request.get("startDate")));
            String algorithm = request.get("algorithm") != null ? String.valueOf(request.get("algorithm")) : null;
            @SuppressWarnings("unchecked")
            List<String> objectives = request.get("objectives") instanceof List
                    ? (List<String>) request.get("objectives") : null;

            logger.info("生成多周期计划: sourcePlanId={}, periodType={}, periodCount={}, startDate={}",
                    sourcePlanId, periodType, periodCount, startDate);
            Map<String, Object> result = multiPeriodPlanService.generatePlan(
                    sourcePlanId, periodType, periodCount, startDate, algorithm, objectives);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("生成多周期计划失败", e);
            return Result.error("生成多周期计划失败: " + e.getMessage());
        }
    }

    /**
     * 删除多周期计划（级联删除周期片段明细）
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "删除多周期计划", description = "删除多周期计划及其全部周期片段明细")
    public Result<Void> deletePlan(@PathVariable Long id) {
        logger.info("删除多周期计划: id={}", id);
        try {
            multiPeriodPlanService.deletePlan(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除多周期计划失败: id={}", id, e);
            return Result.error("删除多周期计划失败: " + e.getMessage());
        }
    }
}
