package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.WmsLocationClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/scm/master-data", "/scm/master-data"})
public class MasterDataController {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private WmsLocationClient wmsLocationClient;

    @GetMapping("/locations")
    public ApiResponse<PageResult<Map<String, Object>>> getLocationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String zoneCode,
            @RequestParam(required = false) String locationTypeCode,
            @RequestParam(required = false) String status
    ) {
        int p = Math.max(page == null ? 1 : page, 1);
        int s = size == null ? 10 : Math.min(Math.max(size, 1), 200);
        try {
            Result<Object> res = wmsLocationClient.page(p, s, keyword, warehouseCode, zoneCode, locationTypeCode, status);
            if (res != null && ResponseStatusAdapter.isSuccess(res.getCode())) {
                PageParts parts = extractPage(res.getData());
                PageResult<Map<String, Object>> pageResult = PageResult.build(parts.total, parts.size, parts.page, parts.rows);
                return success("库位列表查询成功", pageResult);
            }
        } catch (Exception ignored) {
        }
        int offset = (p - 1) * s;

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (l.location_code LIKE :kw OR l.location_name LIKE :kw) ");
            params.addValue("kw", "%" + keyword.trim() + "%");
        }
        if (warehouseCode != null && !warehouseCode.isBlank()) {
            where.append(" AND l.warehouse_code = :warehouseCode ");
            params.addValue("warehouseCode", warehouseCode.trim());
        }
        if (zoneCode != null && !zoneCode.isBlank()) {
            where.append(" AND l.zone_code = :zoneCode ");
            params.addValue("zoneCode", zoneCode.trim());
        }
        if (locationTypeCode != null && !locationTypeCode.isBlank()) {
            where.append(" AND l.location_type_code = :locationTypeCode ");
            params.addValue("locationTypeCode", locationTypeCode.trim());
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND l.status = :status ");
            params.addValue("status", status.trim());
        }

        Long total = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM scm_location l" + where, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", s);
        params.addValue("offset", offset);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT l.id, l.location_code, l.location_name, l.warehouse_code, w.warehouse_name, l.zone_code, l.location_type_code, l.status, l.remark, l.created_time, l.updated_time "
                        + "FROM scm_location l "
                        + "LEFT JOIN scm_warehouse w ON w.warehouse_code = l.warehouse_code "
                        + where
                        + "ORDER BY l.updated_time DESC "
                        + "LIMIT :limit OFFSET :offset",
                params
        );

        List<Map<String, Object>> mapped = new java.util.ArrayList<>();
        for (Map<String, Object> r : rows) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", r.get("id"));
            row.put("locationCode", r.get("location_code"));
            row.put("locationName", r.get("location_name"));
            row.put("warehouseId", r.get("warehouse_code"));
            row.put("warehouseCode", r.get("warehouse_code"));
            row.put("warehouseName", r.get("warehouse_name") == null ? String.valueOf(r.getOrDefault("warehouse_code", "")) : r.get("warehouse_name"));
            row.put("zoneCode", r.get("zone_code"));
            row.put("zoneName", r.get("zone_code") == null ? "" : String.valueOf(r.get("zone_code")));
            row.put("locationTypeCode", r.get("location_type_code"));
            row.put("status", r.get("status"));
            row.put("remark", r.get("remark"));
            row.put("createdAt", r.get("created_time") == null ? "" : String.valueOf(r.get("created_time")));
            row.put("updatedAt", r.get("updated_time") == null ? "" : String.valueOf(r.get("updated_time")));
            mapped.add(row);
        }
        return success("库位列表查询成功", PageResult.build(total, s, p, mapped));
    }

    @GetMapping("/locations/{id}")
    public ApiResponse<Map<String, Object>> getLocationDetail(@PathVariable Long id) {
        if (id == null) {
            return badRequest("id不能为空");
        }
        try {
            Result<Object> res = wmsLocationClient.detail(id);
            if (res != null && ResponseStatusAdapter.isSuccess(res.getCode())) {
                Map<String, Object> row = asMap(res.getData());
                if (!row.isEmpty()) {
                    return success("库位查询成功", row);
                }
            }
        } catch (Exception ignored) {
        }
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT l.id, l.location_code, l.location_name, l.warehouse_code, w.warehouse_name, l.zone_code, l.location_type_code, l.status, l.remark, l.created_time, l.updated_time "
                        + "FROM scm_location l "
                        + "LEFT JOIN scm_warehouse w ON w.warehouse_code = l.warehouse_code "
                        + "WHERE l.id = :id LIMIT 1",
                params
        );
        if (rows.isEmpty()) {
            return notFound("库位不存在");
        }
        Map<String, Object> r = rows.get(0);
        Map<String, Object> row = new HashMap<>();
        row.put("id", r.get("id"));
        row.put("locationCode", r.get("location_code"));
        row.put("locationName", r.get("location_name"));
        row.put("warehouseId", r.get("warehouse_code"));
        row.put("warehouseCode", r.get("warehouse_code"));
        row.put("warehouseName", r.get("warehouse_name") == null ? String.valueOf(r.getOrDefault("warehouse_code", "")) : r.get("warehouse_name"));
        row.put("zoneCode", r.get("zone_code"));
        row.put("zoneName", r.get("zone_code") == null ? "" : String.valueOf(r.get("zone_code")));
        row.put("locationTypeCode", r.get("location_type_code"));
        row.put("status", r.get("status"));
        row.put("remark", r.get("remark"));
        row.put("createdAt", r.get("created_time") == null ? "" : String.valueOf(r.get("created_time")));
        row.put("updatedAt", r.get("updated_time") == null ? "" : String.valueOf(r.get("updated_time")));
        return success("库位查询成功", row);
    }

    private static Map<String, Object> asMap(Object o) {
        if (o instanceof Map<?, ?> m) {
            Map<String, Object> out = new java.util.LinkedHashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                out.put(String.valueOf(e.getKey()), e.getValue());
            }
            return out;
        }
        return new java.util.LinkedHashMap<>();
    }

    private static List<Map<String, Object>> asListOfMap(Object o) {
        if (o instanceof List<?> list) {
            List<Map<String, Object>> out = new java.util.ArrayList<>();
            for (Object it : list) {
                out.add(asMap(it));
            }
            return out;
        }
        return new java.util.ArrayList<>();
    }

    private static PageParts extractPage(Object data) {
        Map<String, Object> map = asMap(data);
        Object content = map.get("content");
        Object records = map.get("records");
        Object list = map.get("list");
        Object total = map.get("totalElements");
        if (total == null) total = map.get("total");
        Object page = map.get("page");
        if (page == null) page = map.get("currentPage");
        Object size = map.get("pageSize");
        if (size == null) size = map.get("size");

        List<Map<String, Object>> rows = asListOfMap(content);
        if (rows.isEmpty()) rows = asListOfMap(records);
        if (rows.isEmpty()) rows = asListOfMap(list);

        long totalNum = toLong(total, 0);
        int pageNum = (int) toLong(page, 1);
        int sizeNum = (int) toLong(size, 10);
        return new PageParts(rows, totalNum, pageNum < 1 ? 1 : pageNum, sizeNum < 1 ? 10 : sizeNum);
    }

    private static long toLong(Object v, long def) {
        if (v == null) return def;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return def; }
    }

    private static final class PageParts {
        private final List<Map<String, Object>> rows;
        private final long total;
        private final int page;
        private final int size;

        private PageParts(List<Map<String, Object>> rows, long total, int page, int size) {
            this.rows = rows;
            this.total = total;
            this.page = page;
            this.size = size;
        }
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.error(400, message);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
