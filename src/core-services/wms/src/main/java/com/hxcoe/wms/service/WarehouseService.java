package com.hxcoe.wms.service;

import com.hxcoe.wms.entity.WarehouseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface WarehouseService {
    WarehouseEntity createWarehouse(WarehouseEntity warehouse);
    WarehouseEntity updateWarehouse(Long id, WarehouseEntity warehouse);
    void deleteWarehouse(Long id);
    Page<WarehouseEntity> getWarehouses(Pageable pageable);
    Optional<WarehouseEntity> getWarehouseById(Long id);
}
