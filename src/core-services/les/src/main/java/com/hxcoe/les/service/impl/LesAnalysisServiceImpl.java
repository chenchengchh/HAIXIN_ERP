package com.hxcoe.les.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesLogisticsAnalysisDto;
import com.hxcoe.les.dto.LesTransportStatsDto;
import com.hxcoe.les.entity.LesMonitorLogEntity;
import com.hxcoe.les.entity.LesRouteEntity;
import com.hxcoe.les.entity.LesServiceQualityEntity;
import com.hxcoe.les.entity.LesTransportCostEntity;
import com.hxcoe.les.entity.LesTransportPlanEntity;
import com.hxcoe.les.entity.LesSignVoucherEntity;
import com.hxcoe.les.entity.LesVehicleEntity;
import com.hxcoe.les.entity.LesDriverEntity;
import com.hxcoe.les.repository.LesAnomalyEventRepository;
import com.hxcoe.les.repository.LesMonitorLogRepository;
import com.hxcoe.les.repository.LesRouteRepository;
import com.hxcoe.les.repository.LesServiceQualityRepository;
import com.hxcoe.les.repository.LesSignVoucherRepository;
import com.hxcoe.les.repository.LesTransportCostRepository;
import com.hxcoe.les.repository.LesTransportPlanRepository;
import com.hxcoe.les.repository.LesVehicleRepository;
import com.hxcoe.les.repository.LesDriverRepository;
import com.hxcoe.les.service.LesAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LesAnalysisServiceImpl implements LesAnalysisService {

    @Autowired
    private LesTransportCostRepository transportCostRepository;

    @Autowired
    private LesServiceQualityRepository serviceQualityRepository;

    @Autowired
    private LesTransportPlanRepository transportPlanRepository;

    @Autowired
    private LesRouteRepository routeRepository;

    @Autowired
    private LesMonitorLogRepository monitorLogRepository;

    @Autowired
    private LesSignVoucherRepository signVoucherRepository;

    @Autowired
    private LesAnomalyEventRepository anomalyEventRepository;

    @Autowired
    private LesVehicleRepository vehicleRepository;

    @Autowired
    private LesDriverRepository driverRepository;

    @Override
    public Result<PageResult<LesTransportCostEntity>> fetchTransportCosts(int page, int size, Long planId) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        Page<LesTransportCostEntity> result;
        if (planId != null) {
            result = transportCostRepository.findByPlanId(planId, pageable);
        } else {
            result = transportCostRepository.findAll(pageable);
        }
        PageResult<LesTransportCostEntity> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, result.getContent());
        return Result.success("运输成本查询成功", pageResult);
    }

    @Override
    public Result<LesTransportCostEntity> getPlanCost(Long planId) {
        if (planId == null) {
            return Result.fail("planId不能为空");
        }
        LesTransportCostEntity cost = transportCostRepository.findTopByPlanIdOrderByCreateTimeDesc(planId).orElse(null);
        if (cost == null) {
            return Result.fail("成本数据不存在");
        }
        return Result.success(cost);
    }

    @Override
    public Result<PageResult<LesLogisticsAnalysisDto>> fetchLogisticsAnalysis(int page, int size, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        Page<LesTransportPlanEntity> plans = transportPlanRepository.findAll(pageable);
        List<LesLogisticsAnalysisDto> rows = plans.getContent().stream().map(this::toAnalysisRow).toList();
        PageResult<LesLogisticsAnalysisDto> pageResult = PageResult.build(plans.getTotalElements(), plans.getSize(), plans.getNumber() + 1, rows);
        return Result.success("物流分析查询成功", pageResult);
    }

    @Override
    public Result<PageResult<LesServiceQualityEntity>> fetchServiceQualities(int page, int size, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        Page<LesServiceQualityEntity> result = serviceQualityRepository.findAll(pageable);
        PageResult<LesServiceQualityEntity> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, result.getContent());
        return Result.success("服务质量评估查询成功", pageResult);
    }

    @Override
    public Result<LesTransportStatsDto> fetchTransportStats(String startDate, String endDate) {
        long totalPlans = transportPlanRepository.count();
        long inTransit = transportPlanRepository.countByStatus(2);
        long completed = transportPlanRepository.countByStatus(3);
        long aborted = transportPlanRepository.countByStatus(4);
        long delayedPlans = aborted;

        double totalCost = transportCostRepository.findAll().stream()
                .map(LesTransportCostEntity::getTotalCost)
                .filter(v -> v != null)
                .mapToDouble(Double::doubleValue)
                .sum();

        Map<Long, LesRouteEntity> routeMap = routeRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(LesRouteEntity::getId, r -> r, (a, b) -> a));
        double totalDistance = transportPlanRepository.findAll().stream()
                .map(LesTransportPlanEntity::getRouteId)
                .filter(v -> v != null)
                .map(routeMap::get)
                .filter(r -> r != null && r.getDistance() != null)
                .mapToDouble(r -> r.getDistance())
                .sum();

        long signedVouchers = signVoucherRepository.countByStatus("signed");
        long totalVouchers = signVoucherRepository.count();
        long onTimeOrEarly = signVoucherRepository.findAll().stream()
                .map(LesSignVoucherEntity::getOnTimeStatus)
                .filter(v -> v != null && (v == 1 || v == 2))
                .count();

        double averageOnTimeRate = totalVouchers > 0 ? round2(((double) onTimeOrEarly / (double) totalVouchers) * 100.0) : 0.0;
        double averageSignSuccessRate = totalPlans > 0 ? round2(((double) signedVouchers / (double) totalPlans) * 100.0) : 0.0;

        LesTransportStatsDto dto = new LesTransportStatsDto();
        dto.setTotalPlans(totalPlans);
        dto.setInTransitPlans(inTransit);
        dto.setCompletedPlans(completed);
        dto.setDelayedPlans(delayedPlans);
        dto.setTotalDistance(round2(totalDistance));
        dto.setTotalCost(round2(totalCost));
        dto.setAverageOnTimeRate(averageOnTimeRate);
        dto.setAverageSignSuccessRate(averageSignSuccessRate);
        return Result.success("运输统计查询成功", dto);
    }

    @Override
    public Result<Object> fetchEfficiencyAnalysis(String startDate, String endDate) {
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);
        Map<Long, LesRouteEntity> routeMap = routeRepository.findAll().stream()
                .filter(r -> r.getId() != null)
                .collect(Collectors.toMap(LesRouteEntity::getId, r -> r, (a, b) -> a));

        List<LesTransportPlanEntity> plans = transportPlanRepository.findAll().stream()
                .filter(p -> inRange(p.getCreateTime(), start, end))
                .toList();

        Map<Long, RouteDelayAgg> routeAgg = new HashMap<>();
        long delaySamples = 0;
        double delaySum = 0;

        for (LesTransportPlanEntity plan : plans) {
            if (plan.getId() == null) continue;
            LesRouteEntity route = plan.getRouteId() == null ? null : routeMap.get(plan.getRouteId());
            int planned = route != null && route.getEstimatedTime() != null ? route.getEstimatedTime() : 0;

            int actual = calcActualDurationMinutes(plan.getId());
            if (actual <= 0 || planned <= 0) continue;

            int delay = Math.max(0, actual - planned);
            delaySum += delay;
            delaySamples += 1;

            if (route != null && route.getId() != null) {
                RouteDelayAgg agg = routeAgg.getOrDefault(route.getId(), new RouteDelayAgg(route.getId(), route.getRouteName()));
                agg.planCount += 1;
                agg.totalDelayMinutes += delay;
                routeAgg.put(route.getId(), agg);
            }
        }

        double avgDelay = delaySamples > 0 ? round2(delaySum / (double) delaySamples) : 0.0;

        List<Map<String, Object>> topDelayRoutes = routeAgg.values().stream()
                .sorted((a, b) -> Integer.compare(b.totalDelayMinutes, a.totalDelayMinutes))
                .limit(10)
                .map(agg -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("routeId", agg.routeId);
                    row.put("routeName", agg.routeName == null ? "" : agg.routeName);
                    row.put("planCount", agg.planCount);
                    row.put("totalDelayMinutes", agg.totalDelayMinutes);
                    row.put("avgDelayMinutes", agg.planCount > 0 ? round2((double) agg.totalDelayMinutes / (double) agg.planCount) : 0.0);
                    return row;
                })
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("totalPlans", plans.size());
        result.put("averageDelayMinutes", avgDelay);
        result.put("topDelayRoutes", topDelayRoutes);
        return Result.success("运输效率分析查询成功", result);
    }

    @Override
    public Result<Object> fetchUtilizationAnalysis(String startDate, String endDate) {
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);
        Map<Long, LesRouteEntity> routeMap = routeRepository.findAll().stream()
                .filter(r -> r.getId() != null)
                .collect(Collectors.toMap(LesRouteEntity::getId, r -> r, (a, b) -> a));

        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<LesTransportPlanEntity> plans = transportPlanRepository.findAll().stream()
                .filter(p -> p.getCreateTime() != null && p.getCreateTime().isAfter(cutoff))
                .filter(p -> inRange(p.getCreateTime(), start, end))
                .toList();

        Map<Long, List<LesTransportPlanEntity>> plansByVehicle = plans.stream()
                .filter(p -> p.getVehicleId() != null)
                .collect(Collectors.groupingBy(LesTransportPlanEntity::getVehicleId));

        Map<Long, List<LesTransportPlanEntity>> plansByDriver = plans.stream()
                .filter(p -> p.getDriverId() != null)
                .collect(Collectors.groupingBy(LesTransportPlanEntity::getDriverId));

        List<LesVehicleEntity> vehicles = vehicleRepository.findAll();
        List<Map<String, Object>> vehicleUtilization = vehicles.stream().map(v -> {
            List<LesTransportPlanEntity> vPlans = v.getId() == null ? List.of() : plansByVehicle.getOrDefault(v.getId(), List.of());
            long activeDays = vPlans.stream()
                    .map(p -> p.getCreateTime() == null ? null : p.getCreateTime().toLocalDate())
                    .filter(d -> d != null)
                    .distinct()
                    .count();
            double mileage = vPlans.stream()
                    .map(p -> p.getRouteId() == null ? null : routeMap.get(p.getRouteId()))
                    .filter(r -> r != null && r.getDistance() != null)
                    .mapToDouble(r -> r.getDistance())
                    .sum();
            long idleDays = Math.max(0, 30 - activeDays);
            double utilizationRate = round2(activeDays > 0 ? ((double) activeDays / 30.0) * 100.0 : 0.0);

            Map<String, Object> row = new HashMap<>();
            row.put("id", v.getId());
            row.put("licensePlate", v.getLicensePlate());
            row.put("vehicleType", v.getVehicleType());
            row.put("dailyMileage", round2(mileage / 30.0));
            row.put("idleDays", idleDays);
            row.put("utilizationRate", utilizationRate);
            return row;
        }).toList();

        List<LesDriverEntity> drivers = driverRepository.findAll();
        List<Map<String, Object>> driverUtilization = drivers.stream().map(d -> {
            List<LesTransportPlanEntity> dPlans = d.getId() == null ? List.of() : plansByDriver.getOrDefault(d.getId(), List.of());
            long workDays = dPlans.stream()
                    .map(p -> p.getCreateTime() == null ? null : p.getCreateTime().toLocalDate())
                    .filter(v -> v != null)
                    .distinct()
                    .count();
            double mileage = dPlans.stream()
                    .map(p -> p.getRouteId() == null ? null : routeMap.get(p.getRouteId()))
                    .filter(r -> r != null && r.getDistance() != null)
                    .mapToDouble(r -> r.getDistance())
                    .sum();

            double totalMinutes = 0.0;
            for (LesTransportPlanEntity p : dPlans) {
                if (p.getId() == null) continue;
                int actual = calcActualDurationMinutes(p.getId());
                if (actual > 0) totalMinutes += actual;
            }
            double avgWorkHours = workDays > 0 ? round2((totalMinutes / 60.0) / (double) workDays) : 0.0;
            double utilizationRate = round2(workDays > 0 ? ((double) workDays / 30.0) * 100.0 : 0.0);

            Map<String, Object> row = new HashMap<>();
            row.put("id", d.getId());
            row.put("name", d.getName());
            row.put("phone", d.getPhone());
            row.put("dailyMileage", round2(mileage / 30.0));
            row.put("workDays", workDays);
            row.put("avgWorkHours", avgWorkHours);
            row.put("utilizationRate", utilizationRate);
            return row;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("vehicleUtilization", vehicleUtilization);
        result.put("driverUtilization", driverUtilization);
        return Result.success("资源利用率分析查询成功", result);
    }

    @Override
    public Result<Object> fetchAnomalyAnalysis(String startDate, String endDate) {
        LocalDateTime start = parseStart(startDate);
        LocalDateTime end = parseEnd(endDate);

        List<com.hxcoe.les.entity.LesAnomalyEventEntity> events = anomalyEventRepository.findAll().stream()
                .filter(e -> inRange(e.getEventTime() != null ? e.getEventTime() : e.getCreateTime(), start, end))
                .toList();

        Map<String, Long> countByType = events.stream()
                .map(e -> e.getEventType() == null ? "" : e.getEventType().trim())
                .collect(Collectors.groupingBy(t -> t.isEmpty() ? "未知" : t, Collectors.counting()));

        long total = events.size();
        List<Map<String, Object>> byType = countByType.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("eventType", e.getKey());
                    row.put("count", e.getValue());
                    row.put("percent", total > 0 ? round2(((double) e.getValue() / (double) total) * 100.0) : 0.0);
                    return row;
                })
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("totalAnomalies", total);
        result.put("byType", byType);
        return Result.success("异常事件分析查询成功", result);
    }

    private LesLogisticsAnalysisDto toAnalysisRow(LesTransportPlanEntity plan) {
        LesLogisticsAnalysisDto dto = new LesLogisticsAnalysisDto();
        dto.setId(plan.getId());
        dto.setPlanId(plan.getId());

        int plannedDuration = 0;
        if (plan.getRouteId() != null) {
            LesRouteEntity route = routeRepository.findById(plan.getRouteId()).orElse(null);
            if (route != null && route.getEstimatedTime() != null) {
                plannedDuration = route.getEstimatedTime();
            }
            dto.setCostPerKm(calcCostPerKm(plan.getId(), route == null ? null : route.getDistance()));
        } else {
            dto.setCostPerKm(0.0);
        }
        dto.setPlannedDuration(plannedDuration);

        int actualDuration = plannedDuration;
        List<LesMonitorLogEntity> logs = monitorLogRepository.findByPlanId(plan.getId(), PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "recordTime"))).getContent();
        List<LesMonitorLogEntity> logsDesc = monitorLogRepository.findByPlanId(plan.getId(), PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "recordTime"))).getContent();
        if (!logs.isEmpty() && !logsDesc.isEmpty() && logs.get(0).getRecordTime() != null && logsDesc.get(0).getRecordTime() != null) {
            actualDuration = (int) Duration.between(logs.get(0).getRecordTime(), logsDesc.get(0).getRecordTime()).toMinutes();
        }

        dto.setActualDuration(actualDuration);
        dto.setDelayMinutes(actualDuration - plannedDuration);
        dto.setVehicleUtilization(0.0);
        dto.setDriverUtilization(0.0);
        dto.setCreateTime(formatDateTime(plan.getCreateTime()));
        return dto;
    }

    private double calcCostPerKm(Long planId, Double distance) {
        if (planId == null || distance == null || distance <= 0) {
            return 0.0;
        }
        Optional<LesTransportCostEntity> cost = transportCostRepository.findTopByPlanIdOrderByCreateTimeDesc(planId);
        if (cost.isEmpty() || cost.get().getTotalCost() == null) {
            return 0.0;
        }
        return round2(cost.get().getTotalCost() / distance);
    }

    private int calcActualDurationMinutes(Long planId) {
        if (planId == null) return 0;
        List<LesMonitorLogEntity> logsAsc = monitorLogRepository.findByPlanId(planId, PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "recordTime"))).getContent();
        List<LesMonitorLogEntity> logsDesc = monitorLogRepository.findByPlanId(planId, PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "recordTime"))).getContent();
        if (logsAsc.isEmpty() || logsDesc.isEmpty()) return 0;
        LocalDateTime start = logsAsc.get(0).getRecordTime();
        LocalDateTime end = logsDesc.get(0).getRecordTime();
        if (start == null || end == null) return 0;
        return (int) Math.max(0, Duration.between(start, end).toMinutes());
    }

    private static boolean inRange(LocalDateTime time, LocalDateTime start, LocalDateTime end) {
        if (time == null) return false;
        if (start != null && time.isBefore(start)) return false;
        if (end != null && time.isAfter(end)) return false;
        return true;
    }

    private static LocalDateTime parseStart(String text) {
        LocalDateTime parsed = parseDateTime(text);
        if (parsed != null) return parsed;
        return null;
    }

    private static LocalDateTime parseEnd(String text) {
        LocalDateTime parsed = parseDateTime(text);
        if (parsed != null) return parsed;
        return null;
    }

    private static LocalDateTime parseDateTime(String text) {
        if (text == null) return null;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return null;
        try {
            if (trimmed.length() == 10) {
                return LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
            }
            if (trimmed.length() == 19 && trimmed.charAt(10) == 'T') {
                return LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            }
            if (trimmed.length() == 19 && trimmed.charAt(10) == ' ') {
                return LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            return LocalDateTime.parse(trimmed);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static class RouteDelayAgg {
        final Long routeId;
        final String routeName;
        int planCount;
        int totalDelayMinutes;

        RouteDelayAgg(Long routeId, String routeName) {
            this.routeId = routeId;
            this.routeName = routeName;
        }
    }

    private static String formatDateTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
