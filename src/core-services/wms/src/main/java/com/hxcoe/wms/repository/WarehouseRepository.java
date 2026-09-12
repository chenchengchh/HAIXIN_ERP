package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<WarehouseEntity, Long> {
    WarehouseEntity findByWarehouseCode(String warehouseCode);
}
