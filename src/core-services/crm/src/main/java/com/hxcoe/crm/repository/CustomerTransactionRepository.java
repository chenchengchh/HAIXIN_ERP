package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.CustomerTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户交易记录Repository接口
 */
@Repository
public interface CustomerTransactionRepository extends JpaRepository<CustomerTransactionEntity, Long>, JpaSpecificationExecutor<CustomerTransactionEntity> {

    /**
     * 根据客户ID查询交易记录
     *
     * @param customerId 客户ID
     * @return 交易记录列表
     */
    List<CustomerTransactionEntity> findByCustomerIdOrderByDealDateDesc(Long customerId);

    /**
     * 根据订单编号查询交易记录
     *
     * @param orderNo 订单编号
     * @return 交易记录
     */
    CustomerTransactionEntity findByOrderNo(String orderNo);
}