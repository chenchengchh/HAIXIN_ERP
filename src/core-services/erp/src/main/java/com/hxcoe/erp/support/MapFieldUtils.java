package com.hxcoe.erp.support;

import java.util.Map;

/**
 * Map 字段提取与类型转换工具
 * 消除 Controller 层中大量重复的 row.getOrDefault / Number 解析样板代码
 */
public final class MapFieldUtils {

    private MapFieldUtils() {
    }

    /**
     * 从 Map 中获取字符串字段，兼容驼峰与下划线两种命名
     * @param row 数据行
     * @param camelKey 驼峰键（如 warehouseCode）
     * @param snakeKey 下划线键（如 warehouse_code）
     * @return 字符串值，未找到返回 null
     */
    public static String getString(Map<String, Object> row, String camelKey, String snakeKey) {
        if (row == null) {
            return null;
        }
        Object v = row.get(camelKey);
        if (v == null && snakeKey != null) {
            v = row.get(snakeKey);
        }
        return v == null ? null : String.valueOf(v);
    }

    /**
     * 同 getString，但未找到时返回空串
     */
    public static String getStringOrEmpty(Map<String, Object> row, String camelKey, String snakeKey) {
        String v = getString(row, camelKey, snakeKey);
        return v == null ? "" : v;
    }

    /**
     * 同 getString，但未找到时返回指定默认值
     */
    public static String getStringOrDefault(Map<String, Object> row, String camelKey, String snakeKey, String defaultValue) {
        String v = getString(row, camelKey, snakeKey);
        return v == null ? defaultValue : v;
    }

    /**
     * 从 Map 中获取 Long 字段，自动兼容 Number 与字符串数字
     * @param row 数据行
     * @param key 字段名
     * @return Long 值，无法解析时返回 null
     */
    public static Long getLong(Map<String, Object> row, String key) {
        if (row == null || key == null) {
            return null;
        }
        Object v = row.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从 Map 中获取 Integer 字段
     */
    public static Integer getInteger(Map<String, Object> row, String key) {
        if (row == null || key == null) {
            return null;
        }
        Object v = row.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从 Map 中获取 Integer 字段，失败时返回默认值
     */
    public static Integer getIntegerOrDefault(Map<String, Object> row, String key, Integer defaultValue) {
        Integer v = getInteger(row, key);
        return v == null ? defaultValue : v;
    }

    /**
     * 解析业务状态字段，将多种表示（数字、字符串 ACTIVE/1/true）归一为 1/0
     * @param status 任意类型状态
     * @return 1=启用，0=停用
     */
    public static Integer toActiveStatus(Object status) {
        if (status == null) {
            return 1;
        }
        if (status instanceof Number n) {
            return n.intValue();
        }
        String s = String.valueOf(status);
        if (s.equalsIgnoreCase("ACTIVE") || s.equals("1") || s.equalsIgnoreCase("true")) {
            return 1;
        }
        return 0;
    }
}
