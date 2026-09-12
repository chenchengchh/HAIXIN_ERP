package com.hxcoe.aibrain.service.prediction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.OrchestratorService;
import com.hxcoe.aibrain.service.SkillParamService;
import com.hxcoe.aibrain.service.SuggestionService;
import com.hxcoe.aibrain.service.evidence.Evidence;
import com.hxcoe.aibrain.service.expert.ExpertFinding;
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
 * 设备故障预测引擎（S01，规则版时序算法，无 LLM）
 * 算法：近3天滑动均值 vs 前6天基线均值的漂移检测 + 线性外推到达报警阈值天数（Weibull 简化为趋势外推）
 * 数据：scada_tag_value 时序 + scada_alarm_rule 阈值 + eam_asset/eam_fault_record 关联
 * 输出：AUTO 级预测建议落 ai_suggestion（附 CONFIRM 级工单草稿动作）
 */
@Slf4j
@Service
public class FaultPredictionService {

    /** 漂移告警默认阈值：近3天均值相对基线变化超过该比例才进入外推（已外置 ai_skill_param，学习环可调优） */
    private static final double DEFAULT_DRIFT_THRESHOLD = 0.10;

    /** 外推到达阈值在 30 天内才生成预测建议 */
    private static final double MAX_DAYS_TO_LIMIT = 30.0;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private SkillParamService skillParamService;

    @Autowired
    private OrchestratorService orchestratorService;

    /** Spring 管理的 ObjectMapper（已注册 JavaTimeModule，可序列化跨域会诊结论中的日期时间类型） */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 执行全点位故障预测扫描
     *
     * @return 生成的预测建议列表
     */
    public List<AiSuggestionEntity> predict() {
        // 1. 读取启用中的报警规则（含阈值）
        List<Map<String, Object>> rules = jdbcTemplate.queryForList(
                "SELECT r.tag_code, r.alarm_name, r.high, r.high_high, r.low, r.low_low, p.device_name, p.unit "
                        + "FROM scada_db.scada_alarm_rule r "
                        + "LEFT JOIN scada_db.scada_collect_point p ON p.tag_code = r.tag_code "
                        + "WHERE r.enabled = 1",
                new MapSqlParameterSource());

        List<AiSuggestionEntity> suggestions = new java.util.ArrayList<>();
        for (Map<String, Object> rule : rules) {
            AiSuggestionEntity s = evaluatePoint(rule);
            if (s != null && suggestionService.publish(s)) {
                suggestions.add(s);
            }
        }
        log.info("设备故障预测: 扫描{}个点位, 新增预测{}条", rules.size(), suggestions.size());
        return suggestions;
    }

    /**
     * 评估单点位的漂移趋势并外推阈值到达时间
     */
    private AiSuggestionEntity evaluatePoint(Map<String, Object> rule) {
        String tagCode = String.valueOf(rule.get("tag_code"));
        MapSqlParameterSource p = new MapSqlParameterSource();
        p.addValue("tagCode", tagCode);

        // 2. 近3天均值 与 前6天基线均值（窗口对齐，避免不同周期长度造成偏差）
        Double recentAvg = queryDouble("SELECT AVG(value) FROM scada_db.scada_tag_value "
                + "WHERE tag_code = :tagCode AND ts >= DATE_SUB(NOW(), INTERVAL 3 DAY)", p);
        Double baseAvg = queryDouble("SELECT AVG(value) FROM scada_db.scada_tag_value "
                + "WHERE tag_code = :tagCode AND ts >= DATE_SUB(NOW(), INTERVAL 9 DAY) AND ts < DATE_SUB(NOW(), INTERVAL 3 DAY)", p);
        if (recentAvg == null || baseAvg == null || baseAvg == 0) {
            return null;
        }

        double drift = (recentAvg - baseAvg) / Math.abs(baseAvg);
        double driftThreshold = skillParamService.getDouble("FAULT_PREDICTION", "drift_threshold", DEFAULT_DRIFT_THRESHOLD);
        if (Math.abs(drift) < driftThreshold) {
            return null;
        }

        // 3. 线性外推：日均斜率（两个窗口中点间隔约6天）
        Double high = toDouble(rule.get("high"));
        Double low = toDouble(rule.get("low"));
        double slopePerDay = (recentAvg - baseAvg) / 6.0;
        Double daysToLimit = null;
        String direction;
        if (slopePerDay > 0 && high != null && recentAvg < high) {
            daysToLimit = (high - recentAvg) / slopePerDay;
            direction = "上行";
        } else if (slopePerDay < 0 && low != null && recentAvg > low) {
            daysToLimit = (low - recentAvg) / slopePerDay;
            direction = "下行";
        } else {
            return null;
        }
        if (daysToLimit == null || daysToLimit <= 0 || daysToLimit > MAX_DAYS_TO_LIMIT) {
            return null;
        }

        // 4. 关联 EAM 设备历史故障（device_name 模糊匹配 eam_asset.name）
        String deviceName = rule.get("device_name") == null ? "" : String.valueOf(rule.get("device_name"));
        p.addValue("deviceName", "%" + deviceName + "%");
        Long faultCount = 0L;
        if (!deviceName.isEmpty()) {
            Long v = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM eam_db.eam_fault_record f JOIN eam_db.eam_asset a ON a.id = f.equipment_id "
                            + "WHERE a.name LIKE :deviceName AND f.report_time >= DATE_SUB(NOW(), INTERVAL 180 DAY)",
                    p, Long.class);
            faultCount = v == null ? 0L : v;
        }

