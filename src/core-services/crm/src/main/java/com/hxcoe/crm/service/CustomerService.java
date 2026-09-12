package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.CustomerEntity;
import com.hxcoe.common.result.PageResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户服务接口
 */
public interface CustomerService {

    /**
     * 创建客户
     *
     * @param customerEntity 客户实体
     * @return 保存后的客户实体
     */
    CustomerEntity createCustomer(CustomerEntity customerEntity);

    /**
     * 根据ID查询客户
     *
     * @param id 主键ID
     * @return 客户实体
     */
    CustomerEntity getCustomerById(Long id);

    /**
     * 根据客户编号查询客户
     *
     * @param customerNo 客户编号
     * @return 客户实体
     */
    CustomerEntity getCustomerByNo(String customerNo);

    /**
     * 更新客户
     *
     * @param customerEntity 客户实体
     * @return 更新后的客户实体
     */
    CustomerEntity updateCustomer(CustomerEntity customerEntity);

    /**
     * 删除客户
     *
     * @param id 主键ID
     * @return 删除结果
     */
    boolean deleteCustomer(Long id);

    /**
     * 分页查询客户列表
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param customerNo 客户编号
     * @param customerName 客户名称
     * @param customerType 客户类型
     * @param industry 行业
     * @param scale 规模
     * @param level 级别
     * @param status 状态
     * @param source 来源
     * @param ownerName 负责人姓名
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    PageResult<CustomerEntity> getCustomerList(Integer page, Integer size, String customerNo, String customerName, String customerType, String industry, String scale, String level, String status, String source, String ownerName, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 根据客户名称模糊查询
     *
     * @param customerName 客户名称
     * @return 客户实体列表
     */
    List<CustomerEntity> searchCustomerByName(String customerName);

    /**
     * 更新客户状态
     *
     * @param id 客户ID
     * @param status 客户状态
     * @return 更新结果
     */
    boolean updateCustomerStatus(Long id, String status);

    /**
     * 更新客户级别
     *
     * @param id 客户ID
     * @param level 客户级别
     * @return 更新结果
     */
    boolean updateCustomerLevel(Long id, String level);
}
