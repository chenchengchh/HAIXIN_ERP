package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.SupplyChainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 渚涘簲閾綬epository鎺ュ彛
 */
@Repository
public interface SupplyChainRepository extends JpaRepository<SupplyChainEntity, Long>, JpaSpecificationExecutor<SupplyChainEntity> {

    /**
     * 鏍规嵁渚涘簲閾惧崟鍙锋煡锟?     *
     * @param scNo 渚涘簲閾惧崟锟?     * @return 渚涘簲閾惧疄锟?     */
    SupplyChainEntity findByScNo(String scNo);

    /**
     * 鏍规嵁涓氬姟绫诲瀷鍜岀姸鎬佹煡锟?     *
     * @param businessType 涓氬姟绫诲瀷
     * @param status 鐘讹拷?     * @return 渚涘簲閾惧疄浣撳垪锟?     */
    List<SupplyChainEntity> findByBusinessTypeAndStatus(Integer businessType, Integer status);

    /**
     * 鏍规嵁鐗╂枡缂栫爜鏌ヨ
     *
     * @param materialCode 鐗╂枡缂栫爜
     * @return 渚涘簲閾惧疄浣撳垪锟?     */
    List<SupplyChainEntity> findByMaterialCode(String materialCode);

    /**
     * 鏍规嵁浠撳簱缂栫爜鏌ヨ
     *
     * @param warehouseCode 浠撳簱缂栫爜
     * @return 渚涘簲閾惧疄浣撳垪锟?     */
    List<SupplyChainEntity> findByWarehouseCode(String warehouseCode);

    /**
     * 鏍规嵁涓氬姟绫诲瀷鍜屾棩鏈熻寖鍥存煡锟?     *
     * @param businessType 涓氬姟绫诲瀷
     * @param startDate 寮€濮嬫棩锟?     * @param endDate 缁撴潫鏃ユ湡
     * @return 渚涘簲閾惧疄浣撳垪锟?     */
    List<SupplyChainEntity> findByBusinessTypeAndCreatedTimeBetween(Integer businessType, LocalDateTime startDate, LocalDateTime endDate);
}

