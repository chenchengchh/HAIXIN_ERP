package com.hxcoe.erp.service;

import com.hxcoe.erp.entity.FixedAssetEntity;
import com.hxcoe.common.result.PageResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 鍥哄畾璧勪骇鏈嶅姟鎺ュ彛
 */
public interface FixedAssetService {

    /**
     * 鍒涘缓鍥哄畾璧勪骇
     * @param fixedAssetEntity 鍥哄畾璧勪骇瀹炰綋
     * @return 鍒涘缓缁撴灉
     */
    FixedAssetEntity createFixedAsset(FixedAssetEntity fixedAssetEntity);

    /**
     * 鏍规嵁ID鏌ヨ鍥哄畾璧勪骇
     * @param id 涓婚敭ID
     * @return 鏌ヨ缁撴灉
     */
    FixedAssetEntity getFixedAssetById(Long id);

    /**
     * 鏍规嵁璧勪骇缂栧彿鏌ヨ鍥哄畾璧勪骇
     * @param assetNo 璧勪骇缂栧彿
     * @return 鏌ヨ缁撴灉
     */
    FixedAssetEntity getFixedAssetByNo(String assetNo);

    /**
     * 鏇存柊鍥哄畾璧勪骇
     * @param fixedAssetEntity 鍥哄畾璧勪骇瀹炰綋
     * @return 鏇存柊缁撴灉
     */
    FixedAssetEntity updateFixedAsset(FixedAssetEntity fixedAssetEntity);

    /**
     * 鍒犻櫎鍥哄畾璧勪骇
     * @param id 涓婚敭ID
     * @return 鍒犻櫎缁撴灉
     */
    boolean deleteFixedAsset(Long id);

    /**
     * 鍒嗛〉鏌ヨ鍥哄畾璧勪骇鍒楄〃
     * @param page 褰撳墠椤电爜
     * @param size 姣忛〉鏉℃暟
     * @param assetName 璧勪骇鍚嶇О
     * @param assetCategory 璧勪骇绫诲埆
     * @param status 鐘舵€?     * @param usingDepartment 浣跨敤閮ㄩ棬
     * @param startDate 寮€濮嬫棩鏈?     * @param endDate 缁撴潫鏃ユ湡
     * @return 鍒嗛〉缁撴灉
     */
    PageResult<FixedAssetEntity> getFixedAssetList(
            Integer page, 
            Integer size, 
            String assetName, 
            String assetCategory, 
            Integer status, 
            String usingDepartment,
            LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * 鐢熸垚璧勪骇缂栧彿
     * @param assetCategory 璧勪骇绫诲埆
     * @return 璧勪骇缂栧彿
     */
    String generateAssetNo(String assetCategory);

    /**
     * 璁＄畻鍥哄畾璧勪骇鏈堟姌鏃ч
     * @param originalValue 鍘熷€?     * @param residualValue 娈嬪€?     * @param expectedUsageYears 棰勮浣跨敤骞撮檺
     * @return 鏈堟姌鏃ч
     */
    BigDecimal calculateMonthlyDepreciation(BigDecimal originalValue, BigDecimal residualValue, Integer expectedUsageYears);

    /**
     * 璁℃彁鍥哄畾璧勪骇鎶樻棫
     * @param id 鍥哄畾璧勪骇ID
     * @param depreciationDate 鎶樻棫鏃ユ湡
     * @return 璁℃彁缁撴灉
     */
    FixedAssetEntity calculateDepreciation(Long id, LocalDateTime depreciationDate);

    /**
     * 鎵归噺璁℃彁鍥哄畾璧勪骇鎶樻棫
     * @param assetIds 鍥哄畾璧勪骇ID鍒楄〃
     * @param depreciationDate 鎶樻棫鏃ユ湡
     * @return 璁℃彁缁撴灉
     */
    List<FixedAssetEntity> batchCalculateDepreciation(List<Long> assetIds, LocalDateTime depreciationDate);

    /**
     * 鑷姩璁℃彁鍥哄畾璧勪骇鎶樻棫
     * 璁℃彁鎵€鏈夐渶瑕佹姌鏃х殑鍥哄畾璧勪骇
     * @param depreciationDate 鎶樻棫鏃ユ湡
     * @return 璁℃彁缁撴灉
     */
    List<FixedAssetEntity> autoCalculateDepreciation(LocalDateTime depreciationDate);

    /**
     * 鍥哄畾璧勪骇澶勭疆
     * @param id 鍥哄畾璧勪骇ID
     * @param disposalDate 澶勭疆鏃ユ湡
     * @param disposalReason 澶勭疆鍘熷洜
     * @param disposalAmount 澶勭疆閲戦
     * @return 澶勭疆缁撴灉
     */
    FixedAssetEntity disposeFixedAsset(Long id, LocalDateTime disposalDate, String disposalReason, BigDecimal disposalAmount);

    /**
     * 鍥哄畾璧勪骇鐩樼偣
     * @param assetIds 鍥哄畾璧勪骇ID鍒楄〃
     * @param inventoryDate 鐩樼偣鏃ユ湡
     * @param inventoryPerson 鐩樼偣浜?     * @return 鐩樼偣缁撴灉
     */
    List<FixedAssetEntity> inventoryFixedAssets(List<Long> assetIds, LocalDateTime inventoryDate, String inventoryPerson);

    /**
     * 璁＄畻鍥哄畾璧勪骇鍑€鍊?     * @param originalValue 鍘熷€?     * @param accumulatedDepreciation 绱鎶樻棫
     * @return 鍑€鍊?     */
    BigDecimal calculateNetValue(BigDecimal originalValue, BigDecimal accumulatedDepreciation);

    /**
     * 缁熻鍥哄畾璧勪骇鏁伴噺
     * @param status 鐘舵€?     * @return 缁熻缁撴灉
     */
    long countFixedAssets(Integer status);

    /**
     * 鑾峰彇鍥哄畾璧勪骇绫诲埆鍒楄〃
     * @return 璧勪骇绫诲埆鍒楄〃
     */
    List<String> getAssetCategories();

    /**
     * 瀵煎嚭鍥哄畾璧勪骇鍒楄〃
     * @param params 鏌ヨ鍙傛暟
     * @return 瀵煎嚭缁撴灉
     */
    byte[] exportFixedAssets(Object params);
}
