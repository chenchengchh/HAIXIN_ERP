package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.OptimizationSuggestionEntity;
import com.hxcoe.aps.service.OptimizationSuggestionService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/optimization-suggestions")
@Tag(name = "排程优化建议", description = "排程优化建议相关接口")
public class ScheduleOptimizationController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleOptimizationController.class);

    @Autowired
    private OptimizationSuggestionService optimizationSuggestionService;

    @GetMapping
    @Operation(summary = "获取优化建议", description = "获取调度优化建议；scheduleResultId为空时返回全部建议")
    public Result<List<OptimizationSuggestionEntity>> getOptimizationSuggestions(
            @RequestParam(name = "scheduleResultId", required = false) Long scheduleResultId,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "status", required = false) String status) {
        logger.info("获取调度优化建议: scheduleResultId={}, type={}, status={}", scheduleResultId, type, status);
        List<OptimizationSuggestionEntity> suggestions = optimizationSuggestionService.getOptimizationSuggestions(scheduleResultId, type, status);
        return Result.success(suggestions);
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "采纳优化建议", description = "将优化建议状态置为ACCEPTED")
    public Result<OptimizationSuggestionEntity> acceptSuggestion(@PathVariable Long id) {
        logger.info("采纳优化建议: id={}", id);
        OptimizationSuggestionEntity suggestion = optimizationSuggestionService.acceptSuggestion(id);
        if (suggestion == null) {
            return Result.error("优化建议不存在: " + id);
        }
        return Result.success(suggestion);
    }

    @PutMapping("/{id}/ignore")
    @Operation(summary = "忽略优化建议", description = "将优化建议状态置为IGNORED")
    public Result<OptimizationSuggestionEntity> ignoreSuggestion(@PathVariable Long id) {
        logger.info("忽略优化建议: id={}", id);
        OptimizationSuggestionEntity suggestion = optimizationSuggestionService.ignoreSuggestion(id);
        if (suggestion == null) {
            return Result.error("优化建议不存在: " + id);
        }
        return Result.success(suggestion);
    }

    @PutMapping("/{id}/reset")
    @Operation(summary = "重置优化建议状态", description = "将优化建议状态重置为NEW")
    public Result<OptimizationSuggestionEntity> resetSuggestionStatus(@PathVariable Long id) {
        logger.info("重置优化建议状态: id={}", id);
        OptimizationSuggestionEntity suggestion = optimizationSuggestionService.resetSuggestionStatus(id);
        if (suggestion == null) {
            return Result.error("优化建议不存在: " + id);
        }
        return Result.success(suggestion);
    }

    @GetMapping("/report/{scheduleResultId}")
    @Operation(summary = "获取优化建议报告", description = "获取指定排程结果的全部优化建议")
    public Result<List<OptimizationSuggestionEntity>> getSuggestionReport(@PathVariable Long scheduleResultId) {
        logger.info("获取优化建议报告: scheduleResultId={}", scheduleResultId);
        List<OptimizationSuggestionEntity> suggestions = optimizationSuggestionService.getSuggestionReport(scheduleResultId);
        return Result.success(suggestions);
    }

    @PostMapping("/analyze/{scheduleResultId}")
    @Operation(summary = "重新分析调度", description = "重新分析调度结果")
    public Result<List<OptimizationSuggestionEntity>> reanalyzeSchedule(@PathVariable Long scheduleResultId) {
        logger.info("重新分析调度: scheduleResultId={}", scheduleResultId);
        List<OptimizationSuggestionEntity> suggestions = optimizationSuggestionService.reanalyzeSchedule(scheduleResultId);
        return Result.success(suggestions);
    }

    @PostMapping
    @Operation(summary = "创建优化建议", description = "创建新的优化建议")
    public Result<OptimizationSuggestionEntity> createOptimizationSuggestion(@RequestBody OptimizationSuggestionEntity suggestion) {
        logger.info("创建优化建议");
        OptimizationSuggestionEntity createdSuggestion = optimizationSuggestionService.createOptimizationSuggestion(suggestion);
        return Result.success(createdSuggestion);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新优化建议", description = "根据ID更新优化建议")
    public Result<OptimizationSuggestionEntity> updateOptimizationSuggestion(
            @PathVariable Long id,
            @RequestBody OptimizationSuggestionEntity suggestion) {
        logger.info("更新优化建议: id={}", id);
        OptimizationSuggestionEntity updatedSuggestion = optimizationSuggestionService.updateOptimizationSuggestion(id, suggestion);
        return Result.success(updatedSuggestion);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除优化建议", description = "根据ID删除优化建议")
    public Result<Void> deleteOptimizationSuggestion(@PathVariable Long id) {
        logger.info("删除优化建议: id={}", id);
        optimizationSuggestionService.deleteOptimizationSuggestion(id);
        return Result.success();
    }
}
