package com.hxcoe.erp.service;

import com.hxcoe.erp.entity.SupplyChainEntity;
import com.hxcoe.common.result.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 渚涘簲閾炬湇鍔℃帴鍙? */
public interface SupplyChainService {

    /**
     * 鍒涘缓渚涘簲閾捐褰?     *
     * @param supplyChainEntity 渚涘簲閾惧疄浣?     * @return 淇濆瓨鍚庣殑渚涘簲閾惧疄浣?     */
    SupplyChainEntity createSupplyChain(SupplyChainEntity supplyChainEntity);

    /**
     * 鏍规嵁ID鏌ヨ渚涘簲閾捐褰?     *
     * @param id 涓婚敭ID
     * @return 渚涘簲閾惧疄浣?     */
    SupplyChainEntity getSupplyChainById(Long id);

    /**
     * 鏍规嵁渚涘簲閾惧崟鍙锋煡璇緵搴旈摼璁板綍
     *
     * @param scNo 渚涘簲閾惧崟鍙?     * @return 渚涘簲閾惧疄浣?     */
    SupplyChainEntity getSupplyChainByNo(String scNo);

    /**
     * 鏇存柊渚涘簲閾捐褰?     *
     * @param supplyChainEntity 渚涘簲閾惧疄浣?     * @return 鏇存柊鍚庣殑渚涘簲閾惧疄浣?     */
    SupplyChainEntity updateSupplyChain(SupplyChainEntity supplyChainEntity);

    /**
     * 鍒犻櫎渚涘簲閾捐褰?     *
     * @param id 涓婚敭ID
     * @return 鍒犻櫎缁撴灉
     */
    boolean deleteSupplyChain(Long id);

    /**
     * 鍒嗛〉鏌ヨ渚涘簲閾捐褰?     *
     * @param page 褰撳墠椤电爜
     * @param size 姣忛〉鏉℃暟
     * @param businessType 涓氬姟绫诲瀷
     * @param materialCode 鐗╂枡缂栫爜
     * @param warehouseCode 浠撳簱缂栫爜
     * @param status 鐘舵€?     * @param startDate 寮€濮嬫棩鏈?     * @param endDate 缁撴潫鏃ユ湡
     * @return 鍒嗛〉缁撴灉
     */
    PageResult<SupplyChainEntity> getSupplyChainList(Integer page, Integer size, Integer businessType, String materialCode, String warehouseCode, Integer status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 鏍规嵁鐗╂枡缂栫爜鏌ヨ渚涘簲閾捐褰?     *
     * @param materialCode 鐗╂枡缂栫爜
     * @return 渚涘簲閾惧疄浣撳垪琛?     */
    List<SupplyChainEntity> getSupplyChainByMaterialCode(String materialCode);

    /**
     * 鏍规嵁浠撳簱缂栫爜鏌ヨ渚涘簲閾捐褰?     *
     * @param warehouseCode 浠撳簱缂栫爜
     * @return 渚涘簲閾惧疄浣撳垪琛?     */
    List<SupplyChainEntity> getSupplyChainByWarehouseCode(String warehouseCode);
}

