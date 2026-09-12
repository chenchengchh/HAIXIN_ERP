package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scm.entity.ForecastConfigEntity;
import com.hxcoe.scm.entity.ForecastDataSourceEntity;
import com.hxcoe.scm.service.ForecastConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/scm/forecast", "/api/scm/forecast"})
public class ForecastConfigController {

    @Autowired
    private ForecastConfigService forecastConfigService;

    @GetMapping("/config")
    public ApiResponse<ForecastConfigEntity> getConfig() {
        return success("查询成功", forecastConfigService.getConfig());
    }

    @PutMapping("/config")
    public ApiResponse<ForecastConfigEntity> saveConfig(@RequestBody ForecastConfigEntity config) {
        return success("保存成功", forecastConfigService.saveConfig(config));
    }

    @GetMapping("/data-sources")
    public ApiResponse<List<ForecastDataSourceEntity>> listDataSources() {
        return success("查询成功", forecastConfigService.listDataSources());
    }

    @PostMapping("/data-sources")
    public ApiResponse<ForecastDataSourceEntity> createDataSource(@RequestBody ForecastDataSourceEntity dataSource) {
        return success("创建成功", forecastConfigService.createDataSource(dataSource));
    }

    @PutMapping("/data-sources/{id}")
    public ApiResponse<ForecastDataSourceEntity> updateDataSource(@PathVariable Long id, @RequestBody ForecastDataSourceEntity dataSource) {
        return success("更新成功", forecastConfigService.updateDataSource(id, dataSource));
    }

    @DeleteMapping("/data-sources/{id}")
    public ApiResponse<Void> deleteDataSource(@PathVariable Long id) {
        forecastConfigService.deleteDataSource(id);
        return success("删除成功", null);
    }

    @PostMapping("/data-sources/{id}/test")
    public ApiResponse<ForecastDataSourceEntity> testDataSource(@PathVariable Long id) {
        return success("测试完成", forecastConfigService.testDataSource(id));
    }

    @PostMapping("/model/test")
    public ApiResponse<Map<String, Object>> testModel(@RequestParam(required = false) String period) {
        return success("测试完成", forecastConfigService.testModel(period));
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }
}

