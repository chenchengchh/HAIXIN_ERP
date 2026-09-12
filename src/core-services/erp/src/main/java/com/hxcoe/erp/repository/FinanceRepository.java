package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.FinanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 财务 Repository 接口
 */
@Repository
public interface FinanceRepository extends JpaRepository<FinanceEntity, Long>, JpaSpecificationExecutor<FinanceEntity> {

    /**
     * 根据财务单号查询
     * @param financeNo 财务单号
     * @return 财务实体
     */
    FinanceEntity findByFinanceNo(String financeNo);

    /**
     * 根据交易类型和状态查询
     * @param transactionType 交易类型
     * @param status 状态
     * @return 财务实体列表
     */
    List<FinanceEntity> findByTransactionTypeAndStatus(Integer transactionType, Integer status);

    /**
     * 根据交易日期范围查询
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 财务实体列表
     */
    List<FinanceEntity> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 按交易类型聚合日期范围内的总金额（SQL 聚合，避免全表加载）
     * @param transactionType 交易类型（1=收入 2=支出）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总金额，无数据返回 0
     */
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinanceEntity f " +
           "WHERE f.transactionType = :transactionType AND f.isDeleted = 0 " +
           "AND f.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByTypeAndDateRange(@Param("transactionType") Integer transactionType,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}

