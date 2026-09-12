package com.hxcoe.oa.service;

import com.hxcoe.oa.entity.ApprovalProcessEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 审批流程定义Service
 */
public interface ApprovalProcessService {

    /**
     * 创建审批流程
     */
    ApprovalProcessEntity createApprovalProcess(ApprovalProcessEntity process);

    /**
     * 更新审批流程
     */
    ApprovalProcessEntity updateApprovalProcess(Long id, ApprovalProcessEntity process);

    /**
     * 删除审批流程
     */
    void deleteApprovalProcess(Long id);

    /**
     * 根据ID获取审批流程
     */
    ApprovalProcessEntity getApprovalProcessById(Long id);

    /**
     * 根据编码获取审批流程
     */
    ApprovalProcessEntity getApprovalProcessByCode(String code);

    /**
     * 获取审批流程列表
     */
    List<ApprovalProcessEntity> getApprovalProcessList();

    /**
     * 分页获取审批流程列表
     */
    Page<ApprovalProcessEntity> getApprovalProcessPage(Pageable pageable);

    /**
     * 根据状态获取审批流程列表
     */
    List<ApprovalProcessEntity> getApprovalProcessByStatus(Integer status);

    /**
     * 根据类型获取审批流程列表
     */
    List<ApprovalProcessEntity> getApprovalProcessByType(String type);

    /**
     * 启用审批流程
     */
    ApprovalProcessEntity enableApprovalProcess(Long id);

    /**
     * 禁用审批流程
     */
    ApprovalProcessEntity disableApprovalProcess(Long id);
    
    /**
     * 复制审批流程
     */
    ApprovalProcessEntity copyApprovalProcess(Long id);
}