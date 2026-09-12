package com.hxcoe.ems.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.ems.entity.EffectEvaluationEntity;
import com.hxcoe.ems.entity.OptimizationPlanEntity;
import com.hxcoe.ems.entity.OptimizationSuggestionEntity;
import com.hxcoe.ems.service.EffectEvaluationService;
import com.hxcoe.ems.service.OptimizationPlanService;
import com.hxcoe.ems.service.OptimizationSuggestionService;
import com.hxcoe.ems.service.RealTimeDataService;
import com.hxcoe.ems.dto.UpdatePlanStatusRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.*;
import java.util.List;
import java.util.Map;


/**
 * 能源优化控制器
 * 处理能源优化相关的API请求，包括优化建议、节能潜力分析等
 *
 * @author author
 * @date 2026-01-01
 */
@RestController
@RequestMapping("/api/v1/ems/optimization")
@Validated
public class EnergyOptimizationController {

    private final OptimizationSuggestionService optimizationSuggestionService;
    private final OptimizationPlanService optimizationPlanService;
    private final EffectEvaluationService effectEvaluationService;
    private final RealTimeDataService realTimeDataService;

    @Autowired
    public EnergyOptimizationController(OptimizationSuggestionService optimizationSuggestionService,
                                      OptimizationPlanService optimizationPlanService,
                                      EffectEvaluationService effectEvaluationService,
                                      RealTimeDataService realTimeDataService) {
        this.optimizationSuggestionService = optimizationSuggestionService;
        this.optimizationPlanService = optimizationPlanService;
        this.effectEvaluationService = effectEvaluationService;
        this.realTimeDataService = realTimeDataService;
    }

