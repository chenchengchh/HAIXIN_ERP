package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.DeviationAlertEntity;
import com.hxcoe.aps.service.DeviationAlertService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/aps/monitoring")
@Tag(name = "计划监控", description = "计划监控相关接口")
public class PlanMonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(PlanMonitoringController.class);

    @Autowired
    private DeviationAlertService deviationAlertService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取APS仪表盘全局统计数据（不限单个计划）。
     * 注意：响应字段禁止使用total命名，避免前端normalizeResponse误判为分页数据。
     * 统计口径：
     * - scheduleRate：排程达成率 = 已排程计划数 / 计划总数 * 100
     * - resourceUtilization：资源利用率 = 有排程任务的资源数 / 可排程资源总数 * 100
     * - pendingPlans：待处理计划数（状态为草稿）
     * - activeAlerts：异常预警数（未处理/未忽略的偏差预警）
     */
    @GetMapping("/dashboard/stats")
    @Operation(summary = "获取仪表盘统计", description = "获取APS仪表盘全局统计数据")
    public Result<Map<String, Object>> getDashboardStats() {
        logger.info("获取仪表盘统计");
        Map<String, Object> stats = new HashMap<>();
        try {
            // 1. 计划总数与已排程数（状态SCHEDULED视为已排程）
            Long planCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM aps_production_plan", Long.class);
            Long scheduledCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM aps_production_plan WHERE status = 'SCHEDULED'", Long.class);
            double scheduleRate = planCount != null && planCount > 0
                    ? Math.round((scheduledCount != null ? scheduledCount : 0) * 1000.0 / planCount) / 10.0 : 0.0;

            // 2. 资源利用率：有SCHEDULED/RELEASED详情的资源数 / 设备+生产线资源总数
            Long resourceCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM aps_resource WHERE type IN ('设备', '生产线')", Long.class);
            Long busyResourceCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT resource_id) FROM aps_schedule_detail WHERE status IN ('SCHEDULED', 'RELEASED')",
                    Long.class);
            double resourceUtilization = resourceCount != null && resourceCount > 0
                    ? Math.round((busyResourceCount != null ? busyResourceCount : 0) * 1000.0 / resourceCount) / 10.0 : 0.0;

            // 3. 待处理计划数（草稿状态）
            Long pendingPlans = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM aps_production_plan WHERE status = '草稿'", Long.class);

            // 4. 异常预警数（未处理且未忽略）
            Long activeAlerts = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM aps_deviation_alert WHERE status NOT IN ('已处理', '已忽略', 'processed', 'dismissed')",
                    Long.class);

            stats.put("scheduleRate", scheduleRate);
            stats.put("resourceUtilization", resourceUtilization);
            stats.put("pendingPlans", pendingPlans != null ? pendingPlans : 0);
            stats.put("activeAlerts", activeAlerts != null ? activeAlerts : 0);
            stats.put("planCount", planCount != null ? planCount : 0);
            stats.put("scheduledCount", scheduledCount != null ? scheduledCount : 0);
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取仪表盘统计失败", e);
            return Result.error("获取仪表盘统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/plan-compare")
    @Operation(summary = "获取计划与实际对比数据", description = "获取计划与实际对比数据")
    public Result<Map<String, Object>> getPlanCompareData(
            @RequestParam(name = "planId") Long planId,
            @RequestParam(name = "startTime", required = false) String startTime,
            @RequestParam(name = "endTime", required = false) String endTime) {
        logger.info("获取计划与实际对比数据: planId={}, startTime={}, endTime={}", planId, startTime, endTime);
        Map<String, Object> compareData = deviationAlertService.getPlanCompareData(planId, startTime, endTime);
        return Result.success(compareData);
    }

    @GetMapping("/progress-tracking")
    @Operation(summary = "获取进度跟踪数据", description = "获取进度跟踪数据")
    public Result<List<Map<String, Object>>> getProgressTrackingData(
            @RequestParam(name = "planId") Long planId,
            @RequestParam(name = "orderId", required = false) Long orderId,
            @RequestParam(name = "status", required = false) String status) {
        logger.info("获取进度跟踪数据: planId={}, orderId={}, status={}", planId, orderId, status);
        List<Map<String, Object>> trackingData = deviationAlertService.getProgressTrackingData(planId, orderId, status);
        return Result.success(trackingData);
    }

    @GetMapping("/deviation-alerts")
    @Operation(summary = "获取偏差预警数据", description = "获取偏差预警数据")
    public Result<List<DeviationAlertEntity>> getDeviationAlerts(
            @RequestParam(name = "planId", required = false) Long planId,
            @RequestParam(name = "status", required = false) String status) {
        logger.info("获取偏差预警数据: planId={}, status={}", planId, status);
        List<DeviationAlertEntity> alerts = deviationAlertService.getDeviationAlerts(planId, null, status);
        return Result.success(alerts);
    }

    @GetMapping("/kpi-metrics")
    @Operation(summary = "获取KPI指标", description = "获取关键绩效指标")
    public Result<Map<String, Object>> getKpiMetrics(
            @RequestParam(name = "planId") Long planId,
            @RequestParam(name = "startTime", required = false) String startTime,
            @RequestParam(name = "endTime", required = false) String endTime) {
        logger.info("获取KPI指标: planId={}, startTime={}, endTime={}", planId, startTime, endTime);
        Map<String, Object> metrics = deviationAlertService.getKpiMetrics(planId, startTime, endTime);
        return Result.success(metrics);
    }

    @PostMapping("/deviation-alerts")
    @Operation(summary = "创建偏差预警", description = "创建新的偏差预警")
    public Result<DeviationAlertEntity> createDeviationAlert(@RequestBody DeviationAlertEntity alert) {
        logger.info("创建偏差预警");
        DeviationAlertEntity createdAlert = deviationAlertService.createDeviationAlert(alert);
        return Result.success(createdAlert);
    }
}