        // 5. 组装建议（含证据链与 CONFIRM 级动作草稿）
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("tagCode", tagCode);
        metrics.put("recentAvg", round2(recentAvg));
        metrics.put("baseAvg", round2(baseAvg));
        metrics.put("driftPercent", round2(drift * 100));
        metrics.put("daysToLimit", round2(daysToLimit));
        metrics.put("threshold", "上行".equals(direction) ? high : low);
        metrics.put("unit", rule.get("unit"));
        metrics.put("historyFaultCount180d", faultCount);

        AiSuggestionEntity s = new AiSuggestionEntity();
        s.setSuggestionType("FAULT_PREDICTION");
        s.setModule("EAM");
        s.setSeverity(daysToLimit <= 7 ? "CRITICAL" : "WARNING");
        s.setTitle(String.format("%s：%s %s趋势异常，漂移 %.1f%%，预计 %.0f 天后触达阈值",
                deviceName.isEmpty() ? tagCode : deviceName, rule.get("alarm_name"), direction, drift * 100, daysToLimit));
        s.setAutomationLevel("CONFIRM");
        s.setEventId("FAULT-PRED-" + tagCode + "-" + LocalDate.now());

        Map<String, Object> action = new HashMap<>();
        action.put("type", "CREATE_MAINTENANCE_WORKORDER");
        action.put("endpoint", "/api/v1/eam/maintenance/records");
        action.put("method", "POST");
        // payload 对齐 MaintenanceRecordEntity 核心字段（设备名/类型/内容/维护人/建议维护时间）
        Map<String, Object> payload = new HashMap<>();
        payload.put("equipmentName", deviceName.isEmpty() ? tagCode : deviceName);
        payload.put("type", "预防性维护");
        payload.put("content", String.format("AI故障预测：%s %s趋势漂移 %.1f%%，预计 %.0f 天后触达阈值（%s），建议提前点检关联部件",
                deviceName.isEmpty() ? tagCode : deviceName, rule.get("alarm_name"), drift * 100, daysToLimit,
                "上行".equals(direction) ? "高限 " + high : "低限 " + low));
        payload.put("maintainer", "AI决策引擎");
        payload.put("maintenanceTime", java.time.LocalDateTime.now()
                .plusDays((long) Math.max(1, Math.ceil(daysToLimit)))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        action.put("payload", payload);
        action.put("description", "创建预防性维护工单（人工确认后执行，执行成功可在EAM维修记录列表查看）");
        action.put("confirmText", "确认为 " + (deviceName.isEmpty() ? tagCode : deviceName) + " 创建预防性维护工单？");
        action.put("rollbackHint", "维修记录创建后如需撤销，可在 EAM 维修记录列表删除");
        s.setActionDraft(toJson(action));

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("algorithm", "滑动窗口漂移检测+线性阈值外推");
        // P4-5 结构化证据：行级引用 + 下钻路由 + 推理步骤（旧格式为表名字符串）
        analysis.put("evidence", Evidence.of(
                List.of(
                        Evidence.source("scada_db", "scada_tag_value",
                                "tag_code='" + tagCode + "' AND ts>=近9天", null,
                                "/home/scada/historical-data?tagCode=" + tagCode),
                        Evidence.source("scada_db", "scada_alarm_rule",
                                "tag_code='" + tagCode + "' AND enabled=1", null,
                                "/home/scada/alarm-management"),
                        Evidence.source("eam_db", "eam_fault_record",
                                "设备" + deviceName + " 180天故障史", faultCount.intValue(),
                                "/home/eam/asset-ledger")),
                metrics,
                List.of(
                        "近3天均值 " + round2(recentAvg) + " " + rule.get("unit"),
                        "前6天基线均值 " + round2(baseAvg) + " " + rule.get("unit"),
                        String.format("漂移 %.1f%% > 阈值 %.0f%%", drift * 100, driftThreshold * 100),
                        String.format("斜率外推 %.0f 天后触达%s限 %.1f", daysToLimit, direction,
                                "上行".equals(direction) ? high : low))));
        analysis.put("actionAdvice", String.format("建议在 %.0f 天窗口内安排点检，重点核查 %s 关联部件", daysToLimit, rule.get("alarm_name")));

        // P4-6 多专家联动：CRITICAL 级（7天内触达阈值）自动召集跨域会诊，富化为组合建议
        if (daysToLimit <= 7) {
            enrichWithCrossDomain(s, analysis, deviceName.isEmpty() ? tagCode : deviceName);
        }
        s.setAnalysis(toJson(analysis));
        return s;
    }

