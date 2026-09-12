package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.ProductionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 鐢熶骇Repository鎺ュ彛
 */
@Repository
public interface ProductionRepository extends JpaRepository<ProductionEntity, Long>, JpaSpecificationExecutor<ProductionEntity> {

    /**
     * 鏍规嵁鐢熶骇鍗曞彿鏌ヨ
     *
     * @param productionNo 鐢熶骇鍗曞彿
     * @return 鐢熶骇瀹炰綋
     */
    ProductionEntity findByProductionNo(String productionNo);

    /**
     * 鏍规嵁鐢熶骇鐘舵€佹煡锟?     *
     * @param productionStatus 鐢熶骇鐘讹拷?     * @return 鐢熶骇瀹炰綋鍒楄〃
     */
    List<ProductionEntity> findByProductionStatus(Integer productionStatus);

    /**
     * 鏍规嵁浜у搧缂栫爜鏌ヨ
     *
     * @param productCode 浜у搧缂栫爜
     * @return 鐢熶骇瀹炰綋鍒楄〃
     */
    List<ProductionEntity> findByProductCode(String productCode);

    /**
     * 鏍规嵁杞﹂棿鏌ヨ
     *
     * @param workshop 杞﹂棿
     * @return 鐢熶骇瀹炰綋鍒楄〃
     */
    List<ProductionEntity> findByWorkshop(String workshop);

    /**
     * 鏍规嵁瀹為檯寮€濮嬫椂闂磋寖鍥存煡锟?     *
     * @param startDate 寮€濮嬫棩锟?     * @param endDate 缁撴潫鏃ユ湡
     * @return 鐢熶骇瀹炰綋鍒楄〃
     */
    List<ProductionEntity> findByActualStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
}

