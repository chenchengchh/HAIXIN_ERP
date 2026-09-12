package com.hxcoe.crm.service;

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
public class CrmDataQualityService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> refresh() {
        Map<String, Object> res = new HashMap<>();
        int missingNo = refreshCustomerMissingNo();
        int missingName = refreshCustomerMissingName();
        int duplicateNo = refreshCustomerDuplicateNo();
        int metrics = refreshMetrics();
        res.put("customerMissingNoUpserts", missingNo);
        res.put("customerMissingNameUpserts", missingName);
        res.put("customerDuplicateNoUpserts", duplicateNo);
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
                "SELECT COUNT(*) FROM crm_data_clean_task " + where,
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
                        + "FROM crm_data_clean_task "
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
                "UPDATE crm_data_clean_task SET status=:status, resolution=:resolution, updated_time=NOW() WHERE id=:id",
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
                        + "FROM crm_data_quality_metric "
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

    private int refreshCustomerMissingNo() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, customer_no, customer_name, customer_type, status "
                        + "FROM crm_customer "
                        + "WHERE customer_no IS NULL OR TRIM(customer_no) = ''",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String id = row.get("id") == null ? "" : String.valueOf(row.get("id"));
            if (id.isBlank()) {
                continue;
            }
            upserts += upsertTask("CUSTOMER", "MISSING_CUSTOMER_NO", id, row);
        }
        return upserts;
    }

    private int refreshCustomerMissingName() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, customer_no, customer_name, customer_type, status "
                        + "FROM crm_customer "
                        + "WHERE customer_name IS NULL OR TRIM(customer_name) = ''",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String customerNo = row.get("customer_no") == null ? "" : String.valueOf(row.get("customer_no"));
            String id = row.get("id") == null ? "" : String.valueOf(row.get("id"));
            String key = customerNo.isBlank() ? id : customerNo;
            if (key.isBlank()) {
                continue;
            }
            upserts += upsertTask("CUSTOMER", "MISSING_CUSTOMER_NAME", key, row);
        }
        return upserts;
    }

    private int refreshCustomerDuplicateNo() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT customer_no, COUNT(*) AS cnt "
                        + "FROM crm_customer "
                        + "WHERE customer_no IS NOT NULL AND TRIM(customer_no) <> '' "
                        + "GROUP BY customer_no "
                        + "HAVING COUNT(*) > 1",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String customerNo = row.get("customer_no") == null ? "" : String.valueOf(row.get("customer_no"));
            if (customerNo.isBlank()) {
                continue;
            }
            upserts += upsertTask("CUSTOMER", "DUPLICATE_CUSTOMER_NO", customerNo, row);
        }
        return upserts;
    }

    private int refreshMetrics() {
        LocalDate d = LocalDate.now();
        long total = nvlLong(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM crm_customer", new MapSqlParameterSource(), Long.class));
        long missingNo = nvlLong(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM crm_customer WHERE customer_no IS NULL OR TRIM(customer_no) = ''", new MapSqlParameterSource(), Long.class));
        long missingName = nvlLong(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM crm_customer WHERE customer_name IS NULL OR TRIM(customer_name) = ''", new MapSqlParameterSource(), Long.class));
        long duplicateNo = nvlLong(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ("
                        + "SELECT customer_no FROM crm_customer WHERE customer_no IS NOT NULL AND TRIM(customer_no) <> '' GROUP BY customer_no HAVING COUNT(*) > 1"
                        + ") t",
                new MapSqlParameterSource(),
                Long.class
        ));

        int upserts = 0;
        upserts += upsertMetric(d, "customer.total", total, null);
        upserts += upsertMetric(d, "customer.missing_customer_no", missingNo, null);
        upserts += upsertMetric(d, "customer.missing_customer_name", missingName, null);
        upserts += upsertMetric(d, "customer.duplicate_customer_no", duplicateNo, null);
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
                "INSERT INTO crm_data_clean_task (entity_type, conflict_type, business_key, detail_json, status, created_time, updated_time) "
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
                "INSERT INTO crm_data_quality_metric (metric_date, metric_key, metric_value, detail_json, created_time) "
                        + "VALUES (:metricDate, :metricKey, :metricValue, CAST(:detailJson AS JSON), NOW()) "
                        + "ON DUPLICATE KEY UPDATE metric_value=VALUES(metric_value), detail_json=VALUES(detail_json)",
                params
        );
    }

    private long countTasks(String status) {
        if (status == null || status.isBlank()) {
            Long v = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM crm_data_clean_task", new MapSqlParameterSource(), Long.class);
            return nvlLong(v);
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("status", status);
        Long v = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM crm_data_clean_task WHERE status=:status", params, Long.class);
        return nvlLong(v);
    }

    private long nvlLong(Long v) {
        return v == null ? 0L : v;
    }
}

