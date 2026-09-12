package com.hxcoe.oa.service;

import com.hxcoe.oa.entity.ApprovalProcessInstanceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 审批流程实例Service
 */
public interface ApprovalProcessInstanceService {

    /**
     * 创建审批流程实例
     */
    ApprovalProcessInstanceEntity createApprovalProcessInstance(ApprovalProcessInstanceEntity instance);

    /**
     * 更新审批流程实例
     */
    ApprovalProcessInstanceEntity updateApprovalProcessInstance(Long id, ApprovalProcessInstanceEntity instance);

    /**
     * 删除审批流程实例
     */
    void deleteApprovalProcessInstance(Long id);

    /**
     * 根据ID获取审批流程实例
     */
    ApprovalProcessInstanceEntity getApprovalProcessInstanceById(Long id);

    /**
     * 根据业务ID获取审批流程实例
     */
    ApprovalProcessInstanceEntity getApprovalProcessInstanceByBusinessId(String businessId);

    /**
     * 获取审批流程实例列表
     */
    List<ApprovalProcessInstanceEntity> getApprovalProcessInstanceList();

    /**
     * 分页获取审批流程实例列表
     */
    Page<ApprovalProcessInstanceEntity> getApprovalProcessInstancePage(Pageable pageable);

    /**
     * 根据流程定义ID获取审批流程实例列表
     */
    List<ApprovalProcessInstanceEntity> getApprovalProcessInstanceByProcessId(Long processId);

    /**
     * 根据状态获取审批流程实例列表
     */
    List<ApprovalProcessInstanceEntity> getApprovalProcessInstanceByStatus(String status);

    /**
     * 根据申请人ID获取审批流程实例列表
     */
    List<ApprovalProcessInstanceEntity> getApprovalProcessInstanceByApplicantId(Long applicantId);

    /**
     * 启动审批流程实例
     */
    ApprovalProcessInstanceEntity startApprovalProcessInstance(Long id);

    /**
     * 暂停审批流程实例
     */
    ApprovalProcessInstanceEntity suspendApprovalProcessInstance(Long id);

    /**
     * 终止审批流程实例
     */
    ApprovalProcessInstanceEntity terminateApprovalProcessInstance(Long id);

    /**
     * 完成审批流程实例
     */
    ApprovalProcessInstanceEntity completeApprovalProcessInstance(Long id);
}