package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/wms/inventory", "/wms/inventory", "/api/wms/inventory"})
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping
    public ApiResponse<InventoryEntity> createInventory(@RequestBody InventoryEntity inventory) {
        inventoryService.addInventory(inventory);
        return success("库存创建成功", inventory);
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<InventoryEntity>> getInventory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String locationCode,
            @RequestParam(required = false) String materialCode,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String batchNo,
            @RequestParam(required = false) String status
    ) {
        // 前端按1基页码传参，转换为Spring Data 0基页码
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "updatedTime"));
        Page<InventoryEntity> result = inventoryService.queryInventory(warehouseCode, locationCode, materialCode, materialName, batchNo, status, pageable);
        return success("库存列表查询成功", PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        ));
    }

    @GetMapping("/by-material/{materialCode}")
    public ApiResponse<List<InventoryEntity>> getInventoryByMaterialCode(@PathVariable String materialCode) {
        List<InventoryEntity> result = inventoryService.getInventoryByMaterialCode(materialCode);
        return success("库存查询成功", result);
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryEntity> getInventoryDetail(@PathVariable Long id) {
        InventoryEntity result = inventoryService.getById(id);
        return success("库存详情查询成功", result);
    }

    @GetMapping("/by-location/{locationCode}")
    public ApiResponse<List<InventoryEntity>> getInventoryByLocation(@PathVariable String locationCode) {
        List<InventoryEntity> result = inventoryService.getInventoryByLocationCode(locationCode);
        return success("库位库存查询成功", result);
    }

    @GetMapping("/alerts")
    public ApiResponse<List<InventoryEntity>> getAlertList(@RequestParam(required = false) String alertType) {
        List<InventoryEntity> result = inventoryService.getAlertList(alertType);
        return success("库存预警查询成功", result);
    }

    @PutMapping("/{id}/freeze")
    public ApiResponse<Void> freezeInventory(@PathVariable Long id, @RequestParam(required = false) String reason) {
        inventoryService.freezeInventory(id, reason);
        return success("库存冻结成功", null);
    }

    @PutMapping("/{id}/unfreeze")
    public ApiResponse<Void> unfreezeInventory(@PathVariable Long id) {
        inventoryService.unfreezeInventory(id);
        return success("库存解冻成功", null);
    }

    @PutMapping("/{id}/adjust")
    public ApiResponse<Void> adjustInventory(@PathVariable Long id, @RequestParam BigDecimal newQuantity, @RequestParam(required = false) String reason) {
        inventoryService.adjustInventory(id, newQuantity, reason);
        return success("库存调整成功", null);
    }

    @PutMapping("/{id}/check")
    public ApiResponse<Void> checkInventory(@PathVariable Long id, @RequestParam BigDecimal actualQuantity) {
        inventoryService.checkInventory(id, actualQuantity);
        return success("库存盘点成功", null);
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }
}
