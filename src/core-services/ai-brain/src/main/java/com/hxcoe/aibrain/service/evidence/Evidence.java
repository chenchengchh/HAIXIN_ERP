package com.hxcoe.aibrain.service.evidence;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结构化证据契约构建器（P4-5 证据链升级）
 * 将建议 evidence.sources 从"表名文本"升级为"可点击下钻的行级引用"，向后兼容：
 * 列表元素允许 String（旧格式）与 Map（新格式）混排，前端按元素类型分别渲染。
 * 契约结构：
 * {
 *   "sources": [
 *     {"type":"table_rows","db":"scada_db","table":"scada_tag_value",
 *      "filter":"tag_code='LEVEL_001' AND ts>=近9天","rowCount":432,
 *      "drillUrl":"/home/scada/historical-data?tagCode=LEVEL_001"}
 *   ],
 *   "metrics": {...},
 *   "steps": ["推理步骤1", "推理步骤2"],
 *   "queryTime": "2026-08-03 10:00:00"
 * }
 */
public final class Evidence {

    private Evidence() {
    }

    /**
     * 构建一条行级证据源（可下钻）
     *
     * @param db 数据库名
     * @param table 表名
     * @param filter 数据过滤条件描述（人类可读）
     * @param rowCount 命中行数（未知传 null）
     * @param drillUrl 前端下钻路由（/home/... 带查询参数）
     * @return 结构化证据源
     */
    public static Map<String, Object> source(String db, String table, String filter,
                                             Integer rowCount, String drillUrl) {
        Map<String, Object> src = new HashMap<>();
        src.put("type", "table_rows");
        src.put("db", db);
        src.put("table", table);
        if (filter != null) {
            src.put("filter", filter);
        }
        if (rowCount != null) {
            src.put("rowCount", rowCount);
        }
        if (drillUrl != null) {
            src.put("drillUrl", drillUrl);
        }
        return src;
    }

    /**
     * 组装完整证据对象（含查询时间戳）
     *
     * @param sources 证据源列表（String 旧格式或 source() 新格式混排）
     * @param metrics 关键指标
     * @return 证据对象
     */
    public static Map<String, Object> of(List<?> sources, Map<String, Object> metrics) {
        return of(sources, metrics, null);
    }

    /**
     * 组装完整证据对象（含推理步骤与查询时间戳）
     *
     * @param sources 证据源列表
     * @param metrics 关键指标
     * @param steps 推理步骤（可空）
     * @return 证据对象
     */
    public static Map<String, Object> of(List<?> sources, Map<String, Object> metrics, List<String> steps) {
        Map<String, Object> evidence = new HashMap<>();
        evidence.put("sources", new ArrayList<>(sources));
        if (metrics != null) {
            evidence.put("metrics", metrics);
        }
        if (steps != null && !steps.isEmpty()) {
            evidence.put("steps", steps);
        }
        evidence.put("queryTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return evidence;
    }
}
