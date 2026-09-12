package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.CustomerContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户联系人Repository接口
 */
@Repository
public interface CustomerContactRepository extends JpaRepository<CustomerContactEntity, Long>, JpaSpecificationExecutor<CustomerContactEntity> {

    /**
     * 根据客户ID查询联系人
     *
     * @param customerId 客户ID
     * @return 联系人列表
     */
    List<CustomerContactEntity> findByCustomerId(Long customerId);

    /**
     * 根据客户ID查询主要联系人
     *
     * @param customerId 客户ID
     * @param isPrimary 是否主要联系人
     * @return 主要联系人
     */
    CustomerContactEntity findByCustomerIdAndIsPrimary(Long customerId, Boolean isPrimary);
}