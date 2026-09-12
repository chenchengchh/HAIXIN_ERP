package com.hxcoe.erp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.dto.integration.WmsOutboundShippedRequest;
import com.hxcoe.erp.dto.supplychain.PurchaseOrderItemDto;
import com.hxcoe.erp.dto.supplychain.TransferOrderDto;
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
public class ErpTransferOrderService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public TransferOrderDto createFromRequest(Map<String, Object> body, Map<String, Object> outboundRow, Map<String, Object> asnRow) {
        String transferNo = firstNonBlank(body, "sourceNo", "source_no", "transferNo", "transfer_no", "orderNo", "order_no");
        if (isBlank(transferNo)) {
            transferNo = firstNonBlank(outboundRow, "sourceNo", "source_no", "orderNo", "order_no", "outboundNo", "outbound_no");
        }
        if (isBlank(transferNo)) {
            transferNo = "TR-" + System.currentTimeMillis();
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("transferNo", transferNo);
        params.addValue("sourceNo", transferNo);
        params.addValue("fromWarehouseCode", firstNonBlank(body, "from_warehouse_id", "fromWarehouseCode", "from_warehouse_code"));
        params.addValue("toWarehouseCode", firstNonBlank(body, "to_warehouse_id", "toWarehouseCode", "to_warehouse_code"));
        params.addValue("status", "CREATED");
        params.addValue("outboundOrderId", toLong(valueOf(outboundRow, "id")));
        params.addValue("outboundOrderNo", firstNonBlank(outboundRow, "orderNo", "order_no", "outboundNo", "outbound_no"));
        params.addValue("outboundStatus", firstNonBlank(outboundRow, "status"));
        params.addValue("asnId", toLong(valueOf(asnRow, "id")));
        params.addValue("asnNo", firstNonBlank(asnRow, "asnNo", "asn_no"));
        params.addValue("inboundStatus", firstNonBlank(asnRow, "status"));
        params.addValue("itemsJson", toJson(asListOfMap(body == null ? null : body.getOrDefault("transfer_items", body.getOrDefault("items", List.of())))));
        params.addValue("remark", body == null ? null : body.get("remark"));
        jdbcTemplate.update(
                "INSERT INTO erp_transfer_order "
                        + "(transfer_no, source_no, from_warehouse_code, to_warehouse_code, status, outbound_order_id, outbound_order_no, outbound_status, asn_id, asn_no, inbound_status, items_json, remark, created_time, updated_time) "
                        + "VALUES (:transferNo, :sourceNo, :fromWarehouseCode, :toWarehouseCode, :status, :outboundOrderId, :outboundOrderNo, :outboundStatus, :asnId, :asnNo, :inboundStatus, CAST(:itemsJson AS JSON), :remark, NOW(), NOW()) "
                        + "ON DUPLICATE KEY UPDATE source_no=VALUES(source_no), from_warehouse_code=VALUES(from_warehouse_code), to_warehouse_code=VALUES(to_warehouse_code), "
                        + "status=VALUES(status), outbound_order_id=VALUES(outbound_order_id), outbound_order_no=VALUES(outbound_order_no), outbound_status=VALUES(outbound_status), "
                        + "asn_id=VALUES(asn_id), asn_no=VALUES(asn_no), inbound_status=VALUES(inbound_status), items_json=VALUES(items_json), remark=VALUES(remark), updated_time=NOW()",
                params
        );
        return findByTransferNo(transferNo);
    }

