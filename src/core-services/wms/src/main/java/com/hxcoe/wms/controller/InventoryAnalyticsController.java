package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.entity.InventoryTransactionEntity;
import com.hxcoe.wms.repository.InventoryRepository;
import com.hxcoe.wms.repository.InventoryTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping({"/api/v1/wms/inventory/analytics", "/wms/inventory/analytics", "/api/wms/inventory/analytics"})
public class InventoryAnalyticsController {

    private static final BigDecimal LOW_STOCK_THRESHOLD = new BigDecimal("10");
    private static final BigDecimal OVER_STOCK_THRESHOLD = new BigDecimal("1000");

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        List<InventoryEntity> inventories = inventoryRepository.findAll();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        Set<String> materialCodes = new HashSet<>();
        int alertCount = 0;
        for (InventoryEntity inv : inventories) {
            BigDecimal qty = inv.getQuantity() == null ? BigDecimal.ZERO : inv.getQuantity();
            totalQuantity = totalQuantity.add(qty);
            if (inv.getMaterialCode() != null && !inv.getMaterialCode().isBlank()) {
                materialCodes.add(inv.getMaterialCode().trim());
            }
            if (qty.compareTo(LOW_STOCK_THRESHOLD) < 0 || qty.compareTo(OVER_STOCK_THRESHOLD) > 0) {
                alertCount++;
            }
        }

