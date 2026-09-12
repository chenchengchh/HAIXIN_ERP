package com.hxcoe.srm.service;

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
public class SrmDataQualityService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> refresh() {
        Map<String, Object> res = new HashMap<>();

        int brokenMapping = refreshBrokenSupplierMapping();
        int mismatchCode = refreshSupplierCodeMismatch();
        int scmMirrorMismatch = refreshSupplierMirrorMismatchAgainstScm();
        int erpMirrorMismatch = refreshSupplierMirrorMismatchAgainstErp();
        int metrics = refreshMetrics();

        res.put("brokenSupplierMappingUpserts", brokenMapping);
        res.put("supplierCodeMismatchUpserts", mismatchCode);
        res.put("supplierMirrorScmMismatchUpserts", scmMirrorMismatch);
        res.put("supplierMirrorErpMismatchUpserts", erpMirrorMismatch);
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
                "SELECT COUNT(*) FROM srm_data_clean_task " + where,
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
                        + "FROM srm_data_clean_task "
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
                "UPDATE srm_data_clean_task SET status=:status, resolution=:resolution, updated_time=NOW() WHERE id=:id",
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
                        + "FROM srm_data_quality_metric "
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

    private int refreshBrokenSupplierMapping() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT m.id, m.supplier_id, m.supplier_code, m.legacy_system, m.legacy_id, m.legacy_code "
                        + "FROM srm_supplier_mapping m "
                        + "LEFT JOIN srm_supplier s ON s.id = m.supplier_id "
                        + "WHERE s.id IS NULL",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            upserts += upsertTask("SUPPLIER", "MAPPING_SUPPLIER_NOT_FOUND", mappingBusinessKey(row), row);
        }
        return upserts;
    }

    private int refreshSupplierCodeMismatch() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT m.id, m.supplier_id, m.supplier_code AS mapping_supplier_code, s.supplier_code AS supplier_code, m.legacy_system, m.legacy_id, m.legacy_code "
                        + "FROM srm_supplier_mapping m "
                        + "JOIN srm_supplier s ON s.id = m.supplier_id "
                        + "WHERE m.supplier_code <> s.supplier_code",
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            upserts += upsertTask("SUPPLIER", "MAPPING_SUPPLIER_CODE_MISMATCH", mappingBusinessKey(row), row);
        }
        return upserts;
    }

    private int refreshMetrics() {
        LocalDate d = LocalDate.now();
        long supplierTotal = nvlLong(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM srm_supplier", new MapSqlParameterSource(), Long.class));
        long mappingTotal = nvlLong(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM srm_supplier_mapping", new MapSqlParameterSource(), Long.class));
        long broken = nvlLong(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM srm_supplier_mapping m LEFT JOIN srm_supplier s ON s.id = m.supplier_id WHERE s.id IS NULL",
                new MapSqlParameterSource(),
                Long.class
        ));
        long mismatch = nvlLong(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM srm_supplier_mapping m JOIN srm_supplier s ON s.id = m.supplier_id WHERE m.supplier_code <> s.supplier_code",
                new MapSqlParameterSource(),
                Long.class
        ));
        long scmMirrorMismatch = nvlLong(jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM srm_supplier s
                        LEFT JOIN scm_db.scm_supplier c ON c.supplier_code = s.supplier_code
                        WHERE c.supplier_code IS NULL
                           OR UPPER(COALESCE(c.supplier_name, '')) <> UPPER(COALESCE(s.supplier_name, ''))
                           OR UPPER(COALESCE(c.status, '')) <> UPPER(COALESCE(s.status, ''))
                           OR UPPER(COALESCE(c.contact_person, '')) <> UPPER(COALESCE(s.contact_person, ''))
                           OR UPPER(COALESCE(c.contact_phone, '')) <> UPPER(COALESCE(s.contact_phone, ''))
                           OR UPPER(COALESCE(c.contact_email, '')) <> UPPER(COALESCE(s.email, ''))
                           OR UPPER(COALESCE(c.address, '')) <> UPPER(COALESCE(s.address, ''))
                        """,
                new MapSqlParameterSource(),
                Long.class
        ));
        long erpMirrorMismatch = nvlLong(jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM srm_supplier s
                        LEFT JOIN erp_db.erp_supplier e ON e.supplier_code = s.supplier_code AND COALESCE(e.is_deleted, 0) = 0
                        WHERE e.supplier_code IS NULL
                           OR UPPER(COALESCE(e.supplier_name, '')) <> UPPER(COALESCE(s.supplier_name, ''))
                           OR (CASE WHEN COALESCE(e.status, 1) = 1 THEN 'ACTIVE' ELSE 'INACTIVE' END) <> UPPER(COALESCE(s.status, 'ACTIVE'))
                           OR UPPER(COALESCE(e.contact_person, '')) <> UPPER(COALESCE(s.contact_person, ''))
                           OR UPPER(COALESCE(e.contact_phone, '')) <> UPPER(COALESCE(s.contact_phone, ''))
                           OR UPPER(COALESCE(e.contact_email, '')) <> UPPER(COALESCE(s.email, ''))
                           OR UPPER(COALESCE(e.address, '')) <> UPPER(COALESCE(s.address, ''))
                        """,
                new MapSqlParameterSource(),
                Long.class
        ));

        int upserts = 0;
        upserts += upsertMetric(d, "supplier.total", supplierTotal, null);
        upserts += upsertMetric(d, "supplier.mapping.total", mappingTotal, null);
        upserts += upsertMetric(d, "supplier.mapping.broken", broken, null);
        upserts += upsertMetric(d, "supplier.mapping.code_mismatch", mismatch, null);
        upserts += upsertMetric(d, "supplier.mirror.scm.mismatch", scmMirrorMismatch, null);
        upserts += upsertMetric(d, "supplier.mirror.erp.mismatch", erpMirrorMismatch, null);
        return upserts;
    }

    private int refreshSupplierMirrorMismatchAgainstScm() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                """
                        SELECT
                          s.supplier_code,
                          s.supplier_name AS srm_supplier_name,
                          s.status AS srm_status,
                          s.contact_person AS srm_contact_person,
                          s.contact_phone AS srm_contact_phone,
                          s.email AS srm_contact_email,
                          s.address AS srm_address,
                          c.supplier_name AS scm_supplier_name,
                          c.status AS scm_status,
                          c.contact_person AS scm_contact_person,
                          c.contact_phone AS scm_contact_phone,
                          c.contact_email AS scm_contact_email,
                          c.address AS scm_address,
                          CONCAT_WS(';',
                            IF(c.supplier_code IS NULL, 'MISSING_IN_SCM', NULL),
                            IF(c.supplier_code IS NOT NULL AND UPPER(COALESCE(c.supplier_name, '')) <> UPPER(COALESCE(s.supplier_name, '')), 'NAME_MISMATCH', NULL),
                            IF(c.supplier_code IS NOT NULL AND UPPER(COALESCE(c.status, '')) <> UPPER(COALESCE(s.status, '')), 'STATUS_MISMATCH', NULL),
                            IF(c.supplier_code IS NOT NULL AND UPPER(COALESCE(c.contact_person, '')) <> UPPER(COALESCE(s.contact_person, '')), 'CONTACT_PERSON_MISMATCH', NULL),
                            IF(c.supplier_code IS NOT NULL AND UPPER(COALESCE(c.contact_phone, '')) <> UPPER(COALESCE(s.contact_phone, '')), 'CONTACT_PHONE_MISMATCH', NULL),
                            IF(c.supplier_code IS NOT NULL AND UPPER(COALESCE(c.contact_email, '')) <> UPPER(COALESCE(s.email, '')), 'CONTACT_EMAIL_MISMATCH', NULL),
                            IF(c.supplier_code IS NOT NULL AND UPPER(COALESCE(c.address, '')) <> UPPER(COALESCE(s.address, '')), 'ADDRESS_MISMATCH', NULL)
                          ) AS mismatch_reasons
                        FROM srm_supplier s
                        LEFT JOIN scm_db.scm_supplier c ON c.supplier_code = s.supplier_code
                        WHERE c.supplier_code IS NULL
                           OR UPPER(COALESCE(c.supplier_name, '')) <> UPPER(COALESCE(s.supplier_name, ''))
                           OR UPPER(COALESCE(c.status, '')) <> UPPER(COALESCE(s.status, ''))
                           OR UPPER(COALESCE(c.contact_person, '')) <> UPPER(COALESCE(s.contact_person, ''))
                           OR UPPER(COALESCE(c.contact_phone, '')) <> UPPER(COALESCE(s.contact_phone, ''))
                           OR UPPER(COALESCE(c.contact_email, '')) <> UPPER(COALESCE(s.email, ''))
                           OR UPPER(COALESCE(c.address, '')) <> UPPER(COALESCE(s.address, ''))
                        """,
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String code = row.get("supplier_code") == null ? "" : String.valueOf(row.get("supplier_code"));
            if (code.isBlank()) {
                continue;
            }
            upserts += upsertTask("SUPPLIER", "MIRROR_SCM_MISMATCH", "SCM:" + code, row);
        }
        autoResolveSupplierMirrorMismatchAgainstScm();
        return upserts;
    }

    private void autoResolveSupplierMirrorMismatchAgainstScm() {
        jdbcTemplate.update(
                """
                        UPDATE srm_data_clean_task t
                        SET t.status='RESOLVED', t.updated_time=NOW()
                        WHERE t.entity_type='SUPPLIER'
                          AND t.conflict_type='MIRROR_SCM_MISMATCH'
                          AND t.status='OPEN'
                          AND NOT EXISTS (
                            SELECT 1
                            FROM srm_supplier s
                            LEFT JOIN scm_db.scm_supplier c ON c.supplier_code = s.supplier_code
                            WHERE s.supplier_code = SUBSTRING_INDEX(t.business_key, ':', -1)
                              AND (
                                c.supplier_code IS NULL
                                OR UPPER(COALESCE(c.supplier_name, '')) <> UPPER(COALESCE(s.supplier_name, ''))
                                OR UPPER(COALESCE(c.status, '')) <> UPPER(COALESCE(s.status, ''))
                                OR UPPER(COALESCE(c.contact_person, '')) <> UPPER(COALESCE(s.contact_person, ''))
                                OR UPPER(COALESCE(c.contact_phone, '')) <> UPPER(COALESCE(s.contact_phone, ''))
                                OR UPPER(COALESCE(c.contact_email, '')) <> UPPER(COALESCE(s.email, ''))
                                OR UPPER(COALESCE(c.address, '')) <> UPPER(COALESCE(s.address, ''))
                              )
                          )
                        """,
                new MapSqlParameterSource()
        );
    }

    private int refreshSupplierMirrorMismatchAgainstErp() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                """
                        SELECT
                          s.supplier_code,
                          s.supplier_name AS srm_supplier_name,
                          s.status AS srm_status,
                          s.contact_person AS srm_contact_person,
                          s.contact_phone AS srm_contact_phone,
                          s.email AS srm_contact_email,
                          s.address AS srm_address,
                          e.supplier_name AS erp_supplier_name,
                          e.status AS erp_status,
                          e.contact_person AS erp_contact_person,
                          e.contact_phone AS erp_contact_phone,
                          e.contact_email AS erp_contact_email,
                          e.address AS erp_address,
                          COALESCE(e.is_deleted, 0) AS erp_is_deleted,
                          CONCAT_WS(';',
                            IF(e.supplier_code IS NULL, 'MISSING_IN_ERP', NULL),
                            IF(e.supplier_code IS NOT NULL AND UPPER(COALESCE(e.supplier_name, '')) <> UPPER(COALESCE(s.supplier_name, '')), 'NAME_MISMATCH', NULL),
                            IF(e.supplier_code IS NOT NULL AND (CASE WHEN COALESCE(e.status, 1) = 1 THEN 'ACTIVE' ELSE 'INACTIVE' END) <> UPPER(COALESCE(s.status, 'ACTIVE')), 'STATUS_MISMATCH', NULL),
                            IF(e.supplier_code IS NOT NULL AND UPPER(COALESCE(e.contact_person, '')) <> UPPER(COALESCE(s.contact_person, '')), 'CONTACT_PERSON_MISMATCH', NULL),
                            IF(e.supplier_code IS NOT NULL AND UPPER(COALESCE(e.contact_phone, '')) <> UPPER(COALESCE(s.contact_phone, '')), 'CONTACT_PHONE_MISMATCH', NULL),
                            IF(e.supplier_code IS NOT NULL AND UPPER(COALESCE(e.contact_email, '')) <> UPPER(COALESCE(s.email, '')), 'CONTACT_EMAIL_MISMATCH', NULL),
                            IF(e.supplier_code IS NOT NULL AND UPPER(COALESCE(e.address, '')) <> UPPER(COALESCE(s.address, '')), 'ADDRESS_MISMATCH', NULL)
                          ) AS mismatch_reasons
                        FROM srm_supplier s
                        LEFT JOIN erp_db.erp_supplier e ON e.supplier_code = s.supplier_code AND COALESCE(e.is_deleted, 0) = 0
                        WHERE e.supplier_code IS NULL
                           OR UPPER(COALESCE(e.supplier_name, '')) <> UPPER(COALESCE(s.supplier_name, ''))
                           OR (CASE WHEN COALESCE(e.status, 1) = 1 THEN 'ACTIVE' ELSE 'INACTIVE' END) <> UPPER(COALESCE(s.status, 'ACTIVE'))
                           OR UPPER(COALESCE(e.contact_person, '')) <> UPPER(COALESCE(s.contact_person, ''))
                           OR UPPER(COALESCE(e.contact_phone, '')) <> UPPER(COALESCE(s.contact_phone, ''))
                           OR UPPER(COALESCE(e.contact_email, '')) <> UPPER(COALESCE(s.email, ''))
                           OR UPPER(COALESCE(e.address, '')) <> UPPER(COALESCE(s.address, ''))
                        """,
                new MapSqlParameterSource()
        );
        int upserts = 0;
        for (Map<String, Object> row : rows) {
            String code = row.get("supplier_code") == null ? "" : String.valueOf(row.get("supplier_code"));
            if (code.isBlank()) {
                continue;
            }
            upserts += upsertTask("SUPPLIER", "MIRROR_ERP_MISMATCH", "ERP:" + code, row);
        }
        autoResolveSupplierMirrorMismatchAgainstErp();
        return upserts;
    }

    private void autoResolveSupplierMirrorMismatchAgainstErp() {
        jdbcTemplate.update(
                """
                        UPDATE srm_data_clean_task t
                        SET t.status='RESOLVED', t.updated_time=NOW()
                        WHERE t.entity_type='SUPPLIER'
                          AND t.conflict_type='MIRROR_ERP_MISMATCH'
                          AND t.status='OPEN'
                          AND NOT EXISTS (
                            SELECT 1
                            FROM srm_supplier s
                            LEFT JOIN erp_db.erp_supplier e ON e.supplier_code = s.supplier_code AND COALESCE(e.is_deleted, 0) = 0
                            WHERE s.supplier_code = SUBSTRING_INDEX(t.business_key, ':', -1)
                              AND (
                                e.supplier_code IS NULL
                                OR UPPER(COALESCE(e.supplier_name, '')) <> UPPER(COALESCE(s.supplier_name, ''))
                                OR (CASE WHEN COALESCE(e.status, 1) = 1 THEN 'ACTIVE' ELSE 'INACTIVE' END) <> UPPER(COALESCE(s.status, 'ACTIVE'))
                                OR UPPER(COALESCE(e.contact_person, '')) <> UPPER(COALESCE(s.contact_person, ''))
                                OR UPPER(COALESCE(e.contact_phone, '')) <> UPPER(COALESCE(s.contact_phone, ''))
                                OR UPPER(COALESCE(e.contact_email, '')) <> UPPER(COALESCE(s.email, ''))
                                OR UPPER(COALESCE(e.address, '')) <> UPPER(COALESCE(s.address, ''))
                              )
                          )
                        """,
                new MapSqlParameterSource()
        );
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
                "INSERT INTO srm_data_clean_task (entity_type, conflict_type, business_key, detail_json, status, created_time, updated_time) "
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
                "INSERT INTO srm_data_quality_metric (metric_date, metric_key, metric_value, detail_json, created_time) "
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
            Long v = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM srm_data_clean_task", new MapSqlParameterSource(), Long.class);
            return nvlLong(v);
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("status", status);
        Long v = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM srm_data_clean_task WHERE status=:status", params, Long.class);
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
