package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 销售线索数据访问接口
 */
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {
    
    /**
     * 根据条件分页查询线索列表
     * @param leadName 线索名称
     * @param status 状态
     * @param rating 评级
     * @param ownerName 负责人姓名
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Lead> findByLeadNameContainingAndStatusContainingAndRatingContainingAndOwnerNameContaining(
            String leadName, String status, String rating, String ownerName, Pageable pageable);
}
