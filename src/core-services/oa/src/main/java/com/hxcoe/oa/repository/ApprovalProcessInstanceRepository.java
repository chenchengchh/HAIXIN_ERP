package com.hxcoe.oa.repository;

import com.hxcoe.oa.entity.ApprovalProcessInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批流程实例Repository
 */
@Repository
public interface ApprovalProcessInstanceRepository extends JpaRepository<ApprovalProcessInstanceEntity, Long> {

    /**
     * 根据发起人ID查询流程实例列表
     */
    List<ApprovalProcessInstanceEntity> findByInitiatorId(Long initiatorId);

    /**
     * 根据流程状态查询流程实例列表
     */
    List<ApprovalProcessInstanceEntity> findByStatus(String status);

    /**
     * 根据流程ID查询流程实例列表
     */
    List<ApprovalProcessInstanceEntity> findByProcessId(Long processId);

    /**
     * 根据流程编码查询流程实例列表
     */
    List<ApprovalProcessInstanceEntity> findByProcessCode(String processCode);

    /**
     * 按状态分组统计审批实例数量
     * @return 状态统计列表，每行[状态, 数量]
     */
    @Query("SELECT i.status, COUNT(i) FROM ApprovalProcessInstanceEntity i GROUP BY i.status")
    List<Object[]> countByStatus();

    /**
     * 根据业务ID查询流程实例（businessId以JSON形式存储于processVariables中）
     * @param businessId 业务单据ID
     * @return 匹配的流程实例列表，按创建时间倒序
     */
    @Query("SELECT i FROM ApprovalProcessInstanceEntity i WHERE i.processVariables LIKE %:businessId% ORDER BY i.createTime DESC")
    List<ApprovalProcessInstanceEntity> findByBusinessIdInVariables(String businessId);
}