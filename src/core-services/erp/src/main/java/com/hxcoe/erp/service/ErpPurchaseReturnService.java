package com.hxcoe.erp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.dto.integration.WmsOutboundShippedRequest;
import com.hxcoe.erp.dto.supplychain.PurchaseOrderItemDto;
import com.hxcoe.erp.dto.supplychain.PurchaseReturnDto;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ErpPurchaseReturnService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public PurchaseReturnDto createFromRequest(Map<String, Object> body, Map<String, Object> outboundRow) {
        String returnNo = firstNonBlank(body, "sourceNo", "source_no", "returnNo", "return_no", "orderNo", "order_no");
        if (isBlank(returnNo)) {
            returnNo = firstNonBlank(outboundRow, "sourceNo", "source_no", "orderNo", "order_no", "outboundNo", "outbound_no");
        }
        if (isBlank(returnNo)) {
            returnNo = "PR-" + System.currentTimeMillis();
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("returnNo", returnNo);
        params.addValue("sourceNo", returnNo);
        params.addValue("supplierCode", firstNonBlank(body, "supplier_code", "supplierCode"));
        params.addValue("supplierName", firstNonBlank(body, "supplier_name", "supplierName"));
        params.addValue("warehouseCode", firstNonBlank(body, "warehouse_code", "warehouseCode"));
        params.addValue("status", "CREATED");
        params.addValue("outboundOrderId", toLong(valueOf(outboundRow, "id")));
        params.addValue("outboundOrderNo", firstNonBlank(outboundRow, "orderNo", "order_no", "outboundNo", "outbound_no"));
        params.addValue("outboundStatus", firstNonBlank(outboundRow, "status"));
        params.addValue("itemsJson", toJson(asListOfMap(body == null ? null : body.getOrDefault("return_items", body.getOrDefault("items", List.of())))));
        params.addValue("remark", body == null ? null : body.get("remark"));
        jdbcTemplate.update(
                "INSERT INTO erp_purchase_return_order "
                        + "(return_no, source_no, supplier_code, supplier_name, warehouse_code, status, outbound_order_id, outbound_order_no, outbound_status, items_json, remark, created_time, updated_time) "
                        + "VALUES (:returnNo, :sourceNo, :supplierCode, :supplierName, :warehouseCode, :status, :outboundOrderId, :outboundOrderNo, :outboundStatus, CAST(:itemsJson AS JSON), :remark, NOW(), NOW()) "
                        + "ON DUPLICATE KEY UPDATE source_no=VALUES(source_no), supplier_code=VALUES(supplier_code), supplier_name=VALUES(supplier_name), warehouse_code=VALUES(warehouse_code), "
                        + "status=VALUES(status), outbound_order_id=VALUES(outbound_order_id), outbound_order_no=VALUES(outbound_order_no), outbound_status=VALUES(outbound_status), items_json=VALUES(items_json), remark=VALUES(remark), updated_time=NOW()",
                params
        );
        return findByReturnNo(returnNo);
    }

    @Transactional
    public void applyOutboundShipped(WmsOutboundShippedRequest req) {
        if (req == null || req.getOutboundOrder() == null) {
            return;
        }
        String orderType = trim(req.getOutboundOrder().getOrderType());
        if (!"PURCHASE_RETURN".equalsIgnoreCase(orderType)) {
            return;
        }
        String sourceNo = trim(req.getOutboundOrder().getSourceNo());
        if (sourceNo == null) {
            return;
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("sourceNo", sourceNo);
        params.addValue("status", "SHIPPED");
        params.addValue("outboundOrderId", req.getOutboundOrder().getOutboundOrderId());
        params.addValue("outboundOrderNo", req.getOutboundOrder().getOrderNo());
        params.addValue("outboundStatus", req.getOutboundOrder().getStatus());
        jdbcTemplate.update(
                "UPDATE erp_purchase_return_order SET status=:status, outbound_order_id=:outboundOrderId, outbound_order_no=:outboundOrderNo, outbound_status=:outboundStatus, updated_time=NOW() "
                        + "WHERE return_no=:sourceNo OR source_no=:sourceNo",
                params
        );
    }

    public PageResult<PurchaseReturnDto> list(int page, int size) {
        int normalizedPage = Math.max(page, 1);
        int normalizedSize = Math.max(size, 1);
        int offset = (normalizedPage - 1) * normalizedSize;
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM erp_purchase_return_order", new MapSqlParameterSource(), Long.class);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("offset", offset);
        params.addValue("size", normalizedSize);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, return_no, source_no, supplier_code, supplier_name, warehouse_code, status, outbound_order_id, outbound_order_no, outbound_status, items_json, remark, created_time, updated_time "
                        + "FROM erp_purchase_return_order ORDER BY id DESC LIMIT :offset, :size",
                params
        );
        List<PurchaseReturnDto> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            list.add(mapRow(row));
        }
        return PageResult.build(total, normalizedSize, normalizedPage, list);
    }

    public PurchaseReturnDto getById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, return_no, source_no, supplier_code, supplier_name, warehouse_code, status, outbound_order_id, outbound_order_no, outbound_status, items_json, remark, created_time, updated_time "
                        + "FROM erp_purchase_return_order WHERE id=:id",
                params
        );
        return rows.isEmpty() ? null : mapRow(rows.get(0));
    }

    public PurchaseReturnDto findByReturnNo(String returnNo) {
        if (isBlank(returnNo)) {
            return null;
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("returnNo", returnNo);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, return_no, source_no, supplier_code, supplier_name, warehouse_code, status, outbound_order_id, outbound_order_no, outbound_status, items_json, remark, created_time, updated_time "
                        + "FROM erp_purchase_return_order WHERE return_no=:returnNo OR source_no=:returnNo ORDER BY id DESC",
                params
        );
        return rows.isEmpty() ? null : mapRow(rows.get(0));
    }

    private PurchaseReturnDto mapRow(Map<String, Object> row) {
        PurchaseReturnDto dto = new PurchaseReturnDto();
        dto.setId(toLong(row.get("id")));
        dto.setReturnNo(str(row, "return_no"));
        dto.setSourceNo(str(row, "source_no"));
        dto.setSupplierCode(str(row, "supplier_code"));
        dto.setSupplierName(str(row, "supplier_name"));
        dto.setWarehouseCode(str(row, "warehouse_code"));
        dto.setStatus(str(row, "status"));
        dto.setOutboundOrderId(toLong(row.get("outbound_order_id")));
        dto.setOutboundOrderNo(str(row, "outbound_order_no"));
        dto.setOutboundStatus(str(row, "outbound_status"));
        dto.setRemark(str(row, "remark"));
        dto.setCreateTime(str(row, "created_time"));
        dto.setUpdateTime(str(row, "updated_time"));
        dto.setItems(parseItems(row.get("items_json")));
        return dto;
    }

    private List<PurchaseOrderItemDto> parseItems(Object itemsJson) {
        List<Map<String, Object>> rows = asListOfMap(itemsJson);
        List<PurchaseOrderItemDto> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            PurchaseOrderItemDto item = new PurchaseOrderItemDto();
            item.setMaterialCode(firstNonBlank(row, "material_code", "materialCode"));
            item.setMaterialName(firstNonBlank(row, "material_name", "materialName"));
            item.setSpecification(firstNonBlank(row, "specification"));
            item.setUnit(firstNonBlank(row, "unit"));
            item.setQuantity(toDecimal(valueOf(row, "quantity", "planQuantity", "plan_quantity")));
            item.setUnitPrice(toDecimal(valueOf(row, "unit_price", "unitPrice")));
            item.setAmount(toDecimal(valueOf(row, "amount", "totalPrice", "total_price")));
            item.setRemark(firstNonBlank(row, "remark"));
            items.add(item);
        }
        return items;
    }

    private List<Map<String, Object>> asListOfMap(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    Map<String, Object> row = new HashMap<>();
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        row.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                    out.add(row);
                }
            }
            return out;
        }
        try {
            return objectMapper.readValue(String.valueOf(value), new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception ignore) {
            return Collections.emptyList();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Collections.emptyList() : value);
        } catch (Exception ignore) {
            return "[]";
        }
    }

    private static Object valueOf(Map<String, Object> row, String... keys) {
        if (row == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (row.containsKey(key)) {
                return row.get(key);
            }
        }
        return null;
    }

    private static String str(Map<String, Object> row, String key) {
        Object value = row == null ? null : row.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static String firstNonBlank(Map<String, Object> row, String... keys) {
        if (row == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = row.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isEmpty()) {
                return text;
            }
        }
        return null;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignore) {
            return null;
        }
    }

    private static java.math.BigDecimal toDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.math.BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return new java.math.BigDecimal(String.valueOf(number));
        }
        try {
            return new java.math.BigDecimal(String.valueOf(value));
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String trim(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean isBlank(String value) {
        return trim(value) == null;
    }
}
