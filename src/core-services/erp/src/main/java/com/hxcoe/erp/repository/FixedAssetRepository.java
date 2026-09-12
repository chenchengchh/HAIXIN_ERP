package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.FixedAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 鍥哄畾璧勪骇浠撳簱鎺ュ彛
 */
public interface FixedAssetRepository extends JpaRepository<FixedAssetEntity, Long>, JpaSpecificationExecutor<FixedAssetEntity> {

    /**
     * 鏍规嵁璧勪骇缂栧彿鏌ヨ鍥哄畾璧勪骇
     * @param assetNo 璧勪骇缂栧彿
     * @return 鍥哄畾璧勪骇瀹炰綋
     */
    FixedAssetEntity findByAssetNo(String assetNo);

    /**
     * 鏍规嵁璧勪骇绫诲埆鏌ヨ鍥哄畾璧勪骇鍒楄〃
     * @param assetCategory 璧勪骇绫诲埆
     * @return 鍥哄畾璧勪骇鍒楄〃
     */
    List<FixedAssetEntity> findByAssetCategory(String assetCategory);

    /**
     * 鏍规嵁鐘舵€佹煡璇㈠浐瀹氳祫浜у垪琛?     * @param status 鐘舵€?     * @return 鍥哄畾璧勪骇鍒楄〃
     */
    List<FixedAssetEntity> findByStatus(Integer status);

    /**
     * 鏌ヨ鏈€鍚庝竴娆℃姌鏃ф棩鏈熷湪鎸囧畾鏃ユ湡涔嬪墠鐨勫浐瀹氳祫浜?     * @param lastDepreciationDate 鏈€鍚庢姌鏃ф棩鏈?     * @return 鍥哄畾璧勪骇鍒楄〃
     */
    List<FixedAssetEntity> findByLastDepreciationDateBefore(LocalDateTime lastDepreciationDate);

    /**
     * 鏌ヨ闇€瑕佹姌鏃х殑鍥哄畾璧勪骇
     * 鐘舵€佷负鍦ㄧ敤锛?锛変笖鏈€鍚庢姌鏃ф棩鏈熷湪鎸囧畾鏃ユ湡涔嬪墠
     * @param status 鐘舵€?     * @param lastDepreciationDate 鏈€鍚庢姌鏃ф棩鏈?     * @return 鍥哄畾璧勪骇鍒楄〃
     */
    List<FixedAssetEntity> findByStatusAndLastDepreciationDateBefore(Integer status, LocalDateTime lastDepreciationDate);

    /**
     * 鏍规嵁浣跨敤閮ㄩ棬鏌ヨ鍥哄畾璧勪骇
     * @param usingDepartment 浣跨敤閮ㄩ棬
     * @return 鍥哄畾璧勪骇鍒楄〃
     */
    List<FixedAssetEntity> findByUsingDepartment(String usingDepartment);

    /**
     * 鏍规嵁璐疆鏃ユ湡鑼冨洿鏌ヨ鍥哄畾璧勪骇
     * @param startDate 寮€濮嬫棩鏈?     * @param endDate 缁撴潫鏃ユ湡
     * @return 鍥哄畾璧勪骇鍒楄〃
     */
    List<FixedAssetEntity> findByPurchaseDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 缁熻鍥哄畾璧勪骇鏁伴噺
     * @param status 鐘舵€?     * @param isDeleted 鏄惁鍒犻櫎
     * @return 鍥哄畾璧勪骇鏁伴噺
     */
    long countByStatusAndIsDeleted(Integer status, Integer isDeleted);

    /**
     * 鏌ヨ鏈垹闄ょ殑鍥哄畾璧勪骇
     * @param isDeleted 鏄惁鍒犻櫎
     * @return 鍥哄畾璧勪骇鍒楄〃
     */
    List<FixedAssetEntity> findByIsDeleted(Integer isDeleted);

    /**
     * 鏍规嵁璧勪骇鍚嶇О妯＄硦鏌ヨ鍥哄畾璧勪骇
     * @param assetName 璧勪骇鍚嶇О
     * @param isDeleted 鏄惁鍒犻櫎
     * @return 鍥哄畾璧勪骇鍒楄〃
     */
    @Query("SELECT a FROM FixedAssetEntity a WHERE a.assetName LIKE %:assetName% AND a.isDeleted = :isDeleted")
    List<FixedAssetEntity> findByAssetNameContainingAndIsDeleted(@Param("assetName") String assetName, @Param("isDeleted") Integer isDeleted);
}
