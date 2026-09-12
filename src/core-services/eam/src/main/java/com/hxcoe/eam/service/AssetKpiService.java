package com.hxcoe.eam.service;

import com.hxcoe.eam.entity.AssetKpiEntity;
import com.hxcoe.eam.entity.FaultRecordEntity;
import com.hxcoe.eam.entity.MaintenanceRecordEntity;
import com.hxcoe.eam.repository.AssetKpiRepository;
import com.hxcoe.eam.repository.FaultRecordRepository;
import com.hxcoe.eam.repository.MaintenanceRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssetKpiService {

    @Autowired
    private AssetKpiRepository kpiRepository;

    @Autowired
    private FaultRecordRepository faultRecordRepository;

    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;

    public List<AssetKpiEntity> getKpiByMonth(String month) {
        /**
         * 获取指定月份的资产KPI记录
         * @param month 月份（YYYY-MM）
         * @return KPI列表
         */
        return kpiRepository.findByRecordMonth(month);
    }
    
    public Map<String, Object> getDashboardData() {
        /**
         * 获取KPI看板数据（默认当月，当月无数据时回退到最近有数据的月份）
         * @return 看板数据（平均OEE/MTBF/MTTR，总成本，KPI明细列表，实际数据月份）
         */
        String currentMonth = LocalDate.now().toString().substring(0, 7);
        List<AssetKpiEntity> kpis = kpiRepository.findByRecordMonth(currentMonth);
        String dataMonth = currentMonth;
        // 当月无数据时回退到最近有数据的月份，避免看板空展示
        if (kpis.isEmpty()) {
            Optional<String> latestMonth = kpiRepository.findLatestRecordMonth();
            if (latestMonth.isPresent()) {
                dataMonth = latestMonth.get();
                kpis = kpiRepository.findByRecordMonth(dataMonth);
            }
        }
        
        // 聚合计算
        BigDecimal avgOee = kpis.stream()
            .map(AssetKpiEntity::getOeeValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(1, kpis.size())), 2, BigDecimal.ROUND_HALF_UP);
            
        BigDecimal totalCost = kpis.stream()
            .map(AssetKpiEntity::getMaintenanceCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        BigDecimal avgMtbf = kpis.stream()
            .map(AssetKpiEntity::getMtbfHours)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(1, kpis.size())), 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal avgMttr = kpis.stream()
            .map(AssetKpiEntity::getMttrHours)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(1, kpis.size())), 2, BigDecimal.ROUND_HALF_UP);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("avgOee", avgOee);
        dashboard.put("totalCost", totalCost);
        dashboard.put("avgMtbf", avgMtbf);
        dashboard.put("avgMttr", avgMttr);
        dashboard.put("kpiList", kpis);
        dashboard.put("dataMonth", dataMonth);
        
        return dashboard;
    }

    public List<Map<String, Object>> getOeeTrend(int months) {
        /**
         * 获取最近N个月的OEE趋势数据（按月聚合）
         * @param months 最近月份数（建议1-36）
         * @return 趋势点列表（month/avgOee/avgMtbf/avgMttr/totalCost）
         */
        int safeMonths = Math.min(Math.max(months, 1), 36);
        YearMonth endMonth = YearMonth.now();
        YearMonth startMonth = endMonth.minusMonths(safeMonths - 1L);

        String startMonthStr = startMonth.toString();
        String endMonthStr = endMonth.toString();

        List<AssetKpiEntity> kpis = kpiRepository.findByRecordMonthBetween(startMonthStr, endMonthStr);
        Map<String, List<AssetKpiEntity>> byMonth = kpis.stream()
            .collect(Collectors.groupingBy(AssetKpiEntity::getRecordMonth));

        List<Map<String, Object>> result = new ArrayList<>();
        YearMonth cursor = startMonth;
        while (!cursor.isAfter(endMonth)) {
            String month = cursor.toString();
            result.add(buildMonthlyKpiPoint(month, byMonth.getOrDefault(month, List.of())));
            cursor = cursor.plusMonths(1);
        }
        return result;
    }

    public Map<String, Object> getFaultSummary(LocalDate startDate, LocalDate endDate) {
        /**
         * 获取故障分析汇总数据
         * @param startDate 开始日期（含）
         * @param endDate 结束日期（含）
         * @return 汇总结果（byType/byStatus/topEquipment/total）
         */
        LocalDate safeEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate safeStart = startDate != null ? startDate : safeEnd.minusDays(29);

        LocalDateTime startTime = safeStart.atStartOfDay();
        LocalDateTime endTime = safeEnd.plusDays(1).atStartOfDay().minusNanos(1);

        List<FaultRecordEntity> records = faultRecordRepository.findByReportTimeBetween(startTime, endTime);
        Map<String, Long> byType = records.stream()
            .collect(Collectors.groupingBy(r -> normalizeString(r.getType(), "未知"), Collectors.counting()));
        Map<String, Long> byStatus = records.stream()
            .collect(Collectors.groupingBy(r -> normalizeString(r.getStatus(), "未知"), Collectors.counting()));
        Map<String, Long> byEquipment = records.stream()
            .collect(Collectors.groupingBy(r -> normalizeString(r.getEquipmentName(), "未知设备"), Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        // 字段名禁用total：避免前端DataTransformer将响应误判为分页数据而丢弃其他字段
        result.put("totalCount", records.size());
        result.put("byType", toNameValueList(byType, 20));
        result.put("byStatus", toNameValueList(byStatus, 20));
        result.put("topEquipment", toNameValueList(byEquipment, 10));
        result.put("start", safeStart.toString());
        result.put("end", safeEnd.toString());
        return result;
    }

    public Map<String, Object> getCostSummary(LocalDate startDate, LocalDate endDate) {
        /**
         * 获取维护成本分析汇总数据
         * @param startDate 开始日期（含）
         * @param endDate 结束日期（含）
         * @return 汇总结果（totalCost/byType/byMonth/topEquipment）
         */
        LocalDate safeEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate safeStart = startDate != null ? startDate : safeEnd.minusDays(29);

        LocalDateTime startTime = safeStart.atStartOfDay();
        LocalDateTime endTime = safeEnd.plusDays(1).atStartOfDay().minusNanos(1);

        List<MaintenanceRecordEntity> records = maintenanceRecordRepository.findByMaintenanceTimeBetween(startTime, endTime);

        BigDecimal totalCost = records.stream()
            .map(r -> r.getCost() != null ? r.getCost() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> byType = records.stream()
            .collect(Collectors.groupingBy(
                r -> normalizeString(r.getType(), "未知"),
                Collectors.mapping(
                    r -> r.getCost() != null ? r.getCost() : BigDecimal.ZERO,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));

        Map<YearMonth, BigDecimal> byMonth = records.stream()
            .filter(r -> r.getMaintenanceTime() != null)
            .collect(Collectors.groupingBy(
                r -> YearMonth.from(r.getMaintenanceTime()),
                Collectors.mapping(
                    r -> r.getCost() != null ? r.getCost() : BigDecimal.ZERO,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));

        Map<String, BigDecimal> byEquipment = records.stream()
            .collect(Collectors.groupingBy(
                r -> normalizeString(r.getEquipmentName(), "未知设备"),
                Collectors.mapping(
                    r -> r.getCost() != null ? r.getCost() : BigDecimal.ZERO,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                )
            ));

        YearMonth startMonth = YearMonth.from(safeStart);
        YearMonth endMonth = YearMonth.from(safeEnd);

        Map<String, Object> result = new HashMap<>();
        result.put("totalCost", totalCost);
        result.put("byType", toNameAmountList(byType, 20));
        result.put("byMonth", buildMonthAmountSeries(startMonth, endMonth, byMonth));
        result.put("topEquipment", toNameAmountList(byEquipment, 10));
        result.put("start", safeStart.toString());
        result.put("end", safeEnd.toString());
        return result;
    }

    private Map<String, Object> buildMonthlyKpiPoint(String month, List<AssetKpiEntity> kpis) {
        /**
         * 构建单月KPI趋势点
         * @param month 月份（YYYY-MM）
         * @param kpis KPI列表
         * @return 趋势点
         */
        BigDecimal avgOee = averageBigDecimal(kpis.stream().map(AssetKpiEntity::getOeeValue).collect(Collectors.toList()), 2);
        BigDecimal avgMtbf = averageBigDecimal(kpis.stream().map(AssetKpiEntity::getMtbfHours).collect(Collectors.toList()), 2);
        BigDecimal avgMttr = averageBigDecimal(kpis.stream().map(AssetKpiEntity::getMttrHours).collect(Collectors.toList()), 2);
        BigDecimal totalCost = kpis.stream()
            .map(AssetKpiEntity::getMaintenanceCost)
            .filter(v -> v != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> point = new HashMap<>();
        point.put("month", month);
        point.put("avgOee", avgOee);
        point.put("avgMtbf", avgMtbf);
        point.put("avgMttr", avgMttr);
        point.put("totalCost", totalCost);
        return point;
    }

    private BigDecimal averageBigDecimal(List<BigDecimal> values, int scale) {
        /**
         * 计算BigDecimal列表平均值
         * @param values 值列表
         * @param scale 保留小数位
         * @return 平均值
         */
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }
        BigDecimal sum = values.stream().filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal divisor = BigDecimal.valueOf(Math.max(1, values.stream().filter(v -> v != null).count()));
        return sum.divide(divisor, scale, RoundingMode.HALF_UP);
    }

    private String normalizeString(String value, String defaultValue) {
        /**
         * 规范化字符串，空值替换为默认值
         * @param value 原值
         * @param defaultValue 默认值
         * @return 规范化后的值
         */
        if (value == null) return defaultValue;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }

    private List<Map<String, Object>> toNameValueList(Map<String, Long> data, int limit) {
        /**
         * 将{name->count}转换为前端通用{name,value}数组
         * @param data 分组计数Map
         * @param limit 最大返回条数
         * @return 列表
         */
        return data.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(Math.max(1, limit))
            .map(e -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", e.getKey());
                item.put("value", e.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> toNameAmountList(Map<String, BigDecimal> data, int limit) {
        /**
         * 将{name->amount}转换为前端通用{name,value}数组
         * @param data 分组汇总Map
         * @param limit 最大返回条数
         * @return 列表
         */
        return data.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(Math.max(1, limit))
            .map(e -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", e.getKey());
                item.put("value", e.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildMonthAmountSeries(
        YearMonth startMonth,
        YearMonth endMonth,
        Map<YearMonth, BigDecimal> data
    ) {
        /**
         * 构建按月份连续的金额序列（缺失月份补0）
         * @param startMonth 开始月份
         * @param endMonth 结束月份
         * @param data 按月份汇总数据
         * @return [{month,value}]
         */
        if (startMonth.isAfter(endMonth)) {
            YearMonth tmp = startMonth;
            startMonth = endMonth;
            endMonth = tmp;
        }
        YearMonth finalStart = startMonth;
        YearMonth finalEnd = endMonth;

        List<Map<String, Object>> series = new ArrayList<>();
        YearMonth cursor = finalStart;
        while (!cursor.isAfter(finalEnd)) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", cursor.toString());
            item.put("value", data.getOrDefault(cursor, BigDecimal.ZERO));
            series.add(item);
            cursor = cursor.plusMonths(1);
        }
        return series;
    }
    
    public AssetKpiEntity saveKpi(AssetKpiEntity kpi) {
        /**
         * 保存单条KPI记录
         * @param kpi KPI实体
         * @return 保存后的实体
         */
        return kpiRepository.save(kpi);
    }
    
    // 模拟计算逻辑 - 实际应基于故障记录和维护记录计算
    public void calculateMonthlyKpi(String month) {
        // TODO: Implement calculation logic based on FaultRecord and MaintenanceRecord
    }
}