        double turnover = computeTurnover(totalQuantity);
        Map<String, Object> data = new HashMap<>();
        data.put("totalQuantity", totalQuantity);
        data.put("materialCount", materialCodes.size());
        data.put("turnover", turnover);
        data.put("alertCount", alertCount);
        return success("库存汇总查询成功", data);
    }

    @GetMapping("/trend")
    public ApiResponse<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "30") int days) {
        int d = days <= 0 ? 30 : Math.min(days, 365);
        LocalDate startDate = LocalDate.now().minusDays(d - 1L);
        LocalDateTime cutoff = startDate.atStartOfDay();

        List<InventoryEntity> inventories = inventoryRepository.findAll();
        BigDecimal currentTotal = BigDecimal.ZERO;
        for (InventoryEntity inv : inventories) {
            if (inv.getQuantity() != null) {
                currentTotal = currentTotal.add(inv.getQuantity());
            }
        }

        List<InventoryTransactionEntity> txns = inventoryTransactionRepository.findByTransactionTimeAfter(cutoff);
        Map<LocalDate, BigDecimal> netByDay = new HashMap<>();
        for (InventoryTransactionEntity t : txns) {
            if (t.getTransactionTime() == null || t.getQuantity() == null) {
                continue;
            }
            LocalDate day = t.getTransactionTime().toLocalDate();
            BigDecimal delta = toDelta(t.getType(), t.getQuantity());
            netByDay.put(day, netByDay.getOrDefault(day, BigDecimal.ZERO).add(delta));
        }

        BigDecimal netSum = BigDecimal.ZERO;
        for (Map.Entry<LocalDate, BigDecimal> e : netByDay.entrySet()) {
            if (!e.getKey().isBefore(startDate) && !e.getKey().isAfter(LocalDate.now())) {
                netSum = netSum.add(e.getValue());
            }
        }

        BigDecimal total = currentTotal.subtract(netSum);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<Map<String, Object>> series = new ArrayList<>();
        for (int i = 0; i < d; i++) {
            LocalDate day = startDate.plusDays(i);
            total = total.add(netByDay.getOrDefault(day, BigDecimal.ZERO));
            Map<String, Object> point = new HashMap<>();
            point.put("date", formatter.format(day));
            point.put("quantity", total);
            series.add(point);
        }
        return success("库存趋势查询成功", series);
    }

    @GetMapping("/distribution")
    public ApiResponse<List<Map<String, Object>>> distribution(@RequestParam(defaultValue = "warehouse") String type) {
        List<InventoryEntity> inventories = inventoryRepository.findAll();
        Map<String, BigDecimal> sumBy = new LinkedHashMap<>();
        for (InventoryEntity inv : inventories) {
            BigDecimal qty = inv.getQuantity() == null ? BigDecimal.ZERO : inv.getQuantity();
            String key;
            if ("location".equalsIgnoreCase(type)) {
                key = toLocationGroup(inv.getLocationCode());
            } else if ("category".equalsIgnoreCase(type)) {
                key = toCategory(inv.getMaterialCode());
            } else {
                key = inv.getWarehouseCode();
            }
            if (key == null || key.isBlank()) {
                key = "UNKNOWN";
            }
            sumBy.put(key, sumBy.getOrDefault(key, BigDecimal.ZERO).add(qty));
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : sumBy.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", e.getKey());
            m.put("value", e.getValue());
            items.add(m);
        }
        return success("库存分布查询成功", items);
    }

    @GetMapping("/alerts")
    public ApiResponse<List<Map<String, Object>>> alerts(@RequestParam(defaultValue = "10") int limit) {
        int n = limit <= 0 ? 10 : Math.min(limit, 50);
        List<InventoryEntity> inventories = inventoryRepository.findAll();
        List<Map<String, Object>> items = new ArrayList<>();
        for (InventoryEntity inv : inventories) {
            if (items.size() >= n) {
                break;
            }
            BigDecimal qty = inv.getQuantity() == null ? BigDecimal.ZERO : inv.getQuantity();
            if (qty.compareTo(LOW_STOCK_THRESHOLD) < 0) {
                items.add(buildAlert(inv, "库存不足", "warning", "当前库存数量低于安全库存"));
            } else if (qty.compareTo(OVER_STOCK_THRESHOLD) > 0) {
                items.add(buildAlert(inv, "库存超量", "danger", "当前库存数量超过最大库存"));
            }
        }
        return success("库存预警查询成功", items);
    }

    private static Map<String, Object> buildAlert(InventoryEntity inv, String type, String level, String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", inv.getId());
        m.put("type", type);
        m.put("level", level);
        m.put("materialName", inv.getMaterialName());
        m.put("message", message);
        LocalDateTime t = inv.getUpdatedTime() == null ? inv.getCreatedTime() : inv.getUpdatedTime();
        m.put("time", com.hxcoe.wms.util.WmsDateTimes.formatOrNull(t));
        return m;
    }

    private double computeTurnover(BigDecimal currentTotal) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<InventoryTransactionEntity> txns = inventoryTransactionRepository.findByTransactionTimeAfter(cutoff);
        BigDecimal out = BigDecimal.ZERO;
        for (InventoryTransactionEntity t : txns) {
            if (t.getQuantity() == null || t.getType() == null) {
                continue;
            }
            if ("OUT".equalsIgnoreCase(t.getType())) {
                out = out.add(t.getQuantity().abs());
            }
        }
        BigDecimal base = currentTotal == null || currentTotal.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ONE : currentTotal;
        return out.divide(base, 4, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    private static BigDecimal toDelta(String type, BigDecimal qty) {
        if (qty == null) {
            return BigDecimal.ZERO;
        }
        if (type == null) {
            return qty;
        }
        String t = type.trim().toUpperCase();
        if ("OUT".equals(t)) {
            return qty.negate();
        }
        if ("MOVE".equals(t)) {
            return BigDecimal.ZERO;
        }
        return qty;
    }

    private static String toLocationGroup(String locationCode) {
        if (locationCode == null || locationCode.isBlank()) {
            return "UNKNOWN";
        }
        String raw = locationCode.trim();
        int idx = raw.indexOf('-');
        if (idx > 0) {
            return raw.substring(0, idx);
        }
        return raw;
    }

    private static String toCategory(String materialCode) {
        if (materialCode == null || materialCode.isBlank()) {
            return "UNKNOWN";
        }
        String raw = materialCode.trim();
        int i = 0;
        while (i < raw.length() && Character.isLetter(raw.charAt(i))) {
            i++;
        }
        if (i <= 0) {
            return "DEFAULT";
        }
        return raw.substring(0, i).toUpperCase();
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }
}

