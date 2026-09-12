package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.DemandForecastEntity;
import com.hxcoe.scm.entity.ForecastVersionEntity;
import com.hxcoe.scm.service.ForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 需求预测管理控制器
 */
@RestController
@RequestMapping({"/api/v1/scm/forecast", "/api/scm/forecast"})
public class ForecastController {

    @Autowired
    private ForecastService forecastService;

    /**
     * 生成预测数据
     * @param period 预测周期 (YYYY-MM)
     */
    @PostMapping("/generate")
    public ApiResponse<List<DemandForecastEntity>> generateForecast(@RequestParam String period) {
        List<DemandForecastEntity> result = forecastService.generateForecast(period);
        return success("预测生成成功", result);
    }

    /**
     * 获取预测列表
     */
    @GetMapping
    public ApiResponse<PageResult<DemandForecastEntity>> getForecastList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String product,
            @RequestParam(required = false) Long versionId) {

        Long resolvedVersionId = versionId;
        if (resolvedVersionId == null && period != null && !period.isEmpty()) {
            List<ForecastVersionEntity> versions = forecastService.getForecastVersions(period);
            Optional<ForecastVersionEntity> published = versions.stream().filter(v -> "PUBLISHED".equals(v.getStatus())).findFirst();
            ForecastVersionEntity target = published.orElseGet(() -> versions.isEmpty() ? null : versions.get(0));
            resolvedVersionId = target == null ? null : target.getId();
        }
        final Long finalVersionId = resolvedVersionId;
        
        Specification<DemandForecastEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (period != null && !period.isEmpty()) {
                predicates.add(cb.equal(root.get("period"), period));
            }
            if (finalVersionId != null) {
                predicates.add(cb.equal(root.get("versionId"), finalVersionId));
            }
            if (region != null && !region.isEmpty()) {
                predicates.add(cb.equal(root.get("region"), region));
            }
            if (product != null && !product.isEmpty()) {
                Predicate p1 = cb.like(root.get("productName"), "%" + product + "%");
                Predicate p2 = cb.like(root.get("productCode"), "%" + product + "%");
                predicates.add(cb.or(p1, p2));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<DemandForecastEntity> pageData = forecastService.getForecastList(spec, pageable);
        
        return success("预测列表查询成功", PageResult.build(
            pageData.getTotalElements(), 
            pageData.getSize(), 
            pageData.getNumber() + 1, 
            pageData.getContent()
        ));
    }

    /**
     * 更新预测调整
     */
    @PutMapping("/{id}")
    public ApiResponse<DemandForecastEntity> updateForecast(@PathVariable Long id, @RequestBody DemandForecastEntity forecast) {
        try {
            DemandForecastEntity updated = forecastService.updateForecast(id, forecast);
            if (updated != null) {
                return success("预测调整已保存", updated);
            } else {
                return notFound("预测记录不存在");
            }
        } catch (IllegalStateException e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/versions")
    public ApiResponse<List<ForecastVersionEntity>> getForecastVersions(@RequestParam String period) {
        return success("版本列表查询成功", forecastService.getForecastVersions(period));
    }

    @PostMapping("/versions")
    public ApiResponse<ForecastVersionEntity> createForecastVersion(
            @RequestParam String period,
            @RequestParam(required = false) Long fromVersionId) {
        ForecastVersionEntity created = forecastService.createForecastVersion(period, fromVersionId);
        return success("版本创建成功", created);
    }

    @PostMapping("/versions/{id}/publish")
    public ApiResponse<ForecastVersionEntity> publishForecastVersion(@PathVariable Long id) {
        ForecastVersionEntity updated = forecastService.publishForecastVersion(id);
        return success("版本发布成功", updated);
    }

    @PostMapping("/versions/{id}/rollback")
    public ApiResponse<ForecastVersionEntity> rollbackForecastVersion(@PathVariable Long id) {
        ForecastVersionEntity created = forecastService.rollbackForecastVersion(id);
        return success("版本回滚成功", created);
    }

    @PutMapping("/versions/{id}/forecasts")
    public ApiResponse<List<DemandForecastEntity>> updateForecastsByVersion(@PathVariable Long id, @RequestBody List<DemandForecastEntity> forecasts) {
        List<DemandForecastEntity> updated = forecastService.updateForecastsByVersion(id, forecasts);
        return success("批量保存成功", updated);
    }

    @GetMapping("/analysis/accuracy")
    public ApiResponse<Map<String, Object>> getForecastAccuracy(
            @RequestParam String period,
            @RequestParam(required = false) Long versionId) {
        return success("准确率分析成功", forecastService.getForecastAccuracy(period, versionId));
    }

    @GetMapping("/analysis/accuracy-trend")
    public ApiResponse<List<Map<String, Object>>> getForecastAccuracyTrend(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) Long versionId) {
        java.time.YearMonth s = java.time.YearMonth.parse(start);
        java.time.YearMonth e = java.time.YearMonth.parse(end);
        List<Map<String, Object>> rows = new ArrayList<>();
        java.time.YearMonth cur = s;
        while (!cur.isAfter(e)) {
            String p = cur.toString();
            Map<String, Object> acc = forecastService.getForecastAccuracy(p, versionId);
            Map<String, Object> row = new java.util.HashMap<>();
            row.put("period", p);
            row.put("mape", acc.get("mape"));
            row.put("avgBias", acc.get("avgBias"));
            rows.add(row);
            cur = cur.plusMonths(1);
        }
        return success("准确率趋势分析成功", rows);
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.error(400, message);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
