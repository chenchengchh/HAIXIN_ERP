package com.hxcoe.les.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesLogisticsAnalysisDto;
import com.hxcoe.les.dto.LesTransportStatsDto;
import com.hxcoe.les.entity.LesServiceQualityEntity;
import com.hxcoe.les.entity.LesTransportCostEntity;
import com.hxcoe.les.service.LesAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/les/analysis", "/les/analysis"})
public class LesAnalysisController {

    @Autowired
    private LesAnalysisService analysisService;

    /**
     * 获取运输成本列表
     */
    @GetMapping("/costs")
    public Result<PageResult<LesTransportCostEntity>> fetchTransportCosts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "planId", required = false) Long planId
    ) {
        return analysisService.fetchTransportCosts(page, size, planId);
    }

    /**
     * 获取运输计划成本
     */
    @GetMapping("/plans/{planId}/cost")
    public Result<LesTransportCostEntity> getPlanCost(@PathVariable("planId") Long planId) {
        return analysisService.getPlanCost(planId);
    }

    /**
     * 获取物流分析数据
     */
    @GetMapping("/logistics")
    public Result<PageResult<LesLogisticsAnalysisDto>> fetchLogisticsAnalysis(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return analysisService.fetchLogisticsAnalysis(page, size, startDate, endDate);
    }

    /**
     * 获取服务质量评估列表
     */
    @GetMapping("/service-qualities")
    public Result<PageResult<LesServiceQualityEntity>> fetchServiceQualities(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return analysisService.fetchServiceQualities(page, size, startDate, endDate);
    }

    /**
     * 获取运输统计数据
     */
    @GetMapping("/stats")
    public Result<LesTransportStatsDto> fetchTransportStats(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return analysisService.fetchTransportStats(startDate, endDate);
    }

    /**
     * 获取运输效率分析
     */
    @GetMapping("/efficiency")
    public Result<Object> fetchEfficiencyAnalysis(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return analysisService.fetchEfficiencyAnalysis(startDate, endDate);
    }

    /**
     * 获取资源利用率分析
     */
    @GetMapping("/utilization")
    public Result<Object> fetchUtilizationAnalysis(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return analysisService.fetchUtilizationAnalysis(startDate, endDate);
    }

    /**
     * 获取异常事件分析
     */
    @GetMapping("/anomalies")
    public Result<Object> fetchAnomalyAnalysis(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return analysisService.fetchAnomalyAnalysis(startDate, endDate);
    }

    /**
     * 导出分析报告（CSV）
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAnalysisReport(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        Result<PageResult<LesLogisticsAnalysisDto>> response = analysisService.fetchLogisticsAnalysis(1, 1000, startDate, endDate);
        List<LesLogisticsAnalysisDto> rows = response.getData() == null ? List.of() : response.getData().getRecords();
        StringBuilder csv = new StringBuilder();
        csv.append("planId,plannedDuration,actualDuration,delayMinutes,vehicleUtilization,driverUtilization,costPerKm,createTime\n");
        for (LesLogisticsAnalysisDto row : rows) {
            csv.append(row.getPlanId()).append(',')
                    .append(row.getPlannedDuration()).append(',')
                    .append(row.getActualDuration()).append(',')
                    .append(row.getDelayMinutes()).append(',')
                    .append(row.getVehicleUtilization()).append(',')
                    .append(row.getDriverUtilization()).append(',')
                    .append(row.getCostPerKm()).append(',')
                    .append(row.getCreateTime() == null ? "" : row.getCreateTime())
                    .append('\n');
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"les-analysis.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }
}

