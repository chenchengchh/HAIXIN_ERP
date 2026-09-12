package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.InventoryTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransactionEntity, Long> {
    List<InventoryTransactionEntity> findByMaterialCode(String materialCode);
    List<InventoryTransactionEntity> findByWarehouseCode(String warehouseCode);
    List<InventoryTransactionEntity> findBySourceNo(String sourceNo);
    List<InventoryTransactionEntity> findByTransactionTimeAfter(LocalDateTime cutoff);
}
