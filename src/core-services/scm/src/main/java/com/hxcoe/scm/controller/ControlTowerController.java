package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scm.service.ControlTowerService;
import com.hxcoe.scm.service.ControlTowerTrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/scm/control-tower", "/api/scm/control-tower"})
public class ControlTowerController {

    @Autowired
    private ControlTowerService controlTowerService;

    @Autowired
    private ControlTowerTrendService controlTowerTrendService;

    @GetMapping("/network")
    public ApiResponse<Map<String, Object>> getNetworkData() {
        return success("网络数据查询成功", controlTowerService.getNetworkData());
    }

    @GetMapping("/kpis")
    public ApiResponse<Map<String, Object>> getKpis() {
        return success("KPI查询成功", controlTowerService.getKpis());
    }

    @GetMapping("/kpis/trend")
    public ApiResponse<List<Map<String, Object>>> getKpiTrend(@RequestParam String metric,
                                                              @RequestParam String from,
                                                              @RequestParam String to) {
        controlTowerTrendService.snapshotToday();
        return success("KPI趋势查询成功", controlTowerTrendService.getKpiTrend(metric, from, to));
    }

    @GetMapping("/alerts")
    public ApiResponse<List<Map<String, Object>>> getAlerts() {
        return success("预警查询成功", controlTowerService.getAlerts());
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }
}