    @Transactional
    public void applyOutboundShipped(WmsOutboundShippedRequest req) {
        if (req == null || req.getOutboundOrder() == null) {
            return;
        }
        String orderType = trim(req.getOutboundOrder().getOrderType());
        if (!"TRANSFER".equalsIgnoreCase(orderType)) {
            return;
        }
        String sourceNo = trim(req.getOutboundOrder().getSourceNo());
        if (sourceNo == null) {
            return;
        }
        String outboundStatus = trim(req.getOutboundOrder().getStatus());
        String status = "OUTBOUND_SHIPPED";
        if (outboundStatus != null && !"SHIPPED".equalsIgnoreCase(outboundStatus)) {
            status = "CREATED";
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("sourceNo", sourceNo);
        params.addValue("status", status);
        params.addValue("outboundOrderId", req.getOutboundOrder().getOutboundOrderId());
        params.addValue("outboundOrderNo", req.getOutboundOrder().getOrderNo());
        params.addValue("outboundStatus", outboundStatus);
        jdbcTemplate.update(
                "UPDATE erp_transfer_order SET status=:status, outbound_order_id=:outboundOrderId, outbound_order_no=:outboundOrderNo, outbound_status=:outboundStatus, updated_time=NOW() "
                        + "WHERE transfer_no=:sourceNo OR source_no=:sourceNo",
                params
        );
    }

    @Transactional
    public TransferOrderDto applyInboundReceived(Long id, Map<String, Object> asnRow) {
        if (id == null) {
            return null;
        }
        String inboundStatus = firstNonBlank(asnRow, "status");
        if (isBlank(inboundStatus)) {
            inboundStatus = "CREATED";
        }
        String nextStatus = switch (inboundStatus.toUpperCase()) {
            case "RECEIVED" -> "COMPLETED";
            case "PARTIAL_RECEIVED" -> "PARTIAL_RECEIVED";
            case "RECEIVING" -> "INBOUND_RECEIVING";
            default -> "OUTBOUND_SHIPPED";
        };
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("status", nextStatus);
        params.addValue("asnId", toLong(valueOf(asnRow, "id")));
        params.addValue("asnNo", firstNonBlank(asnRow, "asnNo", "asn_no"));
        params.addValue("inboundStatus", inboundStatus);
        jdbcTemplate.update(
                "UPDATE erp_transfer_order SET status=:status, "
                        + "outbound_status=CASE "
                        + "WHEN UPPER(:inboundStatus)='RECEIVED' AND (outbound_status IS NULL OR outbound_status='' OR UPPER(outbound_status)='CREATED') THEN 'SHIPPED' "
                        + "ELSE outbound_status END, "
                        + "asn_id=COALESCE(:asnId, asn_id), asn_no=COALESCE(:asnNo, asn_no), inbound_status=:inboundStatus, updated_time=NOW() "
                        + "WHERE id=:id",
                params
        );
        return getById(id);
    }

    public PageResult<TransferOrderDto> list(int page, int size) {
        int normalizedPage = Math.max(page, 1);
        int normalizedSize = Math.max(size, 1);
        int offset = (normalizedPage - 1) * normalizedSize;
        long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM erp_transfer_order", new MapSqlParameterSource(), Long.class);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("offset", offset);
        params.addValue("size", normalizedSize);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, transfer_no, source_no, from_warehouse_code, to_warehouse_code, status, outbound_order_id, outbound_order_no, outbound_status, asn_id, asn_no, inbound_status, items_json, remark, created_time, updated_time "
                        + "FROM erp_transfer_order ORDER BY id DESC LIMIT :offset, :size",
                params
        );
        List<TransferOrderDto> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            list.add(mapRow(row));
        }
        return PageResult.build(total, normalizedSize, normalizedPage, list);
    }

    public TransferOrderDto getById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, transfer_no, source_no, from_warehouse_code, to_warehouse_code, status, outbound_order_id, outbound_order_no, outbound_status, asn_id, asn_no, inbound_status, items_json, remark, created_time, updated_time "
                        + "FROM erp_transfer_order WHERE id=:id",
                params
        );
        return rows.isEmpty() ? null : mapRow(rows.get(0));
    }

    public TransferOrderDto findByTransferNo(String transferNo) {
        if (isBlank(transferNo)) {
            return null;
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("transferNo", transferNo);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, transfer_no, source_no, from_warehouse_code, to_warehouse_code, status, outbound_order_id, outbound_order_no, outbound_status, asn_id, asn_no, inbound_status, items_json, remark, created_time, updated_time "
                        + "FROM erp_transfer_order WHERE transfer_no=:transferNo OR source_no=:transferNo ORDER BY id DESC",
                params
        );
        return rows.isEmpty() ? null : mapRow(rows.get(0));
    }

    private TransferOrderDto mapRow(Map<String, Object> row) {
        TransferOrderDto dto = new TransferOrderDto();
        dto.setId(toLong(row.get("id")));
        dto.setTransferNo(str(row, "transfer_no"));
        dto.setSourceNo(str(row, "source_no"));
        dto.setFromWarehouseCode(str(row, "from_warehouse_code"));
        dto.setToWarehouseCode(str(row, "to_warehouse_code"));
        dto.setStatus(str(row, "status"));
        dto.setOutboundOrderId(toLong(row.get("outbound_order_id")));
        dto.setOutboundOrderNo(str(row, "outbound_order_no"));
        dto.setOutboundStatus(str(row, "outbound_status"));
        dto.setAsnId(toLong(row.get("asn_id")));
        dto.setAsnNo(str(row, "asn_no"));
        dto.setInboundStatus(str(row, "inbound_status"));
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
