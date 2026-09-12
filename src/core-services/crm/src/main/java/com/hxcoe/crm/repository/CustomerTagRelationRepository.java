package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.CustomerTagRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户标签关系Repository接口
 */
@Repository
public interface CustomerTagRelationRepository extends JpaRepository<CustomerTagRelationEntity, Long>, JpaSpecificationExecutor<CustomerTagRelationEntity> {

    /**
     * 根据客户ID查询标签关系
     *
     * @param customerId 客户ID
     * @return 标签关系列表
     */
    List<CustomerTagRelationEntity> findByCustomerId(Long customerId);

    /**
     * 根据标签ID查询标签关系
     *
     * @param tagId 标签ID
     * @return 标签关系列表
     */
    List<CustomerTagRelationEntity> findByTagId(Long tagId);

    /**
     * 根据客户ID和标签ID查询标签关系
     *
     * @param customerId 客户ID
     * @param tagId 标签ID
     * @return 标签关系
     */
    CustomerTagRelationEntity findByCustomerIdAndTagId(Long customerId, Long tagId);

    /**
     * 根据客户ID删除标签关系
     *
     * @param customerId 客户ID
     */
    void deleteByCustomerId(Long customerId);

    /**
     * 根据标签ID删除标签关系
     *
     * @param tagId 标签ID
     */
    void deleteByTagId(Long tagId);
}