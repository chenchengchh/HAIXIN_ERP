package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.SalesForecast;
import com.hxcoe.crm.service.SalesForecastService;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 销售预测控制器
 */
@RestController
@RequestMapping("/api/v1/crm/forecast")
public class ForecastController {

    @Autowired
    private SalesForecastService salesForecastService;

    /**
     * 按周期查询销售预测
     * @param period 周期（如 2026-Q1 或 2026-07）
     * @return 销售预测列表
     */
    @GetMapping("/period/{period}")
    public Result<List<SalesForecast>> getForecastByPeriod(@PathVariable String period) {
        List<SalesForecast> forecasts = salesForecastService.getForecastByPeriod(period);
        return Result.success(forecasts);
    }

    /**
     * 创建销售预测
     * @param forecast 预测数据
     * @return 创建结果
     */
    @PostMapping
    public Result<SalesForecast> createForecast(@RequestBody SalesForecast forecast) {
        SalesForecast createdForecast = salesForecastService.createForecast(forecast);
        return Result.success("成功", createdForecast);
    }
}
