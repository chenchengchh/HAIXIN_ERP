package com.hxcoe.eam.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.eam.entity.AssetKpiEntity;
import com.hxcoe.eam.service.AssetKpiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/eam/analysis")
public class AssetKpiController {

    @Autowired
    private AssetKpiService kpiService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        return Result.success(kpiService.getDashboardData());
    }

    @GetMapping("/oee-trend")
    /**
     * 获取最近N个月OEE趋势数据
     * @param months 最近月份数
     * @return 趋势列表
     */
    public Result<List<Map<String, Object>>> getOeeTrend(@RequestParam(value = "months", required = false) Integer months) {
        return Result.success(kpiService.getOeeTrend(months != null ? months : 12));
    }

    @GetMapping("/fault-summary")
    /**
     * 获取故障分析汇总数据
     * @param start 开始日期（YYYY-MM-DD）
     * @param end 结束日期（YYYY-MM-DD）
     * @return 汇总数据
     */
    public Result<Map<String, Object>> getFaultSummary(
        @RequestParam(value = "start", required = false) String start,
        @RequestParam(value = "end", required = false) String end
    ) {
        try {
            LocalDate startDate = start != null && !start.isBlank() ? LocalDate.parse(start) : null;
            LocalDate endDate = end != null && !end.isBlank() ? LocalDate.parse(end) : null;
            return Result.success(kpiService.getFaultSummary(startDate, endDate));
        } catch (DateTimeParseException ex) {
            return Result.fail("日期参数格式错误，请使用YYYY-MM-DD");
        }
    }

    @GetMapping("/cost-summary")
    /**
     * 获取维护成本分析汇总数据
     * @param start 开始日期（YYYY-MM-DD）
     * @param end 结束日期（YYYY-MM-DD）
     * @return 汇总数据
     */
    public Result<Map<String, Object>> getCostSummary(
        @RequestParam(value = "start", required = false) String start,
        @RequestParam(value = "end", required = false) String end
    ) {
        try {
            LocalDate startDate = start != null && !start.isBlank() ? LocalDate.parse(start) : null;
            LocalDate endDate = end != null && !end.isBlank() ? LocalDate.parse(end) : null;
            return Result.success(kpiService.getCostSummary(startDate, endDate));
        } catch (DateTimeParseException ex) {
            return Result.fail("日期参数格式错误，请使用YYYY-MM-DD");
        }
    }
    
    @PostMapping("/kpi")
    public Result<AssetKpiEntity> createKpi(@RequestBody AssetKpiEntity kpi) {
        return Result.success(kpiService.saveKpi(kpi));
    }
}
