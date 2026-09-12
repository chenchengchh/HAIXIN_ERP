package com.hxcoe.ems.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.ems.dto.ProcessAnomalyRequest;
import com.hxcoe.ems.entity.EnergyAnomalyEntity;
import com.hxcoe.ems.entity.RealTimeDataEntity;
import com.hxcoe.ems.service.EnergyAnomalyService;
import com.hxcoe.ems.service.RealTimeDataService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 能耗分析控制器
 * 处理能耗异常检测相关的API请求
 *
 * @author author
 * @date 2026-01-01
 */
@RestController
@RequestMapping("/api/v1/ems/analysis")
@Validated
public class EnergyAnalysisController {

    private final EnergyAnomalyService energyAnomalyService;
    private final RealTimeDataService realTimeDataService;

    @Autowired
    public EnergyAnalysisController(EnergyAnomalyService energyAnomalyService, RealTimeDataService realTimeDataService) {
        this.energyAnomalyService = energyAnomalyService;
        this.realTimeDataService = realTimeDataService;
    }

    /**
     * 获取能耗异常检测数据
     * GET /api/ems/analysis/anomalies
     *
     * @param energyType 能源类型
     * @param area       区域
     * @param status     状态
     * @return 能耗异常数据列表
     */
    @GetMapping("/anomalies")
    public Result<List<EnergyAnomalyEntity>> getAnomalyDetection(
            @RequestParam(required = false) String energyType,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String status) {
        
        List<EnergyAnomalyEntity> anomalies = energyAnomalyService.getEnergyAnomaliesByConditions(energyType, area, status);
        for (EnergyAnomalyEntity a : anomalies) {
            normalizeAnomaly(a);
        }
        return Result.success(anomalies);
    }

