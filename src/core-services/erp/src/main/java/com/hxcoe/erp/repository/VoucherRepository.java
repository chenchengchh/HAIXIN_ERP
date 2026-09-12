package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.VoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 鍑瘉Repository鎺ュ彛
 */
public interface VoucherRepository extends JpaRepository<VoucherEntity, Long>, JpaSpecificationExecutor<VoucherEntity> {

    /**
     * 鏍规嵁鍑瘉缂栧彿鏌ヨ鍑瘉
     *
     * @param voucherNo 鍑瘉缂栧彿
     * @return 鍑瘉瀹炰綋
     */
    Optional<VoucherEntity> findByVoucherNo(String voucherNo);

    /**
     * 鏍规嵁鐘舵€佹煡璇㈠嚟璇佹暟锟?     *
     * @param status 鐘讹拷?     * @return 鍑瘉鏁伴噺
     */
    long countByStatus(String status);

    /**
     * 鏍规嵁鏃ユ湡鑼冨洿鏌ヨ鍑瘉鏁伴噺
     *
     * @param startDate 寮€濮嬫棩锟?     * @param endDate 缁撴潫鏃ユ湡
     * @return 鍑瘉鏁伴噺
     */
    long countByVoucherDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}

