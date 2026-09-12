package com.hxcoe.aibrain.service.ontology;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 决策本体查询服务（P4-4，激活主设计 §7.3 预留）
 * AI 技能从"裸 SQL 跨库硬编码"迁移为"面向本体对象查询"：
 * - 对象元数据注册于 ai_ontology_object（物理表映射/语义字段映射/可信度标记）
 * - trustedOnly 默认开启：S12 哨兵标记 trusted=0 的对象自动跳过（数据可信度不足不对 AI 开放）
 * - 防注入：表名仅取自注册表，where 字段名必须在 key_fields/pk_field 白名单内，值全走命名参数
 */
@Slf4j
@Service
public class OntologyQueryService {

    /** 物理列名合法格式（防注入兜底） */
    private static final Pattern SAFE_COLUMN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /** 注册表缓存：object_code → 注册行（60s 定时刷新） */
    private final Map<String, Map<String, Object>> registry = new ConcurrentHashMap<>();

    /** 服务启动时加载注册表 */
    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    public void refreshRegistry() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT object_code, object_name, source_module, source_table, pk_field, key_fields, trusted, actions "
                            + "FROM ai_brain_db.ai_ontology_object", new MapSqlParameterSource());
            Map<String, Map<String, Object>> fresh = new HashMap<>();
            for (Map<String, Object> r : rows) {
                fresh.put(String.valueOf(r.get("object_code")), r);
            }
            registry.clear();
            registry.putAll(fresh);
            log.debug("本体注册表刷新: {} 个对象", fresh.size());
        } catch (Exception e) {
            log.warn("本体注册表刷新失败（沿用旧缓存）: {}", e.getMessage());
        }
    }

    /**
     * 面向本体对象的查询入口
     *
     * @param objectCode 对象编码（equipment/spare_part/work_order/purchase_order/collect_point/fault_record）
     * @return 查询构建器；对象未注册时返回的构建器查询结果为空并标记 unregistered
     */
    public OntologyQuery findObjects(String objectCode) {
        return new OntologyQuery(objectCode);
    }

    /** 查询对象当前可信度（S12 哨兵联动标记） */
    public boolean isTrusted(String objectCode) {
        Map<String, Object> meta = registry.get(objectCode);
        if (meta == null) {
            refreshRegistry();
            meta = registry.get(objectCode);
        }
        return meta != null && toTrustFlag(meta.get("trusted"));
    }

    /** TINYINT(1) 经 MySQL JDBC 默认映射为 Boolean，兼容 Boolean/Number 两种返回类型 */
    private boolean toTrustFlag(Object v) {
        if (v instanceof Boolean b) {
            return b;
        }
        return v instanceof Number n && n.intValue() == 1;
    }

    /** 登记/解除对象可信度（S12 哨兵调用） */
    public void setTrusted(String objectCode, boolean trusted, String reason) {
        jdbcTemplate.update("UPDATE ai_brain_db.ai_ontology_object SET trusted = :t WHERE object_code = :c",
                new MapSqlParameterSource().addValue("t", trusted ? 1 : 0).addValue("c", objectCode));
        refreshRegistry();
        log.warn("本体对象可信度变更: {} → trusted={}，原因: {}", objectCode, trusted, reason);
    }

    /** 全部已注册对象（看板/调试用） */
    public List<Map<String, Object>> listRegistered() {
        return new ArrayList<>(registry.values());
    }

    /**
     * 取对象物理表名（含库名前缀），供 JOIN/GROUP BY 等复杂聚合 SQL 使用——
     * 表名来自注册表而非技能代码硬编码，仍是面向本体编程；对象未注册返回 null，调用方应跳过
     */
    public String sourceTableOf(String objectCode) {
        Map<String, Object> meta = registry.get(objectCode);
        if (meta == null) {
            refreshRegistry();
            meta = registry.get(objectCode);
        }
        return meta == null ? null : String.valueOf(meta.get("source_table"));
    }

    /** 按物理表名反查对象编码（S12 契约漂移联动用）；无匹配返回 null */
    public String findObjectCodeByTable(String sourceTable) {
        for (Map.Entry<String, Map<String, Object>> e : registry.entrySet()) {
            if (sourceTable.equals(String.valueOf(e.getValue().get("source_table")))) {
                return e.getKey();
            }
        }
        return null;
    }

    /**
     * 状态感知可信度同步（S12 哨兵调用）：仅状态变化时写库，避免每轮扫描刷新缓存
     *
     * @param objectCode 对象编码
     * @param healthy    检查是否通过（通过→trusted=1，异常→trusted=0）
     * @param reason     变更原因（审计日志）
     */
    public void syncTrust(String objectCode, boolean healthy, String reason) {
        boolean current = isTrusted(objectCode);
        if (healthy && !current) {
            setTrusted(objectCode, true, "哨兵复核通过，解除标记：" + reason);
        } else if (!healthy && current) {
            setTrusted(objectCode, false, reason);
        }
    }

    /**
     * 本体查询构建器（链式：where 等值 / whereGt / whereLt / orderBy / limit / list / count / first）
     */
    public class OntologyQuery {
        private final String objectCode;
        private final Map<String, Object> meta;
        private final List<String> conditions = new ArrayList<>();
        private final MapSqlParameterSource params = new MapSqlParameterSource();
        private String orderByClause = "";
        private Integer limitRows = null;
        private boolean trustedOnly = true;
        private int paramSeq = 0;

        OntologyQuery(String objectCode) {
            this.objectCode = objectCode;
            Map<String, Object> m = registry.get(objectCode);
            if (m == null) {
                refreshRegistry();
                m = registry.get(objectCode);
            }
            this.meta = m;
        }

        /** 等值条件（语义字段名，自动映射物理列） */
        public OntologyQuery where(String semanticField, Object value) {
            return addCondition(semanticField, "=", value);
        }

        /** 大于条件 */
        public OntologyQuery whereGt(String semanticField, Object value) {
            return addCondition(semanticField, ">", value);
        }

        /** 大于等于条件 */
        public OntologyQuery whereGe(String semanticField, Object value) {
            return addCondition(semanticField, ">=", value);
        }

        /** 小于条件 */
        public OntologyQuery whereLt(String semanticField, Object value) {
            return addCondition(semanticField, "<", value);
        }

        /** 模糊匹配（%value%） */
        public OntologyQuery whereLike(String semanticField, String value) {
            return addCondition(semanticField, "LIKE", "%" + value + "%");
        }

        /** 集合包含 */
        public OntologyQuery whereIn(String semanticField, List<?> values) {
            return addCondition(semanticField, "IN", values);
        }

        /** 关闭可信度过滤（仅管理/对账场景使用） */
        public OntologyQuery includeUntrusted() {
            this.trustedOnly = false;
            return this;
        }

        /** 排序（物理列名需过白名单） */
        public OntologyQuery orderBy(String semanticField, boolean desc) {
            String col = resolveColumn(semanticField);
            if (col != null) {
                this.orderByClause = " ORDER BY " + col + (desc ? " DESC" : " ASC");
            }
            return this;
        }

        /** 限制返回行数 */
        public OntologyQuery limit(int rows) {
            this.limitRows = rows;
            return this;
        }

        /** 对象是否已注册 */
        public boolean registered() {
            return meta != null;
        }

        /** 对象是否可信（注册且 trusted=1） */
        public boolean trusted() {
            return meta != null && toTrustFlag(meta.get("trusted"));
        }

        /** 执行查询返回行列表；未注册或 trustedOnly 且不可信时返回空列表 */
        public List<Map<String, Object>> list() {
            if (blocked()) {
                return List.of();
            }
            String sql = "SELECT * FROM " + meta.get("source_table") + buildTail();
            return jdbcTemplate.queryForList(sql, params);
        }

        /** 计数 */
        public long count() {
            if (blocked()) {
                return 0L;
            }
            String sql = "SELECT COUNT(*) FROM " + meta.get("source_table") + buildTail();
            Long v = jdbcTemplate.queryForObject(sql, params, Long.class);
            return v == null ? 0L : v;
        }

        /** 首行（不存在返回 null） */
        public Map<String, Object> first() {
            List<Map<String, Object>> rows = limit(1).list();
            return rows.isEmpty() ? null : rows.get(0);
        }

        /** 查询是否被本体层拦截（未注册/不可信），供技能在建议中注明原因 */
        public boolean blocked() {
            if (meta == null) {
                log.warn("本体对象未注册，查询跳过: {}", objectCode);
                return true;
            }
            if (trustedOnly && !trusted()) {
                log.warn("本体对象可信度不足（S12标记），查询跳过: {}", objectCode);
                return true;
            }
            return false;
        }

        /** 拦截原因（null=未拦截） */
        public String blockReason() {
            if (meta == null) {
                return "对象未注册: " + objectCode;
            }
            if (trustedOnly && !trusted()) {
                return "数据可信度不足（S12哨兵标记 trusted=0）: " + objectCode;
            }
            return null;
        }

        private String buildTail() {
            StringBuilder sb = new StringBuilder();
            if (!conditions.isEmpty()) {
                sb.append(" WHERE ").append(String.join(" AND ", conditions));
            }
            sb.append(orderByClause);
            if (limitRows != null) {
                sb.append(" LIMIT ").append(limitRows);
            }
            return sb.toString();
        }

        private OntologyQuery addCondition(String semanticField, String op, Object value) {
            String col = resolveColumn(semanticField);
            if (col == null) {
                log.warn("本体查询字段不在白名单，条件忽略: {}.{}", objectCode, semanticField);
                return this;
            }
            String key = "p" + (paramSeq++);
            if ("IN".equals(op)) {
                conditions.add(col + " IN (:" + key + ")");
            } else {
                conditions.add(col + " " + op + " :" + key);
            }
            params.addValue(key, value);
            return this;
        }

        /** 语义字段 → 物理列解析（key_fields 映射 + pk_field + 列名格式兜底） */
        private String resolveColumn(String semanticField) {
            if (meta == null) {
                return null;
            }
            if (semanticField.equals(meta.get("pk_field"))) {
                return semanticField;
            }
            Object kf = meta.get("key_fields");
            if (kf != null) {
                try {
                    Map<?, ?> map = kf instanceof Map ? (Map<?, ?>) kf
                            : objectMapper.readValue(String.valueOf(kf), Map.class);
                    Object col = map.get(semanticField);
                    if (col != null && SAFE_COLUMN.matcher(String.valueOf(col)).matches()) {
                        return String.valueOf(col);
                    }
                } catch (Exception e) {
                    log.warn("key_fields 解析失败: {}", objectCode);
                }
            }
            // 语义名本身即合法列名时放行（兼容未注册映射的字段）
            return SAFE_COLUMN.matcher(semanticField).matches() ? semanticField : null;
        }
    }
}
