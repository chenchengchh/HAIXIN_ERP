package com.hxcoe.scrm.repository;

import com.hxcoe.scrm.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 客户仓库接口
 */
@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long>, JpaSpecificationExecutor<CustomerEntity> {
    
    /**
     * 根据客户编号查询客户
     * @param customerCode 客户编号
     * @return 客户实体
     */
    CustomerEntity findByCustomerCode(String customerCode);
    
    /**
     * 根据手机号码查询客户
     * @param phone 手机号码
     * @return 客户实体
     */
    CustomerEntity findByPhone(String phone);
    
    /**
     * 根据邮箱查询客户
     * @param email 邮箱
     * @return 客户实体
     */
    CustomerEntity findByEmail(String email);
    
    /**
     * 根据客户状态查询客户
     * @param status 客户状态
     * @return 客户列表
     */
    java.util.List<CustomerEntity> findByStatus(String status);
}
