package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.VoucherItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 凭证分录 Repository 接口
 */
public interface VoucherItemRepository extends JpaRepository<VoucherItemEntity, Long> {

    /**
     * 根据凭证ID查询凭证分录
     * @param voucherId 凭证ID
     * @return 凭证分录列表
     */
    List<VoucherItemEntity> findByVoucherIdOrderBySortOrder(Long voucherId);

    /**
     * 根据凭证ID删除凭证分录
     * @param voucherId 凭证ID
     */
    void deleteByVoucherId(Long voucherId);

    /**
     * 按科目分组汇总日期范围内的借贷总额
     * 一次性 GROUP BY 替代 N+1 内存遍历，用于总账报表
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 三元组列表 [accountId, debitTotal, creditTotal]
     */
    @Query("SELECT vi.account.id AS accountId, " +
           "       COALESCE(SUM(vi.debitAmount), 0) AS debitTotal, " +
           "       COALESCE(SUM(vi.creditAmount), 0) AS creditTotal " +
           "FROM VoucherItemEntity vi " +
           "WHERE (:startDate IS NULL OR vi.voucher.voucherDate >= :startDate) " +
           "  AND (:endDate IS NULL OR vi.voucher.voucherDate <= :endDate) " +
           "GROUP BY vi.account.id")
    List<Object[]> sumByAccountGrouped(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}

