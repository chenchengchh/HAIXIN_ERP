package com.hxcoe.qms.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.InspectionResultEntity;
import com.hxcoe.qms.entity.NcRegistrationEntity;
import com.hxcoe.qms.repository.InspectionResultRepository;
import com.hxcoe.qms.repository.NcRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 质量统计分析控制器
 */
@RestController
@RequestMapping("/api/v1/qms/statistical-analysis")
public class StatisticalAnalysisController {

    @Autowired
    private NcRegistrationRepository ncRegistrationRepository;

    @Autowired
    private InspectionResultRepository inspectionResultRepository;

    /**
     * 获取质量统计数据
     *
     * @param dataType 数据类型
     * @param period 统计周期
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @GetMapping
    public Result<Map<String, Object>> getStatisticalData(
            @RequestParam String dataType,
            @RequestParam String period,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        LocalDateTime start = parseDateTimeOrNull(startTime);
        LocalDateTime end = parseDateTimeOrNull(endTime);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("dataType", dataType);
        metrics.put("period", period);
        metrics.put("startTime", startTime);
        metrics.put("endTime", endTime);

        Map<String, Object> result = new HashMap<>();
        result.put("id", UUID.randomUUID().toString());
        result.put("dataType", dataType);
        result.put("period", period);
        result.put("metrics", metrics);
        result.put("createTime", LocalDateTime.now().toString());
        return Result.success(result);
    }

    /**
     * 获取帕累托图数据
     *
     * @param category 分类字段
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 帕累托图数据
     */
    @GetMapping("/pareto")
    public Result<Map<String, Object>> getParetoData(
            @RequestParam String category,
            @RequestParam String startTime,
            @RequestParam String endTime
    ) {
        LocalDateTime start = parseDateTimeOrNull(startTime);
        LocalDateTime end = parseDateTimeOrNull(endTime);

        List<NcRegistrationEntity> records = ncRegistrationRepository.findAll();
        Map<String, Long> countBy = new HashMap<>();
        for (NcRegistrationEntity r : records) {
            LocalDateTime t = r.getDiscoveryTime();
            if (start != null && t != null && t.isBefore(start)) {
                continue;
            }
            if (end != null && t != null && t.isAfter(end)) {
                continue;
            }
            String key = Objects.equals(category, "defectType") ? r.getDefectType() : r.getDefectLevel();
            if (key == null || key.isBlank()) {
                key = "unknown";
            }
            countBy.put(key, countBy.getOrDefault(key, 0L) + 1);
        }

        List<Map.Entry<String, Long>> sorted = countBy.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .toList();
        long total = sorted.stream().mapToLong(Map.Entry::getValue).sum();
        double cumulative = 0.0;
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, Long> e : sorted) {
            double percent = total == 0 ? 0.0 : (e.getValue() * 100.0 / total);
            cumulative += percent;
            Map<String, Object> item = new HashMap<>();
            item.put("name", e.getKey());
            item.put("count", e.getValue());
            item.put("percent", round(percent));
            item.put("cumulativePercent", round(cumulative));
            items.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("category", category);
        // 注意：禁止使用"total"作为字段名，前端DataTransformer会将含data.total的响应误判为分页数据，导致items字段丢失
        result.put("totalCount", total);
        result.put("items", items);
        return Result.success(result);
    }

    /**
     * 获取SPC控制图数据
     *
     * @param chartType 图表类型
     * @param productId 产品ID
     * @param parameter 参数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param sampleSize 样本量
     * @return SPC数据
     */
    @GetMapping("/spc")
    public Result<Map<String, Object>> getSpcData(
            @RequestParam String chartType,
            @RequestParam String productId,
            @RequestParam String parameter,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) Integer sampleSize
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("chartType", chartType);
        result.put("productId", productId);
        result.put("parameter", parameter);
        result.put("sampleSize", sampleSize == null ? 10 : sampleSize);
        result.put("ucl", 9.5);
        result.put("cl", 7.0);
        result.put("lcl", 4.5);
        result.put("points", List.of(6.8, 7.2, 6.6, 7.4, 7.0, 6.9, 7.1));
        return Result.success(result);
    }

    /**
     * 获取合格率数据
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param groupBy 聚合粒度
     * @return 合格率数据
     */
    @GetMapping("/pass-rate")
    public Result<Map<String, Object>> getPassRateData(
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam String groupBy
    ) {
        LocalDateTime start = parseDateTimeOrNull(startTime);
        LocalDateTime end = parseDateTimeOrNull(endTime);

        List<InspectionResultEntity> results = inspectionResultRepository.findAll();
        long total = 0;
        long qualified = 0;
        for (InspectionResultEntity r : results) {
            LocalDateTime t = r.getInspectionTime();
            if (start != null && t != null && t.isBefore(start)) {
                continue;
            }
            if (end != null && t != null && t.isAfter(end)) {
                continue;
            }
            total++;
            if ("qualified".equalsIgnoreCase(r.getInspectionResult())) {
                qualified++;
            }
        }

        double passRate = total == 0 ? 0.0 : (qualified * 100.0 / total);
        Map<String, Object> result = new HashMap<>();
        result.put("groupBy", groupBy);
        // 注意：禁止使用"total"作为字段名，前端DataTransformer会将含data.total的响应误判为分页数据，导致series等字段丢失
        result.put("totalCount", total);
        result.put("qualified", qualified);
        result.put("passRate", round(passRate));
        result.put("series", List.of(Map.of("label", "总合格率", "value", round(passRate))));
        return Result.success(result);
    }

    private static LocalDateTime parseDateTimeOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        );
        for (DateTimeFormatter f : formatters) {
            try {
                return LocalDateTime.parse(v, f);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
