package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户Repository接口
 */
@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long>, JpaSpecificationExecutor<CustomerEntity> {

    /**
     * 根据客户编号查询
     *
     * @param customerNo 客户编号
     * @return 客户实体
     */
    CustomerEntity findByCustomerNo(String customerNo);

    /**
     * 根据客户名称模糊查询
     *
     * @param customerName 客户名称
     * @return 客户实体列表
     */
    List<CustomerEntity> findByCustomerNameContaining(String customerName);

    /**
     * 根据客户类型查询
     *
     * @param customerType 客户类型
     * @return 客户实体列表
     */
    List<CustomerEntity> findByCustomerType(String customerType);

    /**
     * 根据客户状态查询
     *
     * @param status 客户状态
     * @return 客户实体列表
     */
    List<CustomerEntity> findByStatus(String status);

    /**
     * 根据客户级别查询
     *
     * @param level 客户级别
     * @return 客户实体列表
     */
    List<CustomerEntity> findByLevel(String level);

    /**
     * 根据创建时间范围查询
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 客户实体列表
     */
    List<CustomerEntity> findByCreateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
}
