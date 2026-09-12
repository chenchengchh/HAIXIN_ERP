package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/wms/pda/inventory", "/wms/pda/inventory", "/api/wms/pda/inventory"})
public class PdaInventoryController {

    private static final BigDecimal LOW_STOCK_THRESHOLD = new BigDecimal("10");
    private static final BigDecimal OVER_STOCK_THRESHOLD = new BigDecimal("1000");

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping("/query")
    public ApiResponse<Map<String, Object>> queryInventory(
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String materialCode,
            @RequestParam(required = false) String locationCode,
            @RequestParam(required = false) String batchNo,
            @RequestParam(required = false) String warehouseCode
    ) {
        String code = normalize(materialCode);
        if ((code == null || code.isBlank()) && barcode != null && !barcode.isBlank()) {
            code = barcode.trim();
        }
        final String codeFinal = code;

        List<InventoryEntity> list = inventoryRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (warehouseCode != null && !warehouseCode.isBlank()) {
                predicates.add(cb.equal(root.get("warehouseCode"), warehouseCode.trim()));
            }
            if (locationCode != null && !locationCode.isBlank()) {
                predicates.add(cb.like(root.get("locationCode"), "%" + locationCode.trim() + "%"));
            }
            if (batchNo != null && !batchNo.isBlank()) {
                predicates.add(cb.like(root.get("batchNo"), "%" + batchNo.trim() + "%"));
            }
            if (codeFinal != null && !codeFinal.isBlank()) {
                predicates.add(cb.like(root.get("materialCode"), "%" + codeFinal.trim() + "%"));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });

        if (list.isEmpty()) {
            return notFound("未查询到库存记录");
        }

        String materialCodeResolved = firstNonBlank(list, InventoryEntity::getMaterialCode);
        String materialNameResolved = firstNonBlank(list, InventoryEntity::getMaterialName);
        String unitResolved = firstNonBlank(list, InventoryEntity::getUnit);
        String warehouseCodeResolved = normalize(warehouseCode) != null ? warehouseCode.trim() : firstNonBlank(list, InventoryEntity::getWarehouseCode);

        BigDecimal totalStock = BigDecimal.ZERO;
        for (InventoryEntity inv : list) {
            if (inv.getQuantity() != null) {
                totalStock = totalStock.add(inv.getQuantity());
            }
        }

        BigDecimal availableStock = totalStock;
        BigDecimal lockedStock = BigDecimal.ZERO;

        Map<String, Object> info = new HashMap<>();
        info.put("materialCode", materialCodeResolved);
        info.put("materialName", materialNameResolved);
        info.put("specification", "");
        info.put("unit", unitResolved == null ? "" : unitResolved);
        info.put("totalStock", totalStock);
        info.put("availableStock", availableStock);
        info.put("lockedStock", lockedStock);
        info.put("warehouseCode", warehouseCodeResolved);
        info.put("warehouseName", warehouseCodeResolved);

        List<Map<String, Object>> details = new ArrayList<>();
        for (InventoryEntity inv : list) {
            Map<String, Object> d = new HashMap<>();
            d.put("locationCode", inv.getLocationCode());
            d.put("batchNo", inv.getBatchNo());
            d.put("quantity", inv.getQuantity());
            d.put("availableQuantity", inv.getQuantity());
            d.put("lockedQuantity", BigDecimal.ZERO);
            d.put("status", toStatus(inv.getQuantity()));
            d.put("expiryDate", null);
            LocalDateTime t = inv.getUpdatedTime() == null ? inv.getCreatedTime() : inv.getUpdatedTime();
            d.put("lastStockTime", com.hxcoe.wms.util.WmsDateTimes.formatOrNull(t));
            details.add(d);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("stockInfo", info);
        data.put("stockDetails", details);
        return success("库存查询成功", data);
    }

    private static String normalize(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String toStatus(BigDecimal qty) {
        BigDecimal q = qty == null ? BigDecimal.ZERO : qty;
        if (q.compareTo(LOW_STOCK_THRESHOLD) < 0 || q.compareTo(OVER_STOCK_THRESHOLD) > 0) {
            return "abnormal";
        }
        return "normal";
    }

    private static String firstNonBlank(List<InventoryEntity> list, java.util.function.Function<InventoryEntity, String> getter) {
        for (InventoryEntity inv : list) {
            String v = getter.apply(inv);
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "";
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
