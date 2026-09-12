package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 商机数据访问接口
 */
@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long>, JpaSpecificationExecutor<Opportunity> {
    
    /**
     * 根据条件分页查询商机列表
     * @param opportunityName 商机名称
     * @param customerName 客户名称
     * @param stage 阶段
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Opportunity> findByOpportunityNameContainingAndCustomerNameContainingAndStageContainingAndStatusContaining(
            String opportunityName, String customerName, String stage, String status, Pageable pageable);
    
    /**
     * 查询我的商机列表
     * @param ownerId 负责人ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Opportunity> findByOwnerId(Long ownerId, Pageable pageable);
}
