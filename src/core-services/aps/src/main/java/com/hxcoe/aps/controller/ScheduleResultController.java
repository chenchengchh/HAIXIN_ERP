package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ProductionPlanEntity;
import com.hxcoe.aps.entity.ScheduleDetailEntity;
import com.hxcoe.aps.entity.ScheduleResultEntity;
import com.hxcoe.aps.enums.SchedulingAlgorithm;
import com.hxcoe.aps.repository.ProductionPlanRepository;
import com.hxcoe.aps.repository.ScheduleDetailRepository;
import com.hxcoe.aps.service.ScheduleResultService;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hxcoe.aps.service.SchedulingEngineService;

@RestController
@RequestMapping("/api/v1/aps/schedule-results")
@Tag(name = "排程结果管理", description = "排程结果管理相关接口")
public class ScheduleResultController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleResultController.class);

    @Autowired
    private ScheduleResultService scheduleResultService;

    @Autowired
    private SchedulingEngineService schedulingEngineService;

    @Autowired
    private ScheduleDetailRepository scheduleDetailRepository;

    @Autowired
    private ProductionPlanRepository productionPlanRepository;

    /**
     * 生成排程结果：接收前端排程参数，调用排程引擎执行计算。
     * 请求体：{ planId, algorithm, objectives, params }
     */
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "生成排程结果", description = "根据计划ID与算法参数执行排程计算")
    public Result<ScheduleResultEntity> generateSchedule(@RequestBody Map<String, Object> request) {
        try {
            Long planId = Long.valueOf(String.valueOf(request.get("planId")));
            String algorithmStr = request.get("algorithm") != null ? String.valueOf(request.get("algorithm")) : "HEURISTIC";
            @SuppressWarnings("unchecked")
            List<String> objectives = request.get("objectives") instanceof List
                    ? (List<String>) request.get("objectives") : List.of();

            // 算法名称容错：非法值回退HEURISTIC
            SchedulingAlgorithm algorithm;
            try {
                algorithm = SchedulingAlgorithm.valueOf(algorithmStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warn("未知算法[{}]，回退为HEURISTIC", algorithmStr);
                algorithm = SchedulingAlgorithm.HEURISTIC;
            }

            logger.info("生成排程结果: planId={}, algorithm={}, objectives={}", planId, algorithm, objectives);
            ScheduleResultEntity result = schedulingEngineService.executeScheduling(planId, algorithm, objectives);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("生成排程结果失败", e);
            return Result.error("生成排程结果失败: " + e.getMessage());
        }
    }

    /**
     * 获取排程结果的甘特图数据：将排程详情转换为前端甘特图组件所需的{tasks, links}结构。
     */
    @GetMapping(value = "/{id}/gantt-data", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "获取甘特图数据", description = "获取排程结果的甘特图任务与依赖数据")
    public Result<Map<String, Object>> getGanttData(@PathVariable Long id) {
        try {
            logger.info("获取甘特图数据: resultId={}", id);
            ScheduleResultEntity result = scheduleResultService.getScheduleResultById(id);
            if (result == null) {
                return Result.error("排程结果不存在: " + id);
            }

            ProductionPlanEntity plan = productionPlanRepository.findById(result.getPlanId()).orElse(null);
            String productName = plan != null && plan.getProductName() != null ? plan.getProductName() : "生产任务";

            List<ScheduleDetailEntity> details = scheduleDetailRepository.findByScheduleResultId(id);

            // 排程详情 -> 甘特任务
            List<Map<String, Object>> tasks = new ArrayList<>();
            for (ScheduleDetailEntity d : details) {
                Map<String, Object> task = new HashMap<>();
                task.put("id", d.getId());
                task.put("text", productName + "-" + d.getResourceName());
                task.put("start_date", d.getStartTime());
                task.put("end_date", d.getEndTime());
                // duration单位：小时（与前端duration_unit:'hour'一致）。
                // 按分钟换算保留2位小数，避免toHours()截断导致85分钟任务显示为1小时
                double hours = d.getStartTime() != null && d.getEndTime() != null
                        ? Duration.between(d.getStartTime(), d.getEndTime()).toMinutes() / 60.0 : 1.0;
                task.put("duration", Math.round(Math.max(0.01, hours) * 100.0) / 100.0);
                task.put("resource_name", d.getResourceName());
                task.put("resource_id", d.getResourceId());
                task.put("quantity", d.getQuantity());
                // 状态映射：SCHEDULED->planned, RELEASED->in_progress
                task.put("status", "RELEASED".equals(d.getStatus()) ? "in_progress" : "planned");
                task.put("priority", 1);
                tasks.add(task);
            }

            // 依赖链：按(开始时间,结束时间)分组，同组为同工序并行资源不互链；
            // 相邻工序组之间全部首尾相连（finish-to-start, type=0），还原工艺路线的真实依赖
            List<Map<String, Object>> links = new ArrayList<>();
            Map<String, List<ScheduleDetailEntity>> groups = new LinkedHashMap<>();
            details.stream()
                    .filter(d -> d.getStartTime() != null && d.getEndTime() != null)
                    .sorted(Comparator.comparing(ScheduleDetailEntity::getStartTime)
                            .thenComparing(ScheduleDetailEntity::getId))
                    .forEach(d -> groups.computeIfAbsent(d.getStartTime() + "|" + d.getEndTime(), k -> new ArrayList<>()).add(d));
            List<List<ScheduleDetailEntity>> groupList = new ArrayList<>(groups.values());
            int linkId = 1;
            for (int g = 1; g < groupList.size(); g++) {
                for (ScheduleDetailEntity prev : groupList.get(g - 1)) {
                    for (ScheduleDetailEntity next : groupList.get(g)) {
                        Map<String, Object> link = new HashMap<>();
                        link.put("id", linkId++);
                        link.put("source", prev.getId());
                        link.put("target", next.getId());
                        link.put("type", 0);
                        links.add(link);
                    }
                }
            }

            Map<String, Object> ganttData = new HashMap<>();
            ganttData.put("tasks", tasks);
            ganttData.put("links", links);
            ganttData.put("scheduleNo", result.getScheduleNo());
            ganttData.put("resultStatus", result.getStatus());
            return Result.success(ganttData);
        } catch (Exception e) {
            logger.error("获取甘特图数据失败", e);
            return Result.error("获取甘特图数据失败: " + e.getMessage());
        }
    }

    // ... (其他方法保持不变)

    @PostMapping("/{id}/release")
    @Operation(summary = "下达排程结果", description = "将排程结果下达至MES生成工单")
    public Result<Void> releaseScheduleResult(@PathVariable Long id) {
        try {
            logger.info("下达排程结果: id={}", id);
            schedulingEngineService.releaseSchedule(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("下达排程结果失败", e);
            return Result.error("下达排程结果失败: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "获取所有排程结果", description = "获取所有排程结果列表")
    public Result<List<ScheduleResultEntity>> getScheduleResults() {
        try {
            logger.info("获取所有排程结果");
            List<ScheduleResultEntity> results = scheduleResultService.getAllScheduleResults();
            return Result.success(results);
        } catch (Exception e) {
            logger.error("获取所有排程结果失败", e);
            return Result.error("获取所有排程结果失败: " + e.getMessage());
        }
    }

    @GetMapping(value = "/page", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "分页获取排程结果", description = "分页查询排程结果")
    public Result<PageResult<ScheduleResultEntity>> getScheduleResultsByPage(
            @Parameter(description = "页码，默认1", required = false) @RequestParam(defaultValue = "1", name = "page") int page,
            @Parameter(description = "每页数量，默认10", required = false) @RequestParam(defaultValue = "10", name = "size") int size,
            @Parameter(description = "计划编号", required = false) @RequestParam(required = false, name = "scheduleNo") String scheduleNo,
            @Parameter(description = "使用算法", required = false) @RequestParam(required = false, name = "algorithm") String algorithm) {
        try {
            logger.info("分页获取排程结果: page={}, size={}, scheduleNo={}, algorithm={}", page, size, scheduleNo, algorithm);
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            var pageResult = scheduleResultService.getScheduleResultsByPage(pageable, scheduleNo, algorithm);
            PageResult<ScheduleResultEntity> result = PageResult.build(
                    pageResult.getTotalElements(),
                    pageResult.getSize(),
                    pageResult.getNumber() + 1,
                    pageResult.getContent()
            );
            return Result.success(result);
        } catch (Exception e) {
            logger.error("分页获取排程结果失败", e);
            return Result.error("分页获取排程结果失败: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "查询排程结果", description = "根据ID查询排程结果")
    public Result<ScheduleResultEntity> getScheduleResultById(
            @Parameter(description = "结果ID", required = true) @PathVariable Long id) {
        try {
            logger.info("查询排程结果: id={}", id);
            ScheduleResultEntity result = scheduleResultService.getScheduleResultById(id);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("查询排程结果失败", e);
            return Result.error("查询排程结果失败: " + e.getMessage());
        }
    }

    @GetMapping(value = "/plan/{planId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "根据计划ID查询结果", description = "根据计划ID查询排程结果")
    public Result<List<ScheduleResultEntity>> getScheduleResultsByPlanId(
            @Parameter(description = "计划ID", required = true) @PathVariable Long planId) {
        try {
            logger.info("根据计划ID查询结果: planId={}", planId);
            List<ScheduleResultEntity> results = scheduleResultService.getScheduleResultsByPlanId(planId);
            return Result.success(results);
        } catch (Exception e) {
            logger.error("根据计划ID查询结果失败", e);
            return Result.error("根据计划ID查询结果失败: " + e.getMessage());
        }
    }

    @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "根据状态查询结果", description = "根据状态查询排程结果")
    public Result<List<ScheduleResultEntity>> getScheduleResultsByStatus(
            @Parameter(description = "状态", required = true) @PathVariable String status) {
        try {
            logger.info("根据状态查询结果: status={}", status);
            List<ScheduleResultEntity> results = scheduleResultService.getScheduleResultsByStatus(status);
            return Result.success(results);
        } catch (Exception e) {
            logger.error("根据状态查询结果失败", e);
            return Result.error("根据状态查询结果失败: " + e.getMessage());
        }
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "创建排程结果", description = "创建新的排程结果")
    public Result<ScheduleResultEntity> createScheduleResult(@RequestBody ScheduleResultEntity scheduleResult) {
        try {
            logger.info("创建排程结果");
            ScheduleResultEntity createdResult = scheduleResultService.createScheduleResult(scheduleResult);
            return Result.success(createdResult);
        } catch (Exception e) {
            logger.error("创建排程结果失败", e);
            return Result.error("创建排程结果失败: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "更新排程结果", description = "根据ID更新排程结果")
    public Result<ScheduleResultEntity> updateScheduleResult(
            @Parameter(description = "结果ID", required = true) @PathVariable Long id,
            @RequestBody ScheduleResultEntity scheduleResult) {
        try {
            logger.info("更新排程结果: id={}", id);
            ScheduleResultEntity updatedResult = scheduleResultService.updateScheduleResult(id, scheduleResult);
            return Result.success(updatedResult);
        } catch (Exception e) {
            logger.error("更新排程结果失败", e);
            return Result.error("更新排程结果失败: " + e.getMessage());
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "删除排程结果", description = "根据ID删除排程结果")
    public Result<Void> deleteScheduleResult(
            @Parameter(description = "结果ID", required = true) @PathVariable Long id) {
        try {
            logger.info("删除排程结果: id={}", id);
            scheduleResultService.deleteScheduleResult(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除排程结果失败", e);
            return Result.error("删除排程结果失败: " + e.getMessage());
        }
    }
}
