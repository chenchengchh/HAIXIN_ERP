package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.WmsClient;
import com.hxcoe.scm.client.dto.WmsInventoryDTO;
import com.hxcoe.scm.entity.InventoryStrategyEntity;
import com.hxcoe.scm.repository.InventoryStrategyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/scm/inventory-health", "/api/scm/inventory-health"})
public class InventoryHealthController {

    @Autowired
    private InventoryStrategyRepository inventoryStrategyRepository;

    @Autowired
    private WmsClient wmsClient;

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> getSummary(@RequestParam(defaultValue = "30") Integer days) {
        int d = days == null || days <= 0 ? 30 : days;
        List<InventoryStrategyEntity> strategies = inventoryStrategyRepository.findAll();
        BigDecimal totalQty = BigDecimal.ZERO;
        int lowStockCount = 0;
        BigDecimal outQty = BigDecimal.ZERO;

        for (InventoryStrategyEntity s : strategies) {
            BigDecimal qty = sumInventoryQty(s.getMaterialCode());
            totalQty = totalQty.add(qty);
            if (s.getReorderPoint() != null && qty.compareTo(s.getReorderPoint()) < 0) {
                lowStockCount++;
            }
            outQty = outQty.add(sumOutboundQty(s.getMaterialCode(), d));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("days", d);
        res.put("totalQuantity", totalQty);
        res.put("lowStockCount", lowStockCount);
        res.put("outboundQuantity", outQty);
        res.put("turnoverRate", totalQty.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : outQty.divide(totalQty, 6, java.math.RoundingMode.HALF_UP));
        return success("成功", res);
    }

    @GetMapping("/distribution")
    public ApiResponse<List<Map<String, Object>>> getDistribution(@RequestParam(defaultValue = "abc") String dimension) {
        List<InventoryStrategyEntity> strategies = inventoryStrategyRepository.findAll();
        Map<String, BigDecimal> sums = new HashMap<>();
        for (InventoryStrategyEntity s : strategies) {
            BigDecimal qty = sumInventoryQty(s.getMaterialCode());
            String key;
            if ("material".equalsIgnoreCase(dimension)) {
                key = s.getMaterialCode();
            } else {
                key = s.getAbcClass() == null ? "UNKNOWN" : s.getAbcClass();
            }
            sums.merge(key, qty, BigDecimal::add);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : sums.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("name", e.getKey());
            row.put("value", e.getValue());
            rows.add(row);
        }
        rows.sort((a, b) -> ((BigDecimal) b.get("value")).compareTo((BigDecimal) a.get("value")));
        return success("成功", rows);
    }

    @GetMapping("/turnover/trend")
    public ApiResponse<List<Map<String, Object>>> getTurnoverTrend(@RequestParam(defaultValue = "30") Integer days) {
        int d = days == null || days <= 0 ? 30 : days;
        List<InventoryStrategyEntity> strategies = inventoryStrategyRepository.findAll();
        Map<LocalDate, BigDecimal> outByDay = new HashMap<>();
        for (InventoryStrategyEntity s : strategies) {
            List<Map<String, Object>> tx = getTransactions(s.getMaterialCode(), d);
            for (Map<String, Object> t : tx) {
                if (!"OUT".equals(String.valueOf(t.get("type")))) continue;
                Object timeObj = t.get("transactionTime");
                Object qtyObj = t.get("quantity");
                if (timeObj == null || qtyObj == null) continue;
                LocalDate day = parseToLocalDate(timeObj);
                if (day == null) continue;
                BigDecimal q = new BigDecimal(String.valueOf(qtyObj));
                outByDay.merge(day, q, BigDecimal::add);
            }
        }
        List<LocalDate> daysList = new ArrayList<>(outByDay.keySet());
        daysList.sort(LocalDate::compareTo);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (LocalDate day : daysList) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", day.toString());
            row.put("outboundQuantity", outByDay.get(day));
            rows.add(row);
        }
        return success("成功", rows);
    }

    @GetMapping("/alerts")
    public ApiResponse<List<Map<String, Object>>> getAlerts(@RequestParam(defaultValue = "50") Integer limit) {
        int l = limit == null || limit <= 0 ? 50 : limit;
        List<InventoryStrategyEntity> strategies = inventoryStrategyRepository.findAll();
        List<Map<String, Object>> alerts = new ArrayList<>();
        for (InventoryStrategyEntity s : strategies) {
            BigDecimal qty = sumInventoryQty(s.getMaterialCode());
            if (s.getReorderPoint() != null && qty.compareTo(s.getReorderPoint()) < 0) {
                Map<String, Object> a = new HashMap<>();
                a.put("type", "LOW_STOCK");
                a.put("materialCode", s.getMaterialCode());
                a.put("materialName", s.getMaterialName());
                a.put("quantity", qty);
                a.put("reorderPoint", s.getReorderPoint());
                a.put("time", LocalDateTime.now());
                alerts.add(a);
            }
            if (alerts.size() >= l) break;
        }
        return success("成功", alerts);
    }

    private BigDecimal sumInventoryQty(String materialCode) {
        try {
            var res = wmsClient.getInventoryByMaterialCode(materialCode);
            if (res != null && ResponseStatusAdapter.isSuccess(res.getCode()) && res.getData() != null) {
                BigDecimal sum = BigDecimal.ZERO;
                for (WmsInventoryDTO inv : res.getData()) {
                    if (inv != null && inv.getQuantity() != null) {
                        sum = sum.add(inv.getQuantity());
                    }
                }
                return sum;
            }
        } catch (Exception e) {
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal sumOutboundQty(String materialCode, int days) {
        List<Map<String, Object>> tx = getTransactions(materialCode, days);
        BigDecimal sum = BigDecimal.ZERO;
        for (Map<String, Object> t : tx) {
            if (!"OUT".equals(String.valueOf(t.get("type")))) continue;
            Object qtyObj = t.get("quantity");
            if (qtyObj == null) continue;
            sum = sum.add(new BigDecimal(String.valueOf(qtyObj)));
        }
        return sum;
    }

    private List<Map<String, Object>> getTransactions(String materialCode, int days) {
        try {
            var res = wmsClient.getTransactions(materialCode, days);
            if (res != null && ResponseStatusAdapter.isSuccess(res.getCode()) && res.getData() != null) {
                return res.getData();
            }
        } catch (Exception e) {
        }
        return List.of();
    }

    private LocalDate parseToLocalDate(Object timeObj) {
        try {
            String s = String.valueOf(timeObj);
            if (s.length() >= 10) {
                return LocalDate.parse(s.substring(0, 10));
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }
}