    /**
     * 获取优化建议列表
     * GET /api/ems/optimization/suggestions
     *
     * @param status     状态
     * @param targetArea 目标区域
     * @return 优化建议列表
     */
    @GetMapping("/suggestions")
    public Result<List<OptimizationSuggestionEntity>> getOptimizationSuggestions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetArea) {
        
        List<OptimizationSuggestionEntity> suggestions = optimizationSuggestionService.getOptimizationSuggestionsByConditions(status, targetArea);
        return Result.success(suggestions);
    }

    @GetMapping("/suggestions/page")
    public Result<Map<String, Object>> getOptimizationSuggestionsPage(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetArea,
            @RequestParam(required = false, defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(200) int size
    ) {
        Page<OptimizationSuggestionEntity> p = optimizationSuggestionService.getOptimizationSuggestionsPage(
                status,
                targetArea,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        );
        Map<String, Object> data = new HashMap<>();
        data.put("total", p.getTotalElements());
        data.put("list", p.getContent());
        data.put("page", p.getNumber());
        data.put("size", p.getSize());
        return Result.success(data);
    }

    /**
     * 根据ID获取优化建议
     * GET /api/ems/optimization/suggestions/{id}
     *
     * @param id 建议ID
     * @return 优化建议
     */
    @GetMapping("/suggestions/{id}")
    public Result<OptimizationSuggestionEntity> getOptimizationSuggestionById(@PathVariable Long id) {
        OptimizationSuggestionEntity entity = optimizationSuggestionService.getOptimizationSuggestionById(id).orElse(null);
        if (entity == null) {
            return Result.error(404, "优化建议不存在");
        }
        return Result.success(entity);
    }

    /**
     * 采纳优化建议
     * PUT /api/ems/optimization/suggestions/{id}/adopt
     *
     * @param id 建议ID
     * @return 采纳后的优化建议
     */
    @PutMapping("/suggestions/{id}/adopt")
    public Result<OptimizationSuggestionEntity> adoptSuggestion(@PathVariable Long id) {
        OptimizationSuggestionEntity entity = optimizationSuggestionService.adoptOptimizationSuggestion(id).orElse(null);
        if (entity == null) {
            return Result.error(404, "优化建议不存在");
        }
        return Result.success("采纳成功", entity);
    }

    /**
     * 保存优化建议
     * POST /api/ems/optimization/suggestions
     *
     * @param suggestion 优化建议
     * @return 保存后的优化建议
     */
    @PostMapping("/suggestions")
    public Result<OptimizationSuggestionEntity> saveOptimizationSuggestion(@RequestBody OptimizationSuggestionEntity suggestion) {
        OptimizationSuggestionEntity savedSuggestion = optimizationSuggestionService.saveOptimizationSuggestion(suggestion);
        return Result.success("保存成功", savedSuggestion);
    }

    /**
     * 删除优化建议
     * DELETE /api/ems/optimization/suggestions/{id}
     *
     * @param id 建议ID
     * @return 响应结果
     */
    @DeleteMapping("/suggestions/{id}")
    public Result<Void> deleteOptimizationSuggestion(@PathVariable Long id) {
        optimizationSuggestionService.deleteOptimizationSuggestion(id);
        return Result.success("删除成功");
    }

    /**
     * 获取节能潜力分析数据
     * GET /api/ems/optimization/potential
     *
     * @param params 查询参数
     * @return 节能潜力分析数据
     */
    @GetMapping("/potential")
    public Result<List<Map<String, Object>>> getPotentialAnalysis(@RequestParam(required = false) Map<String, String> params) {
        String filterArea = params == null ? null : params.get("area");
        String filterEnergyTypeRaw = params == null ? null : params.get("energyType");
        String filterPriority = params == null ? null : params.get("priority");

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(30);
        if (params != null) {
            LocalDateTime startParam = parseDateStart(params.get("startDate"));
            LocalDateTime endParam = parseDateEnd(params.get("endDate"));
            if (startParam != null && endParam != null) {
                start = startParam;
                end = endParam;
            }
        }

        Duration d = Duration.between(start, end);
        if (d.isNegative() || d.isZero()) {
            throw new IllegalArgumentException("时间范围不合法");
        }
        LocalDateTime prevEnd = start;
        LocalDateTime prevStart = start.minus(d);

        List<com.hxcoe.ems.entity.RealTimeDataEntity> currentRows = realTimeDataService.getRealTimeDataByTimeRange(start, end);
        List<com.hxcoe.ems.entity.RealTimeDataEntity> previousRows = realTimeDataService.getRealTimeDataByTimeRange(prevStart, prevEnd);

        String filterEnergyType = canonicalEnergyType(filterEnergyTypeRaw);

        Map<String, Map<String, Double>> current = sumByAreaAndType(currentRows, filterArea, filterEnergyType);
        Map<String, Map<String, Double>> previous = sumByAreaAndType(previousRows, filterArea, filterEnergyType);

        Set<String> areas = new HashSet<>();
        areas.addAll(current.keySet());
        areas.addAll(previous.keySet());

        List<Map<String, Object>> result = new ArrayList<>();
        int id = 1;
        for (String area : areas) {
            Set<String> types = new HashSet<>();
            Map<String, Double> c = current.getOrDefault(area, Collections.emptyMap());
            Map<String, Double> p = previous.getOrDefault(area, Collections.emptyMap());
            types.addAll(c.keySet());
            types.addAll(p.keySet());

            for (String type : types) {
                double currentConsumption = c.getOrDefault(type, 0.0);
                Double previousConsumptionObj = p.get(type);
                double saving;
                double potential;
                if (previousConsumptionObj == null || previousConsumptionObj == 0.0) {
                    if (currentConsumption <= 0) {
                        saving = 0.0;
                        potential = 0.0;
                    } else {
                        potential = 15.0;
                        saving = currentConsumption * 0.15;
                    }
                } else {
                    double previousConsumption = previousConsumptionObj;
                    saving = Math.max(0.0, previousConsumption - currentConsumption);
                    potential = (saving / previousConsumption) * 100.0;
                }
                potential = round1(potential);

                String priority = potential >= 20 ? "高" : (potential >= 10 ? "中" : "低");
                if (filterPriority != null && !filterPriority.isBlank() && !filterPriority.equals(priority)) {
                    continue;
                }

                Map<String, Object> row = new HashMap<>();
                row.put("id", id++);
                row.put("area", area);
                row.put("energyType", displayEnergyType(type));
                row.put("potential", potential);
                row.put("estimatedSavings", formatSavings(type, saving));
                row.put("priority", priority);
                result.add(row);
            }
        }

        result.sort(Comparator.comparing(m -> String.valueOf(m.get("area"))));
        return Result.success(result);
    }

    private Map<String, Map<String, Double>> sumByAreaAndType(
            List<com.hxcoe.ems.entity.RealTimeDataEntity> rows,
            String areaFilter,
            String energyTypeFilter
    ) {
        Map<String, Map<String, Double>> map = new HashMap<>();
        String a = areaFilter == null || areaFilter.isBlank() || "all".equals(areaFilter) ? null : areaFilter;
        for (com.hxcoe.ems.entity.RealTimeDataEntity r : rows) {
            if (r == null) continue;
            String area = r.getArea();
            if (a != null && (area == null || !a.equals(area))) continue;
            String type = canonicalEnergyType(r.getEnergyType());
            if (type == null) continue;
            if (energyTypeFilter != null && !energyTypeFilter.equals(type)) continue;
            Double v = r.getActualValue();
            if (v == null) continue;
            map.putIfAbsent(area, new HashMap<>());
            Map<String, Double> inner = map.get(area);
            inner.put(type, inner.getOrDefault(type, 0.0) + v);
        }
        return map;
    }

    private String canonicalEnergyType(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isBlank() || "all".equals(v)) return null;
        String lower = v.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "electric", "electricity", "电能", "电力" -> "electricity";
            case "water", "水", "水资源" -> "water";
            case "gas", "燃气" -> "gas";
            case "heat", "蒸汽", "热能" -> "heat";
            default -> v;
        };
    }

    private String displayEnergyType(String canonical) {
        if (canonical == null) return "";
        return switch (canonical) {
            case "electricity" -> "电力";
            case "water" -> "水资源";
            case "gas" -> "燃气";
            case "heat" -> "热能";
            default -> canonical;
        };
    }

    private String formatSavings(String canonicalType, double value) {
        String unit = switch (canonicalType) {
            case "electricity" -> "kWh";
            case "water" -> "m³";
            case "gas" -> "m³";
            case "heat" -> "GJ";
            default -> "";
        };
        return round0(value) + (unit.isEmpty() ? "" : (" " + unit));
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private long round0(double v) {
        return Math.round(v);
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



    /**
     * 获取优化方案列表
     * GET /api/ems/optimization/plans
     *
     * @param status     状态
     * @param targetArea 目标区域
     * @return 优化方案列表
     */
    @GetMapping("/plans")
    public Result<?> getOptimizationPlans(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String targetArea,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(200) Integer size) {
        if (page != null || size != null) {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;
            Page<OptimizationPlanEntity> r = optimizationPlanService.getOptimizationPlansPage(
                    status,
                    targetArea,
                    PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"))
            );
            Map<String, Object> data = new HashMap<>();
            data.put("total", r.getTotalElements());
            data.put("list", r.getContent());
            data.put("page", r.getNumber());
            data.put("size", r.getSize());
            return Result.success(data);
        }

        List<OptimizationPlanEntity> plans;
        if (status != null) {
            plans = optimizationPlanService.getOptimizationPlansByStatus(status);
        } else if (targetArea != null) {
            plans = optimizationPlanService.getOptimizationPlansByTargetArea(targetArea);
        } else {
            plans = optimizationPlanService.getAllOptimizationPlans();
        }
        return Result.success(plans);
    }

    /**
     * 根据ID获取优化方案
     * GET /api/ems/optimization/plans/{id}
     *
     * @param id 方案ID
     * @return 优化方案
     */
    @GetMapping("/plans/{id}")
    public Result<OptimizationPlanEntity> getOptimizationPlanById(@PathVariable Long id) {
        OptimizationPlanEntity entity = optimizationPlanService.getOptimizationPlanById(id).orElse(null);
        if (entity == null) {
            return Result.error(404, "优化方案不存在");
        }
        return Result.success(entity);
    }

    /**
     * 保存优化方案
     * POST /api/ems/optimization/plans
     *
     * @param optimizationPlan 优化方案
     * @return 保存后的优化方案
     */
    @PostMapping("/plans")
    public Result<OptimizationPlanEntity> saveOptimizationPlan(@RequestBody OptimizationPlanEntity optimizationPlan) {
        OptimizationPlanEntity savedPlan = optimizationPlanService.saveOptimizationPlan(optimizationPlan);
        return Result.success("保存成功", savedPlan);
    }

    /**
     * 更新优化方案
     * PUT /api/ems/optimization/plans/{id}
     *
     * @param id               方案ID
     * @param optimizationPlan 更新的方案信息
     * @return 更新后的优化方案
     */
    @PutMapping("/plans/{id}")
    public Result<OptimizationPlanEntity> updateOptimizationPlan(@PathVariable Long id, @RequestBody OptimizationPlanEntity optimizationPlan) {
        OptimizationPlanEntity entity = optimizationPlanService.updateOptimizationPlan(id, optimizationPlan).orElse(null);
        if (entity == null) {
            return Result.error(404, "优化方案不存在");
        }
        return Result.success("更新成功", entity);
    }

    /**
     * 删除优化方案
     * DELETE /api/ems/optimization/plans/{id}
     *
     * @param id 方案ID
     * @return 响应结果
     */
    @DeleteMapping("/plans/{id}")
    public Result<Void> deleteOptimizationPlan(@PathVariable Long id) {
        optimizationPlanService.deleteOptimizationPlan(id);
        return Result.success("删除成功");
    }

    /**
     * 更新优化方案状态
     * PUT /api/ems/optimization/plans/{id}/status
     *
     * @param id     方案ID
     * @param status 新状态
     * @return 更新后的优化方案
     */
    @PutMapping("/plans/{id}/status")
    public Result<OptimizationPlanEntity> updateOptimizationPlanStatus(@PathVariable Long id, @Valid @RequestBody UpdatePlanStatusRequest payload) {
        OptimizationPlanEntity entity = optimizationPlanService.updateOptimizationPlanStatus(id, payload.getStatus()).orElse(null);
        if (entity == null) {
            return Result.error(404, "优化方案不存在");
        }
        return Result.success("更新成功", entity);
    }

    /**
     * 获取效果评估数据
     * GET /api/ems/optimization/evaluations
     *
     * @param planId           优化方案ID
     * @param targetArea       目标区域
     * @param energyType       能源类型
     * @param evaluationPeriod 评估周期
     * @return 效果评估数据
     */
    @GetMapping("/evaluations")
    public Result<?> getEffectEvaluations(
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) String targetArea,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) String energyType,
            @RequestParam(required = false) String evaluationPeriod,
            @RequestParam(required = false) List<String> dateRange,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(200) Integer size) {
        if (page != null || size != null || planName != null || dateRange != null) {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;
            LocalDateTime start = null;
            LocalDateTime end = null;
            if (dateRange != null && !dateRange.isEmpty()) {
                start = parseDateStart(dateRange.get(0));
                if (dateRange.size() > 1) {
                    end = parseDateEnd(dateRange.get(1));
                }
            }
            Page<EffectEvaluationEntity> r = effectEvaluationService.getEffectEvaluationsPage(
                    planId,
                    targetArea,
                    planName,
                    energyType,
                    evaluationPeriod,
                    start,
                    end,
                    PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "evaluationDate"))
            );
            Map<String, Object> data = new HashMap<>();
            data.put("total", r.getTotalElements());
            data.put("list", r.getContent());
            data.put("page", r.getNumber());
            data.put("size", r.getSize());
            return Result.success(data);
        }

        List<EffectEvaluationEntity> evaluations;
        
        if (planId != null) {
            evaluations = effectEvaluationService.getEffectEvaluationsByPlanId(planId);
        } else if (targetArea != null) {
            evaluations = effectEvaluationService.getEffectEvaluationsByTargetArea(targetArea);
        } else if (energyType != null) {
            evaluations = effectEvaluationService.getEffectEvaluationsByEnergyType(energyType);
        } else if (evaluationPeriod != null) {
            evaluations = effectEvaluationService.getEffectEvaluationsByEvaluationPeriod(evaluationPeriod);
        } else {
            evaluations = effectEvaluationService.getAllEffectEvaluations();
        }
        
        return Result.success(evaluations);
    }

    /**
     * 根据ID获取效果评估数据
     * GET /api/ems/optimization/evaluations/{id}
     *
     * @param id 评估ID
     * @return 效果评估数据
     */
    @GetMapping("/evaluations/{id}")
    public Result<EffectEvaluationEntity> getEffectEvaluationById(@PathVariable Long id) {
        EffectEvaluationEntity entity = effectEvaluationService.getEffectEvaluationById(id).orElse(null);
        if (entity == null) {
            return Result.error(404, "评估记录不存在");
        }
        return Result.success(entity);
    }

    @GetMapping("/evaluations/{id}/report")
    public ResponseEntity<byte[]> downloadEvaluationReport(@PathVariable Long id) {
        EffectEvaluationEntity entity = effectEvaluationService.getEffectEvaluationById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("planName,targetArea,predictedSaving,actualSaving,evaluationDate\n");
        sb.append(entity.getPlanName() == null ? "" : entity.getPlanName()).append(",");
        sb.append(entity.getTargetArea() == null ? "" : entity.getTargetArea()).append(",");
        sb.append(entity.getPredictedSaving() == null ? "" : entity.getPredictedSaving()).append(",");
        sb.append(entity.getActualSaving() == null ? "" : entity.getActualSaving()).append(",");
        sb.append(entity.getEvaluationDate() == null ? "" : entity.getEvaluationDate()).append("\n");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ems_evaluation_" + id + ".csv\"");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /**
     * 保存效果评估数据
     * POST /api/ems/optimization/evaluations
     *
     * @param effectEvaluation 效果评估数据
     * @return 保存后的效果评估数据
     */
    @PostMapping("/evaluations")
    public Result<EffectEvaluationEntity> saveEffectEvaluation(@RequestBody EffectEvaluationEntity effectEvaluation) {
        EffectEvaluationEntity savedEvaluation = effectEvaluationService.saveEffectEvaluation(effectEvaluation);
        return Result.success("保存成功", savedEvaluation);
    }

    /**
     * 更新效果评估数据
     * PUT /api/ems/optimization/evaluations/{id}
     *
     * @param id               评估ID
     * @param effectEvaluation 更新的评估信息
     * @return 更新后的效果评估数据
     */
    @PutMapping("/evaluations/{id}")
    public Result<EffectEvaluationEntity> updateEffectEvaluation(@PathVariable Long id, @RequestBody EffectEvaluationEntity effectEvaluation) {
        EffectEvaluationEntity entity = effectEvaluationService.updateEffectEvaluation(id, effectEvaluation).orElse(null);
        if (entity == null) {
            return Result.error(404, "评估记录不存在");
        }
        return Result.success("更新成功", entity);
    }

    /**
     * 删除效果评估数据
     * DELETE /api/ems/optimization/evaluations/{id}
     *
     * @param id 评估ID
     * @return 响应结果
     */
    @DeleteMapping("/evaluations/{id}")
    public Result<Void> deleteEffectEvaluation(@PathVariable Long id) {
        effectEvaluationService.deleteEffectEvaluation(id);
        return Result.success("删除成功");
    }
}
