package com.hxcoe.wms.service;

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
public class WmsDataQualityService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> refresh() {
        Map<String, Object> res = new HashMap<>();
        int whBroken = refreshBrokenWarehouseMapping();
        int locBroken = refreshBrokenLocationMapping();
        int locMissingWarehouseCode = refreshLocationMissingWarehouseCode();
        int metrics = refreshMetrics();
        res.put("warehouseMappingBrokenUpserts", whBroken);
        res.put("locationMappingBrokenUpserts", locBroken);
        res.put("locationMissingWarehouseCodeUpserts", locMissingWarehouseCode);
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
                "SELECT COUNT(*) FROM wms_data_clean_task " + where,
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
                        + "FROM wms_data_clean_task "
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
                "UPDATE wms_data_clean_task SET status=:status, resolution=:resolution, updated_time=NOW() WHERE id=:id",
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
                        + "FROM wms_data_quality_metric "
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

    private int refreshBrokenWarehouseMapping() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT m.id, m.warehouse_id, m.warehouse_code, m.legacy_system, m.legacy_id, m.legacy_code "
                        + "FROM wms_warehouse_mapping m "
                        + "LEFT JOIN wms_warehouse w ON w.id = m.warehouse_id "
                        + "WHERE m.warehouse_id IS NOT NULL AND w.id IS NULL",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            upserts += upsertTask("WAREHOUSE", "MAPPING_WAREHOUSE_NOT_FOUND", mappingBusinessKey(row), row);
        }
        return upserts;
    }

    private int refreshBrokenLocationMapping() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT m.id, m.location_id, m.location_code, m.warehouse_code, m.legacy_system, m.legacy_id, m.legacy_code "
                        + "FROM wms_location_mapping m "
                        + "LEFT JOIN wms_location l ON l.id = m.location_id "
                        + "WHERE m.location_id IS NOT NULL AND l.id IS NULL",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            upserts += upsertTask("LOCATION", "MAPPING_LOCATION_NOT_FOUND", mappingBusinessKey(row), row);
        }
        return upserts;
    }

    private int refreshLocationMissingWarehouseCode() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, location_code, location_name, warehouse_code, zone_code "
                        + "FROM wms_location "
                        + "WHERE warehouse_code IS NULL OR TRIM(warehouse_code) = ''",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String locationCode = row.get("location_code") == null ? "" : String.valueOf(row.get("location_code"));
            if (locationCode.isBlank()) {
                continue;
            }
            upserts += upsertTask("LOCATION", "MISSING_WAREHOUSE_CODE", locationCode, row);
        }
        return upserts;
    }

    private int refreshMetrics() {
        LocalDate d = LocalDate.now();
        long warehouseTotal = nvlLong(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wms_warehouse", new MapSqlParameterSource(), Long.class));
        long locationTotal = nvlLong(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wms_location", new MapSqlParameterSource(), Long.class));
        long whBroken = nvlLong(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wms_warehouse_mapping m LEFT JOIN wms_warehouse w ON w.id = m.warehouse_id WHERE m.warehouse_id IS NOT NULL AND w.id IS NULL",
                new MapSqlParameterSource(),
                Long.class
        ));
        long locBroken = nvlLong(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wms_location_mapping m LEFT JOIN wms_location l ON l.id = m.location_id WHERE m.location_id IS NOT NULL AND l.id IS NULL",
                new MapSqlParameterSource(),
                Long.class
        ));
        long locMissingWh = nvlLong(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM wms_location WHERE warehouse_code IS NULL OR TRIM(warehouse_code) = ''",
                new MapSqlParameterSource(),
                Long.class
        ));

        int upserts = 0;
        upserts += upsertMetric(d, "warehouse.total", warehouseTotal, null);
        upserts += upsertMetric(d, "location.total", locationTotal, null);
        upserts += upsertMetric(d, "warehouse.mapping.broken", whBroken, null);
        upserts += upsertMetric(d, "location.mapping.broken", locBroken, null);
        upserts += upsertMetric(d, "location.missing_warehouse_code", locMissingWh, null);
        return upserts;
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
                "INSERT INTO wms_data_clean_task (entity_type, conflict_type, business_key, detail_json, status, created_time, updated_time) "
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
                "INSERT INTO wms_data_quality_metric (metric_date, metric_key, metric_value, detail_json, created_time) "
                        + "VALUES (:metricDate, :metricKey, :metricValue, CAST(:detailJson AS JSON), NOW()) "
                        + "ON DUPLICATE KEY UPDATE metric_value=VALUES(metric_value), detail_json=VALUES(detail_json)",
                params
        );
    }

    private long nvlLong(Long v) {
        return v == null ? 0L : v;
    }

    private long countTasks(String status) {
        if (status == null || status.isBlank()) {
            Long v = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wms_data_clean_task", new MapSqlParameterSource(), Long.class);
            return nvlLong(v);
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("status", status);
        Long v = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wms_data_clean_task WHERE status=:status", params, Long.class);
        return nvlLong(v);
    }

    private String mappingBusinessKey(Map<String, Object> row) {
        String legacySystem = row.get("legacy_system") == null ? "" : String.valueOf(row.get("legacy_system"));
        String legacyId = row.get("legacy_id") == null ? "" : String.valueOf(row.get("legacy_id"));
        String legacyCode = row.get("legacy_code") == null ? "" : String.valueOf(row.get("legacy_code"));
        String id = row.get("id") == null ? "" : String.valueOf(row.get("id"));

        String k = legacyId.isBlank() ? legacyCode : legacyId;
        if (k.isBlank()) {
            k = id;
        }
        return (legacySystem.isBlank() ? "UNKNOWN" : legacySystem) + ":" + k;
    }
}

