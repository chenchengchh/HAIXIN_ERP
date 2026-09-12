package com.hxcoe.wms.service;

import com.hxcoe.wms.entity.InventoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.math.BigDecimal;

public interface InventoryService {
    Page<InventoryEntity> getInventory(Pageable pageable);
    Page<InventoryEntity> queryInventory(String warehouseCode, String locationCode, String materialCode, String materialName, String status, Pageable pageable);
    Page<InventoryEntity> queryInventory(String warehouseCode, String locationCode, String materialCode, String materialName, String batchNo, String status, Pageable pageable);
    List<InventoryEntity> getInventoryByMaterialCode(String materialCode);
    List<InventoryEntity> getInventoryByLocationCode(String locationCode);
    List<InventoryEntity> getAlertList(String alertType);
    InventoryEntity getById(Long id);
    void addInventory(InventoryEntity inventory);
    void reduceInventory(Long inventoryId, BigDecimal amount);
    void freezeInventory(Long inventoryId, String reason);
    void unfreezeInventory(Long inventoryId);
    void adjustInventory(Long inventoryId, BigDecimal newQuantity, String reason);
    void checkInventory(Long inventoryId, BigDecimal actualQuantity);
}
