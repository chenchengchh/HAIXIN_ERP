package com.hxcoe.oa.repository;

import com.hxcoe.oa.entity.ApprovalProcessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 审批流程定义Repository
 */
@Repository
public interface ApprovalProcessRepository extends JpaRepository<ApprovalProcessEntity, Long> {

    /**
     * 根据流程编码查询流程定义
     */
    ApprovalProcessEntity findByCode(String code);

    /**
     * 根据流程状态查询流程定义列表
     */
    List<ApprovalProcessEntity> findByStatus(Integer status);

    /**
     * 根据流程类型查询流程定义列表
     */
    List<ApprovalProcessEntity> findByProcessType(String processType);

    /**
     * 根据流程名称模糊查询
     */
    List<ApprovalProcessEntity> findByNameContaining(String name);
}