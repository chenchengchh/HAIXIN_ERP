package com.hxcoe.eam.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.PageResult;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EamDataQualityService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> refresh() {
        Map<String, Object> res = new HashMap<>();
        int missingCode = refreshAssetMissingCode();
        int missingName = refreshAssetMissingName();
        int duplicateCode = refreshAssetDuplicateCode();
        // AI决策系统P0：跨表对账检查项（数据质量哨兵扩展）
        int spareStockMismatch = refreshSpareStockMismatch();
        int issueStatusInconsistent = refreshIssueStatusInconsistent();
        int kpiMonthGap = refreshKpiMonthGap();
        int orphanReference = refreshAssetOrphanReference();
        int metrics = refreshMetrics();
        res.put("assetMissingCodeUpserts", missingCode);
        res.put("assetMissingNameUpserts", missingName);
        res.put("assetDuplicateCodeUpserts", duplicateCode);
        res.put("spareStockMismatchUpserts", spareStockMismatch);
        res.put("issueStatusInconsistentUpserts", issueStatusInconsistent);
        res.put("kpiMonthGapUpserts", kpiMonthGap);
        res.put("assetOrphanReferenceUpserts", orphanReference);
        res.put("metricUpserts", metrics);
        res.put("openTaskCount", countTasks("OPEN"));
        return res;
    }

    public PageResult<Map<String, Object>> listTasks(Integer page, Integer size, String status) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 10 : size;
        int offset = (p - 1) * s;

        String where = "";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (status != null && !status.isBlank()) {
            where = " WHERE status=:status ";
            params.addValue("status", status.trim());
        }

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM eam_data_clean_task " + where,
                params,
                Long.class
        );
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", s);
        params.addValue("offset", offset);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, entity_type, conflict_type, business_key, detail_json, status, resolution, created_time, updated_time "
                        + "FROM eam_data_clean_task "
                        + where
                        + "ORDER BY updated_time DESC "
                        + "LIMIT :limit OFFSET :offset",
                params
        );
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.get("id"));
            m.put("entityType", r.get("entity_type"));
            m.put("conflictType", r.get("conflict_type"));
            m.put("businessKey", r.get("business_key"));
            m.put("detail", r.get("detail_json"));
            m.put("status", r.get("status"));
            m.put("resolution", r.get("resolution"));
            m.put("createdTime", r.get("created_time"));
            m.put("updatedTime", r.get("updated_time"));
            mapped.add(m);
        }
        return PageResult.build(total, s, p, mapped);
    }

    @Transactional
    public boolean resolveTask(Long id, String resolution, String status) {
        if (id == null) {
            return false;
        }
        String st = status == null || status.isBlank() ? "RESOLVED" : status.trim().toUpperCase();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("status", st);
        params.addValue("resolution", resolution == null ? null : resolution.trim());
        int updated = jdbcTemplate.update(
                "UPDATE eam_data_clean_task SET status=:status, resolution=:resolution, updated_time=NOW() WHERE id=:id",
                params
        );
        return updated > 0;
    }

    public List<Map<String, Object>> listMetrics(LocalDate metricDate) {
        LocalDate d = metricDate == null ? LocalDate.now() : metricDate;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("metricDate", d);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT metric_date, metric_key, metric_value, detail_json, created_time "
                        + "FROM eam_data_quality_metric "
                        + "WHERE metric_date=:metricDate "
                        + "ORDER BY metric_key ASC",
                params
        );
        List<Map<String, Object>> mapped = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> m = new HashMap<>();
            m.put("metricDate", r.get("metric_date"));
            m.put("metricKey", r.get("metric_key"));
            m.put("metricValue", r.get("metric_value"));
            m.put("detail", r.get("detail_json"));
            m.put("createdTime", r.get("created_time"));
            mapped.add(m);
        }
        return mapped;
    }

    private int refreshAssetMissingCode() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, code, name, status, location "
                        + "FROM eam_asset "
                        + "WHERE code IS NULL OR TRIM(code) = ''",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String id = row.get("id") == null ? "" : String.valueOf(row.get("id"));
            if (id.isBlank()) {
                continue;
            }
            upserts += upsertTask("ASSET", "MISSING_ASSET_CODE", id, row);
        }
        return upserts;
    }

    private int refreshAssetMissingName() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, code, name, status, location "
                        + "FROM eam_asset "
                        + "WHERE name IS NULL OR TRIM(name) = ''",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String code = row.get("code") == null ? "" : String.valueOf(row.get("code"));
            String id = row.get("id") == null ? "" : String.valueOf(row.get("id"));
            String key = code.isBlank() ? id : code;
            if (key.isBlank()) {
                continue;
            }
            upserts += upsertTask("ASSET", "MISSING_ASSET_NAME", key, row);
        }
        return upserts;
    }

    private int refreshAssetDuplicateCode() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT code, COUNT(*) AS cnt "
                        + "FROM eam_asset "
                        + "WHERE code IS NOT NULL AND TRIM(code) <> '' "
                        + "GROUP BY code "
                        + "HAVING COUNT(*) > 1",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String code = row.get("code") == null ? "" : String.valueOf(row.get("code"));
            if (code.isBlank()) {
                continue;
            }
            upserts += upsertTask("ASSET", "DUPLICATE_ASSET_CODE", code, row);
        }
        return upserts;
    }

    /**
     * 检查项：备件总库存与库存明细合计不一致（同一业务概念两份事实，AI决策前必须检出）
     * spare_part.current_stock vs SUM(spare_inventory.quantity) 按 spare_id 分组比对
     *
     * @return 新增/更新任务数
     */
    private int refreshSpareStockMismatch() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT p.id AS spare_id, p.code, p.name, p.current_stock, "
                        + "COALESCE(SUM(i.quantity), 0) AS inventory_total "
                        + "FROM eam_spare_part p "
                        + "LEFT JOIN eam_spare_inventory i ON i.spare_id = p.id "
                        + "GROUP BY p.id, p.code, p.name, p.current_stock "
                        + "HAVING COALESCE(p.current_stock, 0) <> COALESCE(SUM(i.quantity), 0)",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String key = String.valueOf(row.get("spare_id"));
            upserts += upsertTask("SPARE_PART", "SPARE_STOCK_MISMATCH", key, row);
        }
        return upserts;
    }

    /**
     * 检查项：领用单状态机异常
     * 规则：status为空；非approved状态却有returned归还标记；approved且returned属正常闭环
     *
     * @return 新增/更新任务数
     */
    private int refreshIssueStatusInconsistent() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, spare_id, spare_name, status, return_status, quantity "
                        + "FROM eam_spare_issue "
                        + "WHERE status IS NULL OR TRIM(status) = '' "
                        + "   OR (status <> 'approved' AND return_status = 'returned')",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            upserts += upsertTask("SPARE_ISSUE", "ISSUE_STATUS_INCONSISTENT", String.valueOf(row.get("id")), row);
        }
        return upserts;
    }

    /**
     * 检查项：KPI 记录月份断档（当前月往前12个月内缺失的月份）
     * KPI 是绩效分析与AI预测的特征源，断档会导致看板为空与预测偏差
     *
     * @return 新增/更新任务数
     */
    private int refreshKpiMonthGap() {
        // 生成最近12个月序列，左连 KPI 表找出无记录的月份
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT m.month_key FROM ("
                        + buildMonthSeriesSql(12)
                        + ") m "
                        + "LEFT JOIN (SELECT DISTINCT record_month FROM eam_asset_kpi) k ON k.record_month = m.month_key "
                        + "WHERE k.record_month IS NULL",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String month = String.valueOf(row.get("month_key"));
            upserts += upsertTask("ASSET_KPI", "KPI_MONTH_GAP", month, row);
        }
        return upserts;
    }

    /**
     * 检查项：业务记录引用不存在的设备（工单/故障/维修记录的 equipment_id 孤儿引用）
     * 孤儿引用会导致AI根因追溯链断裂
     *
     * @return 新增/更新任务数
     */
    private int refreshAssetOrphanReference() {
        int upserts = 0;
        // 三类业务表统一检测：equipment_id 非空但在 eam_asset 中不存在
        String[][] tables = {
                {"eam_workorder", "WORK_ORDER"},
                {"eam_fault_record", "FAULT_RECORD"},
                {"eam_maintenance_record", "MAINTENANCE_RECORD"}
        };
        for (String[] t : tables) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT r.id, r.equipment_id, r.equipment_name FROM " + t[0] + " r "
                            + "LEFT JOIN eam_asset a ON a.id = r.equipment_id "
                            + "WHERE r.equipment_id IS NOT NULL AND a.id IS NULL",
                    new MapSqlParameterSource()
            );
            for (Map<String, Object> row : rows) {
                upserts += upsertTask(t[1], "ASSET_ORPHAN_REFERENCE", String.valueOf(row.get("id")), row);
            }
        }
        return upserts;
    }

    /**
     * 构建最近N个月的月份序列SQL（无需递归CTE，兼容当前MySQL版本）
     *
     * @param months 月份数
     * @return UNION ALL 子查询
     */
    private String buildMonthSeriesSql(int months) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < months; i++) {
            if (i > 0) {
                sb.append(" UNION ALL ");
            }
            sb.append("SELECT DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL ").append(i).append(" MONTH), '%Y-%m') AS month_key");
        }
        return sb.toString();
    }

    private int refreshMetrics() {
        LocalDate d = LocalDate.now();
        long total = nvlLong(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM eam_asset", new MapSqlParameterSource(), Long.class));
        long missingCode = nvlLong(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM eam_asset WHERE code IS NULL OR TRIM(code) = ''", new MapSqlParameterSource(), Long.class));
        long missingName = nvlLong(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM eam_asset WHERE name IS NULL OR TRIM(name) = ''", new MapSqlParameterSource(), Long.class));
        long duplicateCode = nvlLong(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ("
                        + "SELECT code FROM eam_asset WHERE code IS NOT NULL AND TRIM(code) <> '' GROUP BY code HAVING COUNT(*) > 1"
                        + ") t",
                new MapSqlParameterSource(),
                Long.class
        ));

        int upserts = 0;
        upserts += upsertMetric(d, "asset.total", total, null);
        upserts += upsertMetric(d, "asset.missing_asset_code", missingCode, null);
        upserts += upsertMetric(d, "asset.missing_asset_name", missingName, null);
        upserts += upsertMetric(d, "asset.duplicate_asset_code", duplicateCode, null);
        // AI决策系统P0：跨表对账类指标（按任务类型统计OPEN任务数）
        upserts += upsertMetric(d, "spare.stock_mismatch", countOpenTasksByType("SPARE_STOCK_MISMATCH"), null);
        upserts += upsertMetric(d, "issue.status_inconsistent", countOpenTasksByType("ISSUE_STATUS_INCONSISTENT"), null);
        upserts += upsertMetric(d, "kpi.month_gap", countOpenTasksByType("KPI_MONTH_GAP"), null);
        upserts += upsertMetric(d, "asset.orphan_reference", countOpenTasksByType("ASSET_ORPHAN_REFERENCE"), null);
        return upserts;
    }

    /**
     * 按冲突类型统计OPEN状态任务数
     *
     * @param conflictType 冲突类型
     * @return OPEN任务数
     */
    private long countOpenTasksByType(String conflictType) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("conflictType", conflictType);
        Long v = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM eam_data_clean_task WHERE conflict_type=:conflictType AND status='OPEN'",
                params,
                Long.class
        );
        return nvlLong(v);
    }

    private int upsertTask(String entityType, String conflictType, String businessKey, Map<String, Object> detail) {
        String json;
        try {
            json = objectMapper.writeValueAsString(detail == null ? Map.of() : detail);
        } catch (Exception e) {
            json = "{}";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("entityType", entityType);
        params.addValue("conflictType", conflictType);
        params.addValue("businessKey", businessKey);
        params.addValue("detailJson", json);
        return jdbcTemplate.update(
                "INSERT INTO eam_data_clean_task (entity_type, conflict_type, business_key, detail_json, status, created_time, updated_time) "
                        + "VALUES (:entityType, :conflictType, :businessKey, CAST(:detailJson AS JSON), 'OPEN', NOW(), NOW()) "
                        + "ON DUPLICATE KEY UPDATE detail_json=VALUES(detail_json), updated_time=NOW()",
                params
        );
    }

    private int upsertMetric(LocalDate date, String key, long value, Map<String, Object> detail) {
        String json;
        try {
            json = objectMapper.writeValueAsString(detail == null ? Map.of() : detail);
        } catch (Exception e) {
            json = "{}";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("metricDate", date);
        params.addValue("metricKey", key);
        params.addValue("metricValue", value);
        params.addValue("detailJson", json);
        return jdbcTemplate.update(
                "INSERT INTO eam_data_quality_metric (metric_date, metric_key, metric_value, detail_json, created_time) "
                        + "VALUES (:metricDate, :metricKey, :metricValue, CAST(:detailJson AS JSON), NOW()) "
                        + "ON DUPLICATE KEY UPDATE metric_value=VALUES(metric_value), detail_json=VALUES(detail_json)",
                params
        );
    }

    private long countTasks(String status) {
        if (status == null || status.isBlank()) {
            Long v = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM eam_data_clean_task", new MapSqlParameterSource(), Long.class);
            return nvlLong(v);
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("status", status);
        Long v = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM eam_data_clean_task WHERE status=:status", params, Long.class);
        return nvlLong(v);
    }

    private long nvlLong(Long v) {
        return v == null ? 0L : v;
    }
}

