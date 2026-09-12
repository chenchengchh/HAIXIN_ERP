package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping({"/api/v1/wms/warehouse", "/wms/warehouse", "/api/wms/warehouse"})
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @PostMapping("/create")
    public ApiResponse<WarehouseEntity> createWarehouse(@RequestBody WarehouseEntity warehouse) {
        WarehouseEntity result = warehouseService.createWarehouse(warehouse);
        return success("仓库创建成功", result);
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseEntity> updateWarehouse(@PathVariable Long id, @RequestBody WarehouseEntity warehouse) {
        WarehouseEntity result = warehouseService.updateWarehouse(id, warehouse);
        if (result != null) {
            return success("仓库更新成功", result);
        }
        return notFound("仓库不存在");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return success("仓库删除成功", null);
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<WarehouseEntity>> getWarehouses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 前端按1基页码传参，转换为Spring Data 0基页码
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));
        Page<WarehouseEntity> result = warehouseService.getWarehouses(pageable);
        PageResult<WarehouseEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return success("仓库列表查询成功", pageResult);
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseEntity> getWarehouseById(@PathVariable Long id) {
        Optional<WarehouseEntity> result = warehouseService.getWarehouseById(id);
        return result.map(entity -> success("仓库查询成功", entity)).orElseGet(() -> notFound("仓库不存在"));
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
