package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.CustomerFollowUpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户跟进记录Repository接口
 */
@Repository
public interface CustomerFollowUpRepository extends JpaRepository<CustomerFollowUpEntity, Long>, JpaSpecificationExecutor<CustomerFollowUpEntity> {

    /**
     * 根据客户ID查询跟进记录
     *
     * @param customerId 客户ID
     * @return 跟进记录列表
     */
    List<CustomerFollowUpEntity> findByCustomerIdOrderByFollowUpTimeDesc(Long customerId);

    /**
     * 根据跟进人ID查询跟进记录
     *
     * @param followUpUserId 跟进人ID
     * @return 跟进记录列表
     */
    List<CustomerFollowUpEntity> findByFollowUpUserIdOrderByFollowUpTimeDesc(Long followUpUserId);
}