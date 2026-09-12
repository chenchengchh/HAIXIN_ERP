package com.hxcoe.erp.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResultDataExtractor {
    private ResultDataExtractor() {
    }

    public static Map<String, Object> asMap(Object o) {
        if (o instanceof Map<?, ?> m) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                out.put(String.valueOf(e.getKey()), e.getValue());
            }
            return out;
        }
        return new LinkedHashMap<>();
    }

    public static List<Map<String, Object>> asListOfMap(Object o) {
        if (o instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object item : list) {
                out.add(asMap(item));
            }
            return out;
        }
        return new ArrayList<>();
    }

    public static PageParts extractPage(Object data) {
        Map<String, Object> map = asMap(data);
        Object content = map.get("content");
        Object records = map.get("records");
        Object list = map.get("list");
        Object number = map.get("number");
        Object size = map.get("size");
        Object totalElements = map.get("totalElements");
        Object total = map.get("total");
        Object currentPage = map.get("currentPage");
        Object page = map.get("page");
        Object pageSize = map.get("pageSize");

        List<Map<String, Object>> rows = asListOfMap(content);
        if (rows.isEmpty()) rows = asListOfMap(records);
        if (rows.isEmpty()) rows = asListOfMap(list);

        long totalNum = toLong(totalElements, -1);
        if (totalNum < 0) totalNum = toLong(total, 0);

        int pageNum = (int) toLong(page, -1);
        if (pageNum < 0) pageNum = (int) toLong(currentPage, -1);
        if (pageNum < 0) {
            long n = toLong(number, 0);
            pageNum = (int) n + 1;
        }
        if (pageNum < 1) pageNum = 1;

        int sizeNum = (int) toLong(pageSize, -1);
        if (sizeNum < 0) sizeNum = (int) toLong(size, 10);
        if (sizeNum < 1) sizeNum = 10;

        return new PageParts(rows, totalNum, pageNum, sizeNum);
    }

    private static long toLong(Object v, long def) {
        if (v == null) return def;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return def;
        }
    }

    public record PageParts(List<Map<String, Object>> rows, long total, int page, int size) {
    }
}

