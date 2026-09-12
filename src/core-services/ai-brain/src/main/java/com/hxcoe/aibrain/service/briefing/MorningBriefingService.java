package com.hxcoe.aibrain.service.briefing;

import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.SuggestionService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * AI 晨会简报引擎（S05，六域巡检）
 * 六个角色 Agent 并行巡检各自域，只输出"异常 + 证据 + 建议动作"，无异常不发声。
 * 全部只读跨库聚合（AUTO 级），结果幂等落入 ai_suggestion（eventId=BRIEF-域-日期）。
 */
@Slf4j
@Service
public class MorningBriefingService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SuggestionService suggestionService;

    /**
     * 执行六域巡检并生成简报条目
     *
     * @return 生成的简报条目列表（无异常的域不产生条目）
     */
    public List<AiSuggestionEntity> generate() {
        List<AiSuggestionEntity> items = new ArrayList<>();
        addIfPresent(items, inspectPlanner());
        addIfPresent(items, inspectEquipment());
        addIfPresent(items, inspectProcurement());
        addIfPresent(items, inspectQuality());
        addIfPresent(items, inspectLogistics());
        addIfPresent(items, inspectEnergy());
        int published = 0;
        for (AiSuggestionEntity item : items) {
            if (suggestionService.publish(item)) {
                published++;
            }
        }
        log.info("AI晨会简报: 巡检6域, 发现异常{}条, 新增发布{}条", items.size(), published);
        return items;
    }

    private void addIfPresent(List<AiSuggestionEntity> items, AiSuggestionEntity item) {
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * 计划员 Agent：MES 今日工单达成与延期
     */
    private AiSuggestionEntity inspectPlanner() {
        MapSqlParameterSource p = new MapSqlParameterSource();
        p.addValue("dayStart", LocalDate.now().atStartOfDay());
        p.addValue("dayEnd", LocalDate.now().plusDays(1).atStartOfDay());
        Long todayCount = queryLong("SELECT COUNT(*) FROM mes_db.mes_work_order WHERE start_time >= :dayStart AND start_time < :dayEnd", p);
        Long overdue = queryLong("SELECT COUNT(*) FROM mes_db.mes_work_order WHERE end_time < NOW() "
                + "AND (status IS NULL OR (UPPER(status) NOT LIKE '%COMPLETE%' AND UPPER(status) NOT LIKE '%DONE%' AND status NOT LIKE '%完成%'))", p);
        if (overdue == 0 && todayCount == 0) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("todayWorkOrders", todayCount);
        metrics.put("overdueWorkOrders", overdue);
        return buildItem("PLANNER", "MES", overdue > 0 ? "WARNING" : "INFO",
                String.format("生产计划：今日工单 %d 张，延期未完成 %d 张", todayCount, overdue),
                overdue > 0 ? "建议核查延期工单资源与物料到位情况" : "今日排产正常",
                List.of("mes_db.mes_work_order"), metrics);
    }

    /**
     * 设备工程师 Agent：EAM 设备健康与未闭环故障
     */
    private AiSuggestionEntity inspectEquipment() {
        Long faultEquipment = queryLong("SELECT COUNT(*) FROM eam_db.eam_asset WHERE UPPER(status) IN ('FAULT','MAINTENANCE','BROKEN') OR status IN ('故障','维修中')", new MapSqlParameterSource());
        Long openFaults = queryLong("SELECT COUNT(*) FROM eam_db.eam_fault_record WHERE status IS NULL OR (UPPER(status) NOT IN ('CLOSED','RESOLVED') AND status NOT IN ('已关闭','已解决'))", new MapSqlParameterSource());
        Long dqOpen = queryLong("SELECT COUNT(*) FROM eam_db.eam_data_clean_task WHERE status='OPEN'", new MapSqlParameterSource());
        if (faultEquipment == 0 && openFaults == 0) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("faultEquipment", faultEquipment);
        metrics.put("openFaultRecords", openFaults);
        metrics.put("dataQualityOpenTasks", dqOpen);
        return buildItem("EQUIPMENT", "EAM", openFaults > 0 ? "CRITICAL" : "WARNING",
                String.format("设备健康：故障/维修中设备 %d 台，未闭环故障记录 %d 条", faultEquipment, openFaults),
                openFaults > 0 ? "建议优先处理未闭环故障，可调用维修Copilot检索历史方案" : "关注故障设备修复进度",
                List.of("eam_db.eam_asset", "eam_db.eam_fault_record", "eam_db.eam_data_clean_task"), metrics);
    }

    /**
     * 采购员 Agent：SRM 在途订单逾期
     */
    private AiSuggestionEntity inspectProcurement() {
        List<Map<String, Object>> overdueOrders = jdbcTemplate.queryForList(
                "SELECT order_no, supplier_name, expected_delivery_date FROM srm_db.srm_purchase_order "
                        + "WHERE expected_delivery_date < NOW() "
                        + "AND (status IS NULL OR (UPPER(status) NOT LIKE '%COMPLETE%' AND UPPER(status) NOT LIKE '%RECEIVED%' "
                        + "AND UPPER(status) NOT LIKE '%CANCEL%' AND status NOT LIKE '%完成%' AND status NOT LIKE '%到货%')) "
                        + "ORDER BY expected_delivery_date ASC LIMIT 5",
                new MapSqlParameterSource());
        if (overdueOrders.isEmpty()) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("overduePurchaseOrders", overdueOrders.size());
        metrics.put("samples", overdueOrders);
        return buildItem("PROCUREMENT", "SRM", "WARNING",
                String.format("采购在途：%d 张采购订单已超预计交期未到货", overdueOrders.size()),
                "建议催货或启用备选供应商（详见交期风险预测）",
                List.of("srm_db.srm_purchase_order"), metrics);
    }

    /**
     * 质量工程师 Agent：QMS 待处置不合格品
     */
    private AiSuggestionEntity inspectQuality() {
        Long pendingNc = queryLong("SELECT COUNT(*) FROM qms_db.qms_nc_registration "
                + "WHERE disposal_status IS NULL OR disposal_status IN ('pending','PENDING','待处置')", new MapSqlParameterSource());
        Long recentNc = queryLong("SELECT COUNT(*) FROM qms_db.qms_nc_registration WHERE registration_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)", new MapSqlParameterSource());
        if (pendingNc == 0 && recentNc == 0) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("pendingNcCount", pendingNc);
        metrics.put("last7DaysNcCount", recentNc);
        return buildItem("QUALITY", "QMS", pendingNc > 0 ? "WARNING" : "INFO",
                String.format("质量：待处置不合格品 %d 单，近7天新登记 %d 单", pendingNc, recentNc),
                pendingNc > 0 ? "建议加快不合格品处置流程" : "近7天质量平稳",
                List.of("qms_db.qms_nc_registration"), metrics);
    }

    /**
     * 仓储物流 Agent：LES 在途与异常事件
     */
    private AiSuggestionEntity inspectLogistics() {
        Long inTransit = queryLong("SELECT COUNT(*) FROM les_db.les_transport_task WHERE status IN ('pending','in_transit')", new MapSqlParameterSource());
        Long anomalies = queryLong("SELECT COUNT(*) FROM les_db.les_anomaly_event WHERE handling_status IS NULL OR UPPER(handling_status) NOT IN ('RESOLVED','CLOSED','已处理','已关闭')", new MapSqlParameterSource());
        if (anomalies == 0) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("inTransitTasks", inTransit);
        metrics.put("unresolvedAnomalies", anomalies);
        return buildItem("LOGISTICS", "LES", "WARNING",
                String.format("物流：在途运输任务 %d 个，未处理异常事件 %d 起", inTransit, anomalies),
                "建议核查异常事件并更新运输任务状态",
                List.of("les_db.les_transport_task", "les_db.les_anomaly_event"), metrics);
    }

    /**
     * 能源专员 Agent：EMS 昨日能耗对标近7日均值
     */
    private AiSuggestionEntity inspectEnergy() {
        Double yesterday = queryDouble("SELECT COALESCE(SUM(actual_value),0) FROM ems_db.ems_real_time_data "
                + "WHERE collection_time >= CURDATE() - INTERVAL 1 DAY AND collection_time < CURDATE()", new MapSqlParameterSource());
        Double avg7d = queryDouble("SELECT COALESCE(SUM(actual_value),0)/7 FROM ems_db.ems_real_time_data "
                + "WHERE collection_time >= CURDATE() - INTERVAL 7 DAY AND collection_time < CURDATE()", new MapSqlParameterSource());
        if (avg7d == 0) {
            return null;
        }
        // 昨日无采集数据 ≠ 能耗为零：识别为数据缺失（数据质量问题），而非能耗下降100%
        if (yesterday == 0) {
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("avg7dConsumption", Math.round(avg7d * 100.0) / 100.0);
            return buildItem("ENERGY", "EMS", "WARNING",
                    "能耗：昨日无采集数据，疑似采集中断（近7日均值 " + Math.round(avg7d * 100.0) / 100.0 + "）",
                    "建议核查EMS数据采集链路与SCADA推送任务",
                    List.of("ems_db.ems_real_time_data"), metrics);
        }
        double deviation = (yesterday - avg7d) / avg7d * 100.0;
        if (Math.abs(deviation) < 15.0) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("yesterdayConsumption", Math.round(yesterday * 100.0) / 100.0);
        metrics.put("avg7dConsumption", Math.round(avg7d * 100.0) / 100.0);
        metrics.put("deviationPercent", Math.round(deviation * 10.0) / 10.0);
        return buildItem("ENERGY", "EMS", deviation > 0 ? "WARNING" : "INFO",
                String.format("能耗：昨日能耗较近7日均值%s %.1f%%", deviation > 0 ? "上升" : "下降", Math.abs(deviation)),
                deviation > 0 ? "建议核查高耗设备运行与排产计划（详见能耗负荷预测）" : "能耗低于均值，运行良好",
                List.of("ems_db.ems_real_time_data"), metrics);
    }

    /**
     * 构造简报条目（统一信封要素齐全：类型/模块/标题/证据/动作建议）
     */
    private AiSuggestionEntity buildItem(String domain, String module, String severity,
                                         String title, String actionAdvice,
                                         List<String> sources, Map<String, Object> metrics) {
        AiSuggestionEntity item = new AiSuggestionEntity();
        item.setSuggestionType("MORNING_BRIEFING");
        item.setModule(module);
        item.setSeverity(severity);
        item.setTitle(title);
        item.setAutomationLevel("AUTO");
        item.setEventId("BRIEF-" + domain + "-" + LocalDate.now());
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("domain", domain);
        analysis.put("actionAdvice", actionAdvice);
        analysis.put("evidence", Map.of("sources", sources, "metrics", metrics));
        try {
            item.setAnalysis(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(analysis));
        } catch (Exception e) {
            item.setAnalysis("{}");
        }
        return item;
    }

    private Long queryLong(String sql, MapSqlParameterSource params) {
        try {
            Long v = jdbcTemplate.queryForObject(sql, params, Long.class);
            return v == null ? 0L : v;
        } catch (Exception e) {
            log.warn("晨会巡检查询失败: {} - {}", sql, e.getMessage());
            return 0L;
        }
    }

    private Double queryDouble(String sql, MapSqlParameterSource params) {
        try {
            Double v = jdbcTemplate.queryForObject(sql, params, Double.class);
            return v == null ? 0.0 : v;
        } catch (Exception e) {
            log.warn("晨会巡检查询失败: {} - {}", sql, e.getMessage());
            return 0.0;
        }
    }
}
