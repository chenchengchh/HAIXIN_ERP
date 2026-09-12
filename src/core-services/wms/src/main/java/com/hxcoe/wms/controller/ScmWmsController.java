package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.wms.dto.WmsInventoryDTO;
import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.entity.InventoryTransactionEntity;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.repository.InventoryTransactionRepository;
import com.hxcoe.wms.service.InventoryService;
import com.hxcoe.wms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wms")
public class ScmWmsController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @GetMapping("/inventory/material/{materialCode}")
    public ApiResponse<List<WmsInventoryDTO>> getInventoryByMaterialCode(@PathVariable String materialCode) {
        List<InventoryEntity> list = inventoryService.getInventoryByMaterialCode(materialCode);
        List<WmsInventoryDTO> dtos = new ArrayList<>();
        for (InventoryEntity inv : list) {
            WmsInventoryDTO dto = new WmsInventoryDTO();
            dto.setId(inv.getId());
            dto.setMaterialCode(inv.getMaterialCode());
            dto.setMaterialName(inv.getMaterialName());
            dto.setWarehouseCode(inv.getWarehouseCode());
            dto.setQuantity(inv.getQuantity());
            dto.setLockedQuantity(BigDecimal.ZERO);
            dto.setUnit(inv.getUnit());
            dtos.add(dto);
        }
        return success("库存查询成功", dtos);
    }

    @GetMapping("/warehouses")
    public ApiResponse<List<Map<String, Object>>> getWarehouses() {
        Page<WarehouseEntity> page = warehouseService.getWarehouses(Pageable.unpaged());
        List<Map<String, Object>> rows = new ArrayList<>();
        for (WarehouseEntity w : page.getContent()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", w.getId());
            m.put("warehouseCode", w.getWarehouseCode());
            m.put("warehouseName", w.getWarehouseName());
            m.put("status", w.getStatus());
            rows.add(m);
        }
        return success("仓库列表查询成功", rows);
    }

    @GetMapping("/inventory/transactions")
    public ApiResponse<List<Map<String, Object>>> getTransactions(@RequestParam("materialCode") String materialCode,
                                                                  @RequestParam(value = "days", required = false) Integer days) {
        int d = (days == null || days <= 0) ? 30 : days;
        LocalDateTime cutoff = LocalDateTime.now().minusDays(d);
        List<InventoryTransactionEntity> list = inventoryTransactionRepository.findByMaterialCode(materialCode);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (InventoryTransactionEntity t : list) {
            if (t.getTransactionTime() != null && t.getTransactionTime().isBefore(cutoff)) {
                continue;
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("type", t.getType());
            m.put("warehouseCode", t.getWarehouseCode());
            m.put("locationCode", t.getLocationCode());
            m.put("materialCode", t.getMaterialCode());
            m.put("materialName", t.getMaterialName());
            m.put("quantity", t.getQuantity());
            m.put("unit", t.getUnit());
            m.put("batchNo", t.getBatchNo());
            m.put("sourceNo", t.getSourceNo());
            m.put("transactionTime", t.getTransactionTime());
            m.put("operator", t.getOperator());
            rows.add(m);
        }
        return success("交易记录查询成功", rows);
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }
}

