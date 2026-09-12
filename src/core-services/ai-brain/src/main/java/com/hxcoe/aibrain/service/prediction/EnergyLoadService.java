package com.hxcoe.aibrain.service.prediction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.SkillParamService;
import com.hxcoe.aibrain.service.SuggestionService;
import com.hxcoe.aibrain.service.evidence.Evidence;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 能耗负荷预测（S04，规则版）
 * 数据链路：EMS 历史能耗按小时聚合曲线 + MES 明日排产
 * 输出：峰值时段预测与班次调整建议（AUTO 级建议）
 */
@Slf4j
@Service
public class EnergyLoadService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SkillParamService skillParamService;

    /**
     * 预测明日能耗负荷（基于近30天同时段均值与历史峰值）
     *
     * @return 生成的预测建议（无风险返回 null）
     */
    public AiSuggestionEntity predictTomorrow() {
        MapSqlParameterSource p = new MapSqlParameterSource();

        // 1. 近30天按小时聚合的平均负荷曲线
        List<Map<String, Object>> hourlyProfile = jdbcTemplate.queryForList(
                "SELECT HOUR(collection_time) AS hr, AVG(actual_value) AS avg_load, MAX(actual_value) AS max_load "
                        + "FROM ems_db.ems_real_time_data "
                        + "WHERE collection_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
                        + "GROUP BY HOUR(collection_time) ORDER BY avg_load DESC LIMIT 3",
                p);
        if (hourlyProfile.isEmpty()) {
            return null;
        }

        // 2. 历史峰值（P95 近似取最大值的90%分位参考线）
        Double p95 = jdbcTemplate.queryForObject(
                "SELECT AVG(top_vals.v) FROM (SELECT actual_value AS v FROM ems_db.ems_real_time_data "
                        + "WHERE collection_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
                        + "ORDER BY actual_value DESC LIMIT 20) top_vals",
                p, Double.class);
        if (p95 == null || p95 <= 0) {
            return null;
        }

        // 3. 明日排产强度（明日工单数）
        Long tomorrowOrders = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mes_db.mes_work_order "
                        + "WHERE start_time >= CURDATE() + INTERVAL 1 DAY AND start_time < CURDATE() + INTERVAL 2 DAY",
                p, Long.class);
        tomorrowOrders = tomorrowOrders == null ? 0 : tomorrowOrders;

        // 4. 峰值时段均值是否接近历史峰值参考线（默认>85% 视为超载风险，学习环可自动调优）
        double peakLoadRatio = skillParamService.getDouble("ENERGY_LOAD", "peak_load_ratio", 0.85);
        Map<String, Object> peakHour = hourlyProfile.get(0);
        double peakAvg = ((Number) peakHour.get("avg_load")).doubleValue();
        double ratio = peakAvg / p95;
        if (ratio < peakLoadRatio && tomorrowOrders == 0) {
            return null;
        }

        int peakHr = ((Number) peakHour.get("hr")).intValue();
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("peakHour", peakHr + ":00-" + (peakHr + 2) + ":00");
        metrics.put("peakAvgLoad", Math.round(peakAvg * 100.0) / 100.0);
        metrics.put("p95Reference", Math.round(p95 * 100.0) / 100.0);
        metrics.put("loadRatio", Math.round(ratio * 1000.0) / 10.0);
        metrics.put("tomorrowWorkOrders", tomorrowOrders);
        metrics.put("topHours", hourlyProfile);

        AiSuggestionEntity s = new AiSuggestionEntity();
        s.setSuggestionType("ENERGY_LOAD");
        s.setModule("EMS");
        s.setSeverity(ratio >= 0.95 ? "CRITICAL" : "WARNING");
        s.setTitle(String.format("能耗负荷预测：明日高峰 %d:00-%d:00 预计达历史峰值参考线 %.0f%%，明日排产 %d 张工单",
                peakHr, peakHr + 2, ratio * 100, tomorrowOrders));
        s.setAutomationLevel("AUTO");
        s.setEventId("ENERGY-LOAD-" + LocalDate.now().plusDays(1));

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("actionAdvice", ratio >= 0.85
                ? String.format("建议将部分班次移出 %d:00-%d:00 高峰时段，降低需量电费", peakHr, peakHr + 2)
                : "明日负荷处于正常区间");
        // P4-5 结构化证据：行级引用 + 下钻路由 + 推理步骤
        analysis.put("evidence", Evidence.of(
                List.of(
                        Evidence.source("ems_db", "ems_real_time_data",
                                "近30天按小时聚合负荷曲线", null,
                                "/home/ems/energy-analysis"),
                        Evidence.source("mes_db", "mes_work_order",
                                "明日排产工单", tomorrowOrders.intValue(),
                                "/home/mes/execution")),
                metrics,
                List.of(
                        String.format("峰值时段均值 %.2f（近30天 %d:00 档）", peakAvg, peakHr),
                        String.format("历史峰值参考线 P95=%.2f", p95),
                        String.format("负荷比 %.1f%%（阈值 %.0f%%）", ratio * 100, peakLoadRatio * 100),
                        tomorrowOrders > 0 ? "明日排产 " + tomorrowOrders + " 张工单，负荷叠加" : "明日无新增排产")));
        try {
            s.setAnalysis(objectMapper.writeValueAsString(analysis));
        } catch (Exception e) {
            s.setAnalysis("{}");
        }
        suggestionService.publish(s);
        log.info("能耗负荷预测: 峰值时段{}:00, 负荷比{}%", peakHr, Math.round(ratio * 100));
        return s;
    }
}