    @GetMapping("/anomalies/page")
    public Result<Map<String, Object>> getAnomalyDetectionPage(
            @RequestParam(required = false) String energyType,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(200) int size
    ) {
        Page<EnergyAnomalyEntity> p = energyAnomalyService.getEnergyAnomaliesPage(
                energyType,
                area,
                status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "detectionTime"))
        );
        for (EnergyAnomalyEntity a : p.getContent()) {
            normalizeAnomaly(a);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", p.getTotalElements());
        data.put("list", p.getContent());
        data.put("page", p.getNumber());
        data.put("size", p.getSize());
        return Result.success(data);
    }

    /**
     * 获取能耗异常详情
     * GET /api/ems/analysis/anomalies/{id}
     *
     * @param id 异常ID
     * @return 能耗异常详情
     */
    @GetMapping("/anomalies/{id}")
    public Result<EnergyAnomalyEntity> getAnomalyDetail(@PathVariable Long id) {
        EnergyAnomalyEntity entity = energyAnomalyService.getEnergyAnomalyById(id).orElse(null);
        if (entity == null) {
            return Result.error(404, "异常记录不存在");
        }
        normalizeAnomaly(entity);
        return Result.success(entity);
    }

    /**
     * 处理能耗异常
     * PUT /api/ems/analysis/anomalies/{id}/process
     *
     * @param id 异常ID
     * @return 处理后的能耗异常
     */
    @PutMapping("/anomalies/{id}/process")
    public Result<EnergyAnomalyEntity> processAnomaly(@PathVariable Long id, @Valid @RequestBody(required = false) ProcessAnomalyRequest payload) {
        EnergyAnomalyEntity entity;
        if (payload != null && (payload.getProcessedBy() != null || payload.getRemark() != null)) {
            entity = energyAnomalyService.processEnergyAnomaly(id, payload.getProcessedBy(), payload.getRemark()).orElse(null);
        } else {
            entity = energyAnomalyService.processEnergyAnomaly(id).orElse(null);
        }
        if (entity == null) {
            return Result.error(404, "异常记录不存在");
        }
        normalizeAnomaly(entity);
        return Result.success("处理成功", entity);
    }

    private void normalizeAnomaly(EnergyAnomalyEntity entity) {
        if (entity == null) return;
        entity.setEnergyType(displayEnergyType(entity.getEnergyType()));
    }

    private String displayEnergyType(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isBlank()) return v;
        String lower = v.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "electric", "electricity", "电能", "电力" -> "电力";
            case "water", "水", "水资源" -> "水资源";
            case "gas", "燃气" -> "燃气";
            case "heat", "蒸汽", "热能" -> "热能";
            default -> v;
        };
    }

    /**
     * 标记所有异常为已处理
     * PUT /api/ems/analysis/anomalies/mark-all-processed
     *
     * @return 处理结果
     */
    @PutMapping("/anomalies/mark-all-processed")
    public Result<Void> markAllAnomaliesAsProcessed() {
        energyAnomalyService.markAllAnomaliesAsProcessed();
        return Result.success("处理成功");
    }

    /**
     * 保存能耗异常
     * POST /api/ems/analysis/anomalies
     *
     * @param energyAnomaly 能耗异常
     * @return 保存后的能耗异常
     */
    @PostMapping("/anomalies")
    public Result<EnergyAnomalyEntity> saveEnergyAnomaly(@RequestBody EnergyAnomalyEntity energyAnomaly) {
        EnergyAnomalyEntity savedAnomaly = energyAnomalyService.saveEnergyAnomaly(energyAnomaly);
        return Result.success("保存成功", savedAnomaly);
    }

    /**
     * 删除能耗异常
     * DELETE /api/ems/analysis/anomalies/{id}
     *
     * @param id 异常ID
     * @return 响应结果
     */
    @DeleteMapping("/anomalies/{id}")
    public Result<Void> deleteEnergyAnomaly(@PathVariable Long id) {
        energyAnomalyService.deleteEnergyAnomaly(id);
        return Result.success("删除成功");
    }

    /**
     * 获取能耗统计数据
     * GET /api/ems/analysis/statistics
     *
     * @param dimension 统计维度（workshop、production-line、equipment、team）
     * @param dateRange 日期范围
     * @param energyType 能源类型
     * @return 能耗统计数据
     */
    @GetMapping("/statistics")
    public Result<List<Map<String, Object>>> getEnergyStatistics(
            @RequestParam(required = false) String dimension,
            @RequestParam(required = false) List<String> dateRange,
            @RequestParam(required = false) String energyType) {
        LocalDateTime start = parseDateStart(dateRange == null || dateRange.isEmpty() ? null : dateRange.get(0));
        LocalDateTime end = parseDateEnd(dateRange == null || dateRange.size() < 2 ? null : dateRange.get(1));
        List<RealTimeDataEntity> allData = (start == null || end == null)
                ? realTimeDataService.getAllRealTimeData()
                : realTimeDataService.getRealTimeDataByTimeRange(start, end);

        String filterEnergyType = energyType == null || energyType.isBlank() ? null : canonicalEnergyType(energyType);
        if (filterEnergyType != null) {
            allData = allData.stream()
                    .filter(d -> d != null && filterEnergyType.equals(canonicalEnergyType(d.getEnergyType())))
                    .toList();
        }

        Map<String, Map<String, Double>> areaEnergyMap = new HashMap<>();
        for (RealTimeDataEntity data : allData) {
            if (data == null) continue;
            String area = data.getArea();
            String type = canonicalEnergyType(data.getEnergyType());
            Double value = data.getActualValue();
            if (area == null || area.isBlank() || type == null || type.isBlank() || value == null) continue;
            areaEnergyMap.putIfAbsent(area, new HashMap<>());
            Map<String, Double> energyMap = areaEnergyMap.get(area);
            energyMap.put(type, energyMap.getOrDefault(type, 0.0) + value);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        int id = 1;
        String statDate = (start != null ? start.toLocalDate() : LocalDate.now()).toString();
        for (Map.Entry<String, Map<String, Double>> entry : areaEnergyMap.entrySet()) {
            String area = entry.getKey();
            Map<String, Double> energyMap = entry.getValue();

            double electricity = energyMap.getOrDefault("electricity", 0.0);
            double water = energyMap.getOrDefault("water", 0.0);
            double gas = energyMap.getOrDefault("gas", 0.0);
            double heat = energyMap.getOrDefault("heat", 0.0);

            Map<String, Object> statData = new HashMap<>();
            statData.put("id", id++);
            statData.put("name", area);
            statData.put("electricity", round2(electricity));
            statData.put("water", round2(water));
            statData.put("gas", round2(gas));
            statData.put("heat", round2(heat));

            double totalCost = electricity * 0.8 + water * 5 + gas * 3 + heat * 20;
            statData.put("totalCost", round2(totalCost));
            statData.put("statDate", statDate);
            result.add(statData);
        }

        return Result.success(result);
    }

    /**
     * 获取能耗趋势分析数据
     * GET /api/ems/analysis/trend
     *
     * @param energyType 能源类型（electricity、water、gas、heat）
     * @param timeRange 时间范围（day、week、month、year）
     * @param area 区域
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 能耗趋势数据
     */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrendAnalysis(
            @RequestParam(required = false) String energyType,
            @RequestParam(required = false, defaultValue = "day") String timeRange,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseDateStart(startDate);
        LocalDateTime end = parseDateEnd(endDate);
        if (start == null || end == null) {
            LocalDateTime now = LocalDateTime.now();
            if ("week".equals(timeRange)) {
                start = now.minusDays(7);
                end = now;
            } else if ("month".equals(timeRange)) {
                start = now.minusDays(30);
                end = now;
            } else if ("year".equals(timeRange)) {
                start = now.minusDays(365);
                end = now;
            } else {
                start = now.minusDays(1);
                end = now;
            }
        }

        List<RealTimeDataEntity> allData = realTimeDataService.getRealTimeDataByTimeRange(start, end);
        String filterEnergyType = energyType == null || energyType.isBlank() ? null : canonicalEnergyType(energyType);
        String filterArea = area == null || area.isBlank() ? null : area;

        List<RealTimeDataEntity> filteredData = allData.stream()
                .filter(d -> d != null && d.getCollectionTime() != null)
                .filter(d -> filterEnergyType == null || filterEnergyType.equals(canonicalEnergyType(d.getEnergyType())))
                .filter(d -> filterArea == null || filterArea.equals(d.getArea()))
                .toList();

        Map<Long, Double> bucket = new HashMap<>();
        for (RealTimeDataEntity data : filteredData) {
            LocalDateTime t = data.getCollectionTime();
            long key = bucketKey(t, timeRange);
            bucket.put(key, bucket.getOrDefault(key, 0.0) + (data.getActualValue() == null ? 0.0 : data.getActualValue()));
        }

        List<Map<String, Object>> result = bucket.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("time", bucketLabel(e.getKey(), timeRange));
                    m.put("value", round2(e.getValue()));
                    return m;
                })
                .collect(Collectors.toList());

        return Result.success(result);
    }

    /**
     * 获取能耗对比数据
     * GET /api/ems/analysis/comparison
     *
     * @param type 对比类型（year-on-year、month-on-month、multi-equipment）
     * @param energyType 能源类型
     * @param dimension 维度
     * @param dateRange 日期范围
     * @return 能耗对比数据
     */
    @GetMapping("/comparison")
    public Result<List<Map<String, Object>>> getEnergyComparison(
            @RequestParam(required = false, defaultValue = "year-on-year") String type,
            @RequestParam(required = false) String energyType,
            @RequestParam(required = false) String dimension,
            @RequestParam(required = false) List<String> dateRange) {
        LocalDateTime currentStart = parseDateStart(dateRange == null || dateRange.isEmpty() ? null : dateRange.get(0));
        LocalDateTime currentEnd = parseDateEnd(dateRange == null || dateRange.size() < 2 ? null : dateRange.get(1));
        if (currentStart == null || currentEnd == null) {
            LocalDateTime now = LocalDateTime.now();
            currentStart = now.minusDays(30);
            currentEnd = now;
        }

        LocalDateTime previousStart;
        LocalDateTime previousEnd;
        if ("month-on-month".equals(type)) {
            previousStart = currentStart.minusMonths(1);
            previousEnd = currentEnd.minusMonths(1);
        } else {
            previousStart = currentStart.minusYears(1);
            previousEnd = currentEnd.minusYears(1);
        }

        String filterEnergyType = energyType == null || energyType.isBlank() ? null : canonicalEnergyType(energyType);

        Map<String, Double> current = sumByArea(realTimeDataService.getRealTimeDataByTimeRange(currentStart, currentEnd), filterEnergyType);
        Map<String, Double> previous = sumByArea(realTimeDataService.getRealTimeDataByTimeRange(previousStart, previousEnd), filterEnergyType);

        Set<String> areas = new HashSet<>();
        areas.addAll(current.keySet());
        areas.addAll(previous.keySet());

        List<Map<String, Object>> result = new ArrayList<>();
        int id = 1;
        for (String area : areas) {
            double currentValue = current.getOrDefault(area, 0.0);
            double previousValue = previous.getOrDefault(area, 0.0);
            double difference = currentValue - previousValue;
            double growthRate = previousValue == 0 ? 0 : (difference / previousValue) * 100;

            Map<String, Object> comparisonData = new HashMap<>();
            comparisonData.put("id", id++);
            comparisonData.put("name", area);
            comparisonData.put("currentPeriod", round2(currentValue));
            comparisonData.put("previousPeriod", round2(previousValue));
            comparisonData.put("difference", round2(difference));
            comparisonData.put("growthRate", round2(growthRate));
            result.add(comparisonData);
        }

        result.sort(Comparator.comparing(m -> String.valueOf(m.get("name"))));
        return Result.success(result);
    }

    private LocalDateTime parseDateStart(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value).atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        throw new IllegalArgumentException("日期格式错误: " + value);
    }

    private LocalDateTime parseDateEnd(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value).atTime(23, 59, 59);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        throw new IllegalArgumentException("日期格式错误: " + value);
    }

    private Map<String, Double> sumByArea(List<RealTimeDataEntity> rows, String energyType) {
        Map<String, Double> map = new HashMap<>();
        for (RealTimeDataEntity d : rows) {
            if (d == null) continue;
            if (energyType != null && !energyType.equals(canonicalEnergyType(d.getEnergyType()))) continue;
            String area = d.getArea();
            Double v = d.getActualValue();
            if (area == null || area.isBlank() || v == null) continue;
            map.put(area, map.getOrDefault(area, 0.0) + v);
        }
        return map;
    }

    private String canonicalEnergyType(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isBlank()) return null;
        String lower = v.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "electric", "electricity", "电能", "电力" -> "electricity";
            case "water", "水", "水资源" -> "water";
            case "gas", "燃气" -> "gas";
            case "heat", "蒸汽", "热能" -> "heat";
            default -> v;
        };
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private long bucketKey(LocalDateTime time, String timeRange) {
        if ("week".equals(timeRange) || "month".equals(timeRange)) {
            return time.toLocalDate().toEpochDay();
        }
        if ("year".equals(timeRange)) {
            return time.getYear() * 100L + time.getMonthValue();
        }
        return time.toLocalDate().toEpochDay() * 24L + time.getHour();
    }

    private String bucketLabel(long key, String timeRange) {
        if ("year".equals(timeRange)) {
            long year = key / 100L;
            long month = key % 100L;
            return year + "-" + (month < 10 ? "0" + month : String.valueOf(month));
        }
        if ("day".equals(timeRange)) {
            long day = key / 24L;
            long hour = key % 24L;
            LocalDate date = LocalDate.ofEpochDay(day);
            return date + " " + (hour < 10 ? "0" + hour : String.valueOf(hour)) + ":00";
        }
        LocalDate date = LocalDate.ofEpochDay(key);
        return date.toString();
    }
}
