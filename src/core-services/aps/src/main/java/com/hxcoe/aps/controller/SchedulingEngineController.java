package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ScheduleResultEntity;
import com.hxcoe.aps.enums.SchedulingAlgorithm;
import com.hxcoe.aps.service.SchedulingEngineService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/aps")
@Tag(name = "排程引擎", description = "排程引擎相关接口")
public class SchedulingEngineController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingEngineController.class);

    @Autowired
    private SchedulingEngineService schedulingEngineService;

    @GetMapping("/engine/algorithms")
    @Operation(summary = "获取所有可用算法", description = "获取所有可用算法列表")
    public Result<List<Map<String, Object>>> getAllAlgorithms() {
        logger.info("获取所有可用算法");
        List<Map<String, Object>> algorithms = new ArrayList<>();
        
        Map<String, Object> algorithm1 = new HashMap<>();
        algorithm1.put("name", "geneticAlgorithm");
        algorithm1.put("displayName", "遗传算法");
        algorithm1.put("description", "基于自然选择和遗传机制的优化算法");
        algorithms.add(algorithm1);
        
        Map<String, Object> algorithm2 = new HashMap<>();
        algorithm2.put("name", "simulatedAnnealing");
        algorithm2.put("displayName", "模拟退火算法");
        algorithm2.put("description", "基于固体退火原理的优化算法");
        algorithms.add(algorithm2);
        
        Map<String, Object> algorithm3 = new HashMap<>();
        algorithm3.put("name", "tabuSearch");
        algorithm3.put("displayName", "禁忌搜索算法");
        algorithm3.put("description", "通过禁忌策略避免局部最优的搜索算法");
        algorithms.add(algorithm3);
        
        return Result.success(algorithms);
    }

    @GetMapping("/engine/algorithms/{algorithmName}")
    @Operation(summary = "获取算法详情", description = "根据算法名称获取算法详情")
    public Result<Map<String, Object>> getAlgorithmDetail(@PathVariable String algorithmName) {
        logger.info("获取算法详情: algorithmName={}", algorithmName);
        Map<String, Object> algorithm = new HashMap<>();
        algorithm.put("name", algorithmName);
        algorithm.put("displayName", "算法名称");
        algorithm.put("description", "算法描述");
        algorithm.put("parameters", new ArrayList<>());
        algorithm.put("defaultObjectives", new String[] {"makespan", "resourceUtilization"});
        
        return Result.success(algorithm);
    }

    /**
     * 执行调度算法：真实调用排程引擎，返回排程结果ID与状态。
     * 请求体：{ algorithmName, planId, params }
     */
    @PostMapping("/engine/execute")
    @Operation(summary = "执行调度算法", description = "执行调度算法")
    public Result<Map<String, Object>> executeAlgorithm(@RequestBody Map<String, Object> data) {
        try {
            Long planId = Long.valueOf(String.valueOf(data.get("planId")));
            String algorithmName = data.get("algorithmName") != null ? String.valueOf(data.get("algorithmName")) : "HEURISTIC";
            logger.info("执行调度算法: algorithmName={}, planId={}", algorithmName, planId);

            // 算法名称映射：前端传geneticAlgorithm/simulatedAnnealing/tabuSearch或枚举名，统一容错
            SchedulingAlgorithm algorithm = mapAlgorithm(algorithmName);

            ScheduleResultEntity scheduleResult = schedulingEngineService.executeScheduling(planId, algorithm, List.of());

            Map<String, Object> result = new HashMap<>();
            result.put("resultId", scheduleResult.getId());
            result.put("scheduleNo", scheduleResult.getScheduleNo());
            result.put("algorithmName", algorithmName);
            result.put("planId", planId);
            result.put("status", scheduleResult.getStatus());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("执行调度算法失败", e);
            return Result.error("执行调度算法失败: " + e.getMessage());
        }
    }

    /**
     * 算法名称映射：兼容前端算法key（geneticAlgorithm等）与枚举名（GENETIC等）
     */
    private SchedulingAlgorithm mapAlgorithm(String algorithmName) {
        if (algorithmName == null) {
            return SchedulingAlgorithm.HEURISTIC;
        }
        String normalized = algorithmName.trim();
        // 前端展示算法key到枚举的映射
        switch (normalized) {
            case "geneticAlgorithm":
                return SchedulingAlgorithm.GENETIC;
            case "simulatedAnnealing":
            case "tabuSearch":
                // 模拟退火/禁忌搜索暂无专用枚举，回退启发式
                return SchedulingAlgorithm.HEURISTIC;
            default:
                try {
                    return SchedulingAlgorithm.valueOf(normalized.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("未知算法[{}]，回退为HEURISTIC", algorithmName);
                    return SchedulingAlgorithm.HEURISTIC;
                }
        }
    }

    /**
     * 执行排程计算：真实调用排程引擎，返回排程结果实体。
     * 请求体：{ planId, algorithm, objectives }
     */
    @PostMapping("/scheduling/execute")
    @Operation(summary = "执行排程计算", description = "执行排程计算")
    public Result<Object> executeScheduling(@RequestBody Map<String, Object> params) {
        try {
            Long planId = Long.valueOf(String.valueOf(params.get("planId")));
            String algorithmName = params.get("algorithm") != null ? String.valueOf(params.get("algorithm")) : "HEURISTIC";
            @SuppressWarnings("unchecked")
            List<String> objectives = params.get("objectives") instanceof List
                    ? (List<String>) params.get("objectives") : List.of();
            logger.info("执行排程计算: planId={}, algorithm={}", planId, algorithmName);

            ScheduleResultEntity result = schedulingEngineService.executeScheduling(
                    planId, mapAlgorithm(algorithmName), objectives);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("执行排程计算失败", e);
            return Result.error("执行排程计算失败: " + e.getMessage());
        }
    }

    @GetMapping("/scheduling/result/{planId}")
    @Operation(summary = "获取排程结果", description = "根据计划ID获取排程结果")
    public Result<Object> getSchedulingResult(@PathVariable Long planId) {
        logger.info("获取排程结果: planId={}", planId);
        return Result.success(schedulingEngineService.getSchedulingResult(planId));
    }

    @PostMapping("/scheduling/validate/{scheduleResultId}")
    @Operation(summary = "验证排程可行性", description = "验证排程可行性")
    public Result<Boolean> validateSchedule(@PathVariable Long scheduleResultId) {
        logger.info("验证排程可行性: scheduleResultId={}", scheduleResultId);
        return Result.success(schedulingEngineService.validateSchedule(scheduleResultId));
    }

    @GetMapping("/scheduling/conflicts/{scheduleResultId}")
    @Operation(summary = "检测排程冲突", description = "检测排程冲突")
    public Result<List<String>> detectConflicts(@PathVariable Long scheduleResultId) {
        logger.info("检测排程冲突: scheduleResultId={}", scheduleResultId);
        return Result.success(schedulingEngineService.detectConflicts(scheduleResultId));
    }

    /**
     * 优化现有排程：调用排程引擎优化逻辑。
     * 请求体：{ scheduleResultId, objectives }
     */
    @PostMapping("/scheduling/optimize")
    @Operation(summary = "优化现有排程", description = "优化现有排程")
    public Result<Object> optimizeSchedule(@RequestBody Map<String, Object> params) {
        try {
            Long scheduleResultId = Long.valueOf(String.valueOf(params.get("scheduleResultId")));
            @SuppressWarnings("unchecked")
            List<String> objectives = params.get("objectives") instanceof List
                    ? (List<String>) params.get("objectives") : List.of();
            logger.info("优化现有排程: scheduleResultId={}", scheduleResultId);
            ScheduleResultEntity result = schedulingEngineService.optimizeSchedule(scheduleResultId, objectives);
            if (result == null) {
                return Result.error("排程结果不存在: " + scheduleResultId);
            }
            return Result.success(result);
        } catch (Exception e) {
            logger.error("优化排程失败", e);
            return Result.error("优化排程失败: " + e.getMessage());
        }
    }

    /**
     * 手动调整任务：调整任务时间或资源。
     * 请求体：{ taskId, startTime, endTime, resourceId }
     */
    @PostMapping("/scheduling/manual-adjust")
    @Operation(summary = "手动调整任务", description = "手动调整任务")
    public Result<Object> manualAdjustTask(@RequestBody Map<String, Object> params) {
        try {
            Long taskId = Long.valueOf(String.valueOf(params.get("taskId")));
            String startTime = params.get("startTime") != null ? String.valueOf(params.get("startTime")) : null;
            String endTime = params.get("endTime") != null ? String.valueOf(params.get("endTime")) : null;
            Long resourceId = params.get("resourceId") != null
                    ? Long.valueOf(String.valueOf(params.get("resourceId"))) : null;
            logger.info("手动调整任务: taskId={}", taskId);
            ScheduleResultEntity result = schedulingEngineService.manualAdjustTask(taskId, startTime, endTime, resourceId);
            if (result == null) {
                return Result.error("任务调整功能暂未开放或任务不存在");
            }
            return Result.success(result);
        } catch (Exception e) {
            logger.error("手动调整任务失败", e);
            return Result.error("手动调整任务失败: " + e.getMessage());
        }
    }
}
