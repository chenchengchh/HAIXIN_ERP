package com.hxcoe.erp.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.WarehouseEntity;

public interface WarehouseService {
    PageResult<WarehouseEntity> getWarehouseList(Integer page, Integer size, String name, String code);
    WarehouseEntity createWarehouse(WarehouseEntity warehouse);
    WarehouseEntity updateWarehouse(WarehouseEntity warehouse);
    boolean deleteWarehouse(Long id);
    WarehouseEntity getWarehouseById(Long id);
}
