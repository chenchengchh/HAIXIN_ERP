package com.hxcoe.les.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesVehicleLocationDto;
import com.hxcoe.les.entity.LesAnomalyEventEntity;
import com.hxcoe.les.entity.LesMonitorLogEntity;
import com.hxcoe.les.service.LesMonitoringService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/les/monitoring", "/les/monitoring"})
public class LesMonitoringController {

    @Autowired
    private LesMonitoringService monitoringService;

    /**
     * 获取在途监控日志
     */
    @GetMapping("/logs")
    public Result<PageResult<LesMonitorLogEntity>> fetchMonitorLogs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "planId", required = false) Long planId,
            @RequestParam(value = "vehicleId", required = false) Long vehicleId
    ) {
        return monitoringService.fetchMonitorLogs(page, size, planId, vehicleId);
    }

    /**
     * 获取指定运输计划的监控日志
     */
    @GetMapping("/plans/{planId}/logs")
    public Result<PageResult<LesMonitorLogEntity>> fetchPlanMonitorLogs(
            @PathVariable("planId") Long planId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return monitoringService.fetchPlanMonitorLogs(planId, page, size);
    }

    /**
     * 获取异常事件列表
     */
    @GetMapping("/anomalies")
    public Result<PageResult<LesAnomalyEventEntity>> fetchAnomalyEvents(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "planId", required = false) Long planId,
            @RequestParam(value = "vehicleId", required = false) Long vehicleId,
            @RequestParam(value = "status", required = false) String status
    ) {
        return monitoringService.fetchAnomalyEvents(page, size, planId, vehicleId, status);
    }

    /**
     * 处理异常事件
     */
    @PutMapping("/anomalies/{id}")
    public Result<LesAnomalyEventEntity> handleAnomalyEvent(@PathVariable("id") Long id, @RequestBody HandleAnomalyReq req) {
        String handlingStatus = req == null ? null : req.getHandlingStatus();
        String handlingResult = req == null ? null : req.getHandlingResult();
        return monitoringService.handleAnomalyEvent(id, handlingStatus, handlingResult);
    }

    /**
     * 获取车辆实时位置
     */
    @GetMapping("/vehicle-locations")
    public Result<List<LesVehicleLocationDto>> fetchVehicleLocations(@RequestParam(value = "vehicleIds", required = false) List<Long> vehicleIds) {
        return monitoringService.fetchVehicleLocations(vehicleIds);
    }

    /**
     * 获取车辆历史轨迹
     */
    @GetMapping("/vehicles/{vehicleId}/history")
    public Result<List<LesVehicleLocationDto>> fetchVehicleHistory(
            @PathVariable("vehicleId") Long vehicleId,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime
    ) {
        return monitoringService.fetchVehicleHistory(vehicleId, startTime, endTime);
    }

    /**
     * 获取运输计划轨迹回放
     */
    @GetMapping("/plans/{planId}/track")
    public Result<List<LesVehicleLocationDto>> fetchPlanTrack(
            @PathVariable("planId") Long planId,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime
    ) {
        return monitoringService.fetchPlanTrack(planId, startTime, endTime);
    }

    @Data
    public static class HandleAnomalyReq {
        private String handlingResult;
        private String handlingStatus;
    }
}