    /**
     * CRITICAL 故障预测的跨域富化（P4-6 技能联动）
     * 并行召集设备/库存/采购/生产四专家，结论写入 analysis.crossDomain；
     * 专家补充动作（如紧急采购申请）写入 analysis.secondaryActions，经 P4-1 引擎按 actionIndex 执行
     *
     * @param s          建议实体（标题升级为组合建议）
     * @param analysis   分析对象（就地富化）
     * @param deviceName 设备名称
     */
    private void enrichWithCrossDomain(AiSuggestionEntity s, Map<String, Object> analysis, String deviceName) {
        try {
            List<ExpertFinding> findings = orchestratorService.consultCrossDomain(null, deviceName);
            analysis.put("crossDomain", findings);
            String comboRisk = orchestratorService.crossValidate(findings);
            if (comboRisk != null) {
                analysis.put("crossDomainRisk", comboRisk);
                s.setTitle(s.getTitle() + "｜" + comboRisk);
            }
            List<Map<String, Object>> secondaryActions = findings.stream()
                    .filter(f -> f.getActionDraft() != null)
                    .map(f -> {
                        Map<String, Object> a = new HashMap<>(f.getActionDraft());
                        a.put("domain", f.getDomain());
                        return a;
                    })
                    .toList();
            if (!secondaryActions.isEmpty()) {
                analysis.put("secondaryActions", secondaryActions);
            }
            log.info("S01跨域联动: device={}, 专家结论={}, 补充动作={}", deviceName,
                    findings.stream().map(f -> f.getDomain() + ":" + f.getStatus()).toList(), secondaryActions.size());
        } catch (Exception e) {
            // 联动失败不阻塞主建议生成
            log.warn("S01跨域联动失败（主建议不受影响）: {}", e.getMessage());
        }
    }

    private Double queryDouble(String sql, MapSqlParameterSource params) {
        try {
            return jdbcTemplate.queryForObject(sql, params, Double.class);
        } catch (Exception e) {
            return null;
        }
    }

    private Double toDouble(Object v) {
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        return null;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            // 序列化失败必须留痕（此前静默返回 {} 导致跨域富化内容丢失且无法定位）
            log.warn("建议内容序列化失败: {}", e.getMessage());
            return "{}";
        }
    }
}
