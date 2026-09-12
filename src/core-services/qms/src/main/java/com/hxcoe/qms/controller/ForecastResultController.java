package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.ForecastResultEntity;
import com.hxcoe.qms.service.ForecastResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 质量趋势预测管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/forecast-results")
public class ForecastResultController {

    @Autowired
    private ForecastResultService forecastResultService;

    /**
     * 分页查询预测结果列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param forecastType 预测类型
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<ForecastResultEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String forecastType,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return forecastResultService.page(forecastType, status, pageable);
    }

    /**
     * 获取预测详情
     *
     * @param id 预测ID
     * @return 预测详情
     */
    @GetMapping("/{id}")
    public Result<ForecastResultEntity> getById(@PathVariable Long id) {
        return forecastResultService.getById(id);
    }

    /**
     * 生成预测结果
     *
     * @param body 预测参数
     * @return 生成结果
     */
    @PostMapping("/generate")
    public Result<ForecastResultEntity> generate(@RequestBody Map<String, Object> body) {
        return forecastResultService.generate(body);
    }

    /**
     * 获取预测趋势图数据
     *
     * @param id 预测ID
     * @return 图表数据
     */
    @GetMapping("/{id}/chart-data")
    public Result<Map<String, Object>> getChartData(@PathVariable Long id) {
        return forecastResultService.getChartData(id);
    }
}
