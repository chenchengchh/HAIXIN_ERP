package com.hxcoe.scrm.service;

import com.hxcoe.scrm.entity.CustomerEntity;
import com.hxcoe.common.result.PageResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 客户服务接口
 */
public interface CustomerService {
    
    /**
     * 创建客户
     * @param customer 客户实体
     * @return 创建后的客户实体
     */
    CustomerEntity createCustomer(CustomerEntity customer);
    
    /**
     * 更新客户
     * @param id 客户ID
     * @param customer 更新的客户实体
     * @return 更新后的客户实体
     */
    CustomerEntity updateCustomer(Long id, CustomerEntity customer);
    
    /**
     * 删除客户
     * @param id 客户ID
     */
    void deleteCustomer(Long id);
    
    /**
     * 根据ID查询客户
     * @param id 客户ID
     * @return 客户实体
     */
    Optional<CustomerEntity> getCustomerById(Long id);
    
    /**
     * 根据客户编号查询客户
     * @param customerCode 客户编号
     * @return 客户实体
     */
    CustomerEntity getCustomerByCustomerCode(String customerCode);
    
    /**
     * 根据手机号码查询客户
     * @param phone 手机号码
     * @return 客户实体
     */
    CustomerEntity getCustomerByPhone(String phone);
    
    /**
     * 查询所有客户
     * @return 客户列表
     */
    List<CustomerEntity> getAllCustomers();
    
    /**
     * 分页查询客户
     * @param pageable 分页参数
     * @return 分页结果
     */
    PageResult<CustomerEntity> getCustomersByPage(Pageable pageable);
    
    /**
     * 根据客户状态查询客户
     * @param status 客户状态
     * @return 客户列表
     */
    List<CustomerEntity> getCustomersByStatus(String status);
}
