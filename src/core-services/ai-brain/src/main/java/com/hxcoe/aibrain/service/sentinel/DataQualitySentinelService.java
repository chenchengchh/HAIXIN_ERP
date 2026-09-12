package com.hxcoe.aibrain.service.sentinel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.SuggestionService;
import com.hxcoe.aibrain.service.ontology.OntologyQueryService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 数据质量哨兵（S12 全模块推广版）
 * 定时对账各模块关键表，自动发现"上一轮人工修的那类问题"：
 * 跨表对账（EAM备件/ERP凭证）+ 时区巡检 + WMS负库存 + MES滞留工单 + 契约漂移检测。
 * 全部只读检查（AUTO 级只告警），异常幂等落入 ai_suggestion（type=DQ_SENTINEL）。
 */
@Slf4j
@Service
public class DataQualitySentinelService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OntologyQueryService ontology;

    /** 契约基线缓存（classpath:contract-baseline.json，关键表→关键列清单） */
    private volatile Map<String, List<String>> contractBaseline;

    /**
     * 执行全部哨兵检查并发布异常建议
     *
     * @return 本次检查发现的异常条目数（幂等去重后新增数）
     */
    public int scan() {
        List<AiSuggestionEntity> items = new ArrayList<>();
        addIfPresent(items, checkEamSpareStockReconcile());
        addIfPresent(items, checkErpVoucherBalance());
        addIfPresent(items, checkTimezoneDrift());
        addIfPresent(items, checkWmsNegativeStock());
        addIfPresent(items, checkMesStalledOrders());
        addIfPresent(items, checkContractDrift());
        int published = 0;
        for (AiSuggestionEntity item : items) {
            if (suggestionService.publish(item)) {
                published++;
            }
        }
        log.info("数据质量哨兵: 6项检查, 发现异常{}条, 新增发布{}条", items.size(), published);
        return published;
    }

    private void addIfPresent(List<AiSuggestionEntity> items, AiSuggestionEntity item) {
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * 对账1：EAM 备件总库存（spare_part.current_stock）vs 库存明细汇总（spare_inventory）
     */
    private AiSuggestionEntity checkEamSpareStockReconcile() {
        String sql = "SELECT p.code, p.name, p.current_stock AS total_stock, COALESCE(s.detail_sum, 0) AS detail_sum "
                + "FROM eam_db.eam_spare_part p "
                + "LEFT JOIN (SELECT spare_id, SUM(quantity) AS detail_sum FROM eam_db.eam_spare_inventory GROUP BY spare_id) s "
                + "ON p.id = s.spare_id "
                + "WHERE COALESCE(p.current_stock, 0) <> COALESCE(s.detail_sum, 0)";
        List<Map<String, Object>> mismatches = jdbcTemplate.queryForList(sql, new MapSqlParameterSource());
        // P4-4 本体联动：备件两表事实不一致 → spare_part 标记 trusted=0（修复前不对 AI 开放）；复核通过自动解除
        ontology.syncTrust("spare_part", mismatches.isEmpty(),
                "EAM备件对账异常：" + mismatches.size() + " 种备件总库存与明细不一致");
        if (mismatches.isEmpty()) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("mismatchCount", mismatches.size());
        metrics.put("samples", mismatches.stream().limit(3).toList());
        return buildItem("EAM_SPARE_RECONCILE", "EAM", "WARNING",
                "EAM备件对账异常：" + mismatches.size() + " 种备件总库存与库存明细不一致",
                "核查备件库存调整/领用/归还链路是否同步更新两表",
                List.of("eam_db.eam_spare_part", "eam_db.eam_spare_inventory"), metrics);
    }

    /**
     * 对账2：ERP 凭证借贷平衡（每张凭证 SUM(借) = SUM(贷)）
     */
    private AiSuggestionEntity checkErpVoucherBalance() {
        String sql = "SELECT voucher_id, SUM(debit_amount) AS debit_sum, SUM(credit_amount) AS credit_sum "
                + "FROM erp_db.erp_voucher_item GROUP BY voucher_id "
                + "HAVING ABS(SUM(debit_amount) - SUM(credit_amount)) > 0.01 LIMIT 10";
        List<Map<String, Object>> unbalanced = jdbcTemplate.queryForList(sql, new MapSqlParameterSource());
        if (unbalanced.isEmpty()) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("unbalancedCount", unbalanced.size());
        metrics.put("samples", unbalanced.stream().limit(3).toList());
        return buildItem("ERP_VOUCHER_BALANCE", "ERP", "CRITICAL",
                "ERP凭证借贷不平衡：" + unbalanced.size() + " 张凭证借方与贷方合计不等",
                "借贷不平衡属于财务硬约束破坏，建议立即核查凭证录入与自动过账逻辑",
                List.of("erp_db.erp_voucher_item"), metrics);
    }

    /**
     * 时区巡检：抽查关键表是否存在"未来时间"记录（写入时间超过当前时间10分钟=时区配置异常）
     */
    private AiSuggestionEntity checkTimezoneDrift() {
        Map<String, String> checks = new HashMap<>();
        checks.put("eam_db.eam_fault_record", "report_time");
        checks.put("mes_db.mes_work_order", "create_time");
        checks.put("wms_db.wms_inventory_transaction", "create_time");
        List<String> drifted = new ArrayList<>();
        for (Map.Entry<String, String> c : checks.entrySet()) {
            try {
                Long cnt = queryLong("SELECT COUNT(*) FROM " + c.getKey()
                        + " WHERE " + c.getValue() + " > DATE_ADD(NOW(), INTERVAL 10 MINUTE)");
                if (cnt != null && cnt > 0) {
                    drifted.add(c.getKey() + "(" + cnt + "条)");
                }
            } catch (Exception e) {
                log.debug("时区巡检跳过表 {}: {}", c.getKey(), e.getMessage());
            }
        }
        if (drifted.isEmpty()) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("driftedTables", drifted);
        return buildItem("TIMEZONE_DRIFT", "CROSS", "WARNING",
                "时区巡检异常：" + String.join("、", drifted) + " 存在未来时间记录",
                "JDBC serverTimezone 与容器时区不一致，检查各服务 JVM 时区配置",
                List.copyOf(checks.keySet()), metrics);
    }

    /**
     * WMS 负库存巡检（库存量为负属于数据异常）
     */
    private AiSuggestionEntity checkWmsNegativeStock() {
        Long cnt = queryLong("SELECT COUNT(*) FROM wms_db.wms_inventory WHERE COALESCE(quantity, 0) < 0");
        if (cnt == null || cnt == 0) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("negativeRows", cnt);
        return buildItem("WMS_NEGATIVE_STOCK", "WMS", "CRITICAL",
                "WMS负库存异常：" + cnt + " 行库存量为负",
                "负库存意味着出库超发或入库漏记，建议立即盘点并核查出入库流水",
                List.of("wms_db.wms_inventory"), metrics);
    }

    /**
     * MES 滞留工单巡检（进行中但启动超过7天未完工）
     */
    private AiSuggestionEntity checkMesStalledOrders() {
        Long cnt = queryLong("SELECT COUNT(*) FROM mes_db.mes_work_order "
                + "WHERE start_time < DATE_SUB(NOW(), INTERVAL 7 DAY) "
                + "AND (status IS NULL OR (UPPER(status) NOT LIKE '%COMPLETE%' AND UPPER(status) NOT LIKE '%DONE%' AND status NOT LIKE '%完成%'))");
        if (cnt == null || cnt == 0) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("stalledOrders", cnt);
        return buildItem("MES_STALLED_ORDER", "MES", "WARNING",
                "MES滞留工单：" + cnt + " 张工单启动超7天未完工",
                "滞留工单可能关联设备停机/物料短缺，建议结合根因会诊追溯",
                List.of("mes_db.mes_work_order"), metrics);
    }

    /**
     * 契约漂移检测：对比关键表实际列与契约基线（contract-baseline.json），
     * 表不存在或关键列缺失=表结构变更破坏 AI 查询/前端契约，自动预警。
     * 新增列不报警（向后兼容）；基线随表结构正当变更同步维护。
     */
    private AiSuggestionEntity checkContractDrift() {
        Map<String, List<String>> baseline = loadContractBaseline();
        if (baseline.isEmpty()) {
            return null;
        }
        List<String> drifts = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : baseline.entrySet()) {
            String[] parts = e.getKey().split("\\.");
            if (parts.length != 2) {
                continue;
            }
            try {
                List<String> columns = jdbcTemplate.queryForList(
                        "SELECT COLUMN_NAME FROM information_schema.COLUMNS "
                                + "WHERE TABLE_SCHEMA = :schema AND TABLE_NAME = :table",
                        new MapSqlParameterSource()
                                .addValue("schema", parts[0])
                                .addValue("table", parts[1]),
                        String.class);
                if (columns.isEmpty()) {
                    drifts.add(e.getKey() + "(表不存在)");
                    continue;
                }
                Set<String> actual = new HashSet<>(columns);
                List<String> missing = e.getValue().stream().filter(c -> !actual.contains(c)).toList();
                if (!missing.isEmpty()) {
                    drifts.add(e.getKey() + "缺列" + missing);
                }
            } catch (Exception ex) {
                log.debug("契约漂移检测跳过表 {}: {}", e.getKey(), ex.getMessage());
            }
        }
        // P4-4 本体联动：基线表对应注册对象统一同步可信度（漂移→trusted=0，复核通过→自动解除）
        for (String baseTable : baseline.keySet()) {
            String objectCode = ontology.findObjectCodeByTable(baseTable);
            if (objectCode != null) {
                boolean drifted = drifts.stream().anyMatch(d -> d.startsWith(baseTable));
                ontology.syncTrust(objectCode, !drifted, "契约漂移：" + baseTable);
            }
        }
        if (drifts.isEmpty()) {
            return null;
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("driftedTables", drifts);
        return buildItem("CONTRACT_DRIFT", "CROSS", "CRITICAL",
                "契约漂移异常：" + drifts.size() + " 张关键表结构变更（"
                        + String.join("；", drifts.stream().limit(3).toList()) + "）",
                "关键列缺失会破坏AI查询与前端契约，请核查实体/表结构变更，修复DDL或同步更新 contract-baseline.json 基线",
                new ArrayList<>(baseline.keySet()), metrics);
    }

    /**
     * 加载契约基线（首次加载后缓存；加载失败则契约漂移检测自动停用，不影响其他检查项）
     *
     * @return 关键表（db.table）→ 关键列清单
     */
    private Map<String, List<String>> loadContractBaseline() {
        if (contractBaseline != null) {
            return contractBaseline;
        }
        try {
            contractBaseline = objectMapper.readValue(
                    new ClassPathResource("contract-baseline.json").getInputStream(),
                    new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("契约基线加载失败，契约漂移检测停用: {}", e.getMessage());
            contractBaseline = Map.of();
        }
        return contractBaseline;
    }

    /**
     * 构造哨兵建议条目（DQ_SENTINEL 类型，AUTO 级只告警）
     */
    private AiSuggestionEntity buildItem(String checkCode, String module, String severity,
                                         String title, String actionAdvice,
                                         List<String> sources, Map<String, Object> metrics) {
        AiSuggestionEntity item = new AiSuggestionEntity();
        item.setSuggestionType("DQ_SENTINEL");
        item.setModule(module);
        item.setSeverity(severity);
        item.setTitle(title);
        item.setAutomationLevel("AUTO");
        item.setEventId("DQ-" + checkCode + "-" + LocalDate.now());
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("checkCode", checkCode);
        analysis.put("actionAdvice", actionAdvice);
        analysis.put("evidence", Map.of("sources", sources, "metrics", metrics));
        try {
            item.setAnalysis(objectMapper.writeValueAsString(analysis));
        } catch (Exception e) {
            item.setAnalysis("{}");
        }
        return item;
    }

    private Long queryLong(String sql) {
        try {
            Long v = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
            return v == null ? 0L : v;
        } catch (Exception e) {
            log.debug("哨兵查询失败: {} - {}", sql, e.getMessage());
            return 0L;
        }
    }
}
