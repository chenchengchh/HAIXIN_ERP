package com.hxcoe.oa.service;

import com.hxcoe.oa.entity.ApprovalTaskEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 审批任务Service
 */
public interface ApprovalTaskService {

    /**
     * 创建审批任务
     */
    ApprovalTaskEntity createApprovalTask(ApprovalTaskEntity task);

    /**
     * 更新审批任务
     */
    ApprovalTaskEntity updateApprovalTask(Long id, ApprovalTaskEntity task);

    /**
     * 删除审批任务
     */
    void deleteApprovalTask(Long id);

    /**
     * 根据ID获取审批任务
     */
    ApprovalTaskEntity getApprovalTaskById(Long id);

    /**
     * 获取审批任务列表
     */
    List<ApprovalTaskEntity> getApprovalTaskList();

    /**
     * 分页获取审批任务列表
     */
    Page<ApprovalTaskEntity> getApprovalTaskPage(Pageable pageable);
    
    /**
     * 分页获取审批任务列表，支持多种查询条件
     */
    Page<ApprovalTaskEntity> getApprovalTaskPage(Pageable pageable, String processType, String status, 
                                               String initiator, String result, String assigneeId);

    /**
     * 根据流程实例ID获取审批任务列表
     */
    List<ApprovalTaskEntity> getApprovalTaskByInstanceId(Long instanceId);

    /**
     * 根据审批人ID获取审批任务列表
     */
    List<ApprovalTaskEntity> getApprovalTaskByApproverId(Long approverId);

    /**
     * 根据审批人ID和状态获取审批任务列表
     */
    List<ApprovalTaskEntity> getApprovalTaskByApproverIdAndStatus(Long approverId, String status);
    
    /**
     * 根据审批人ID和状态分页获取审批任务列表
     */
    Page<ApprovalTaskEntity> getApprovalTaskByAssigneeIdAndStatusPage(Long assigneeId, String status, Pageable pageable);
    
    /**
     * 根据实例ID分页获取审批任务列表
     */
    Page<ApprovalTaskEntity> getApprovalTaskByInstanceIdPage(Long instanceId, Pageable pageable);
    
    /**
     * 分页获取审批任务列表，支持多种查询条件（调整参数顺序以匹配控制器调用）
     */
    Page<ApprovalTaskEntity> getApprovalTaskPage(Pageable pageable, String status, Long assigneeId, String processType, String initiator, String result);

    /**
     * 根据状态获取审批任务列表
     */
    List<ApprovalTaskEntity> getApprovalTaskByStatus(String status);
    
    /**
     * 根据审批人ID获取已审批任务列表
     */
    List<ApprovalTaskEntity> getApprovedTaskByApproverId(Long approverId);
    
    /**
     * 根据审批人ID获取待审批任务列表
     */
    List<ApprovalTaskEntity> getPendingTaskByApproverId(Long approverId);

    /**
     * 审批任务通过
     */
    ApprovalTaskEntity approveTask(Long id, String comment);

    /**
     * 审批任务拒绝
     */
    ApprovalTaskEntity rejectTask(Long id, String comment);
    
    /**
     * 统一审批任务处理，支持通过和拒绝
     */
    ApprovalTaskEntity approve(Long id, String result, String comment);

    /**
     * 转发审批任务
     */
    ApprovalTaskEntity forwardTask(Long id, Long newApproverId, String comment);

    /**
     * 认领审批任务
     */
    ApprovalTaskEntity claimTask(Long id, Long approverId);

    /**
     * 取消认领审批任务
     */
    ApprovalTaskEntity unclaimTask(Long id);
}