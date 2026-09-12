package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long>, JpaSpecificationExecutor<InventoryEntity> {
    List<InventoryEntity> findByMaterialCode(String materialCode);
    List<InventoryEntity> findByWarehouseCode(String warehouseCode);
    List<InventoryEntity> findByLocationCode(String locationCode);
    List<InventoryEntity> findByQuantityLessThan(BigDecimal threshold);
    List<InventoryEntity> findByQuantityGreaterThan(BigDecimal threshold);

    /**
     * 按物料编码+仓库编码查询库存记录（EAM备件领用扣减用）
     *
     * @param materialCode  物料编码
     * @param warehouseCode 仓库编码
     * @return 库存记录列表
     */
    List<InventoryEntity> findByMaterialCodeAndWarehouseCode(String materialCode, String warehouseCode);

    // 用于查找特定批次、库位的库存记录
    java.util.Optional<InventoryEntity> findByMaterialCodeAndWarehouseCodeAndLocationCodeAndBatchNo(
        String materialCode, String warehouseCode, String locationCode, String batchNo
    );
}
