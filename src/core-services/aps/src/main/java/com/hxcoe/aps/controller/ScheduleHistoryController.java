package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ScheduleHistoryEntity;
import com.hxcoe.aps.service.ScheduleHistoryService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aps/schedule-histories")
@Tag(name = "排程历史管理", description = "排程历史管理相关接口")
public class ScheduleHistoryController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleHistoryController.class);

    @Autowired
    private ScheduleHistoryService scheduleHistoryService;

    @GetMapping
    @Operation(summary = "获取所有排程历史", description = "获取所有排程历史列表")
    public Result<List<ScheduleHistoryEntity>> getAllHistories() {
        logger.info("获取所有排程历史");
        return Result.success(scheduleHistoryService.getAllHistories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询排程历史", description = "根据ID查询排程历史")
    public Result<ScheduleHistoryEntity> getHistoryById(@PathVariable Long id) {
        logger.info("查询排程历史: id={}", id);
        ScheduleHistoryEntity history = scheduleHistoryService.getHistoryById(id);
        return Result.success(history);
    }

    @GetMapping("/plan/{planId}")
    @Operation(summary = "根据计划ID查询历史", description = "根据计划ID查询排程历史")
    public Result<List<ScheduleHistoryEntity>> getHistoriesByPlanId(@PathVariable Long planId) {
        logger.info("根据计划ID查询历史: planId={}", planId);
        List<ScheduleHistoryEntity> histories = scheduleHistoryService.getHistoriesByPlanId(planId);
        return Result.success(histories);
    }

    @GetMapping("/schedule/{scheduleId}")
    @Operation(summary = "根据排程ID查询历史", description = "根据排程ID查询排程历史")
    public Result<List<ScheduleHistoryEntity>> getHistoriesByScheduleId(@PathVariable Long scheduleId) {
        logger.info("根据排程ID查询历史: scheduleId={}", scheduleId);
        List<ScheduleHistoryEntity> histories = scheduleHistoryService.getHistoriesByScheduleId(scheduleId);
        return Result.success(histories);
    }

    @PostMapping
    @Operation(summary = "创建排程历史", description = "创建新的排程历史")
    public Result<ScheduleHistoryEntity> createHistory(@RequestBody ScheduleHistoryEntity history) {
        logger.info("创建排程历史");
        ScheduleHistoryEntity createdHistory = scheduleHistoryService.createHistory(history);
        return Result.success(createdHistory);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除排程历史", description = "根据ID删除排程历史")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        logger.info("删除排程历史: id={}", id);
        scheduleHistoryService.deleteHistory(id);
        return Result.success();
    }

    @PostMapping("/batch")
    @Operation(summary = "批量创建排程历史", description = "批量创建排程历史")
    public Result<List<ScheduleHistoryEntity>> batchCreateHistories(@RequestBody List<ScheduleHistoryEntity> histories) {
        logger.info("批量创建排程历史: count={}", histories.size());
        List<ScheduleHistoryEntity> createdHistories = scheduleHistoryService.batchCreateHistories(histories);
        return Result.success(createdHistories);
    }
}
