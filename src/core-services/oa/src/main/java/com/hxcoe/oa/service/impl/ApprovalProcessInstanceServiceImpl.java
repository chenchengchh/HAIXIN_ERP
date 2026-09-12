package com.hxcoe.oa.service.impl;

import com.hxcoe.oa.entity.ApprovalProcessInstanceEntity;
import com.hxcoe.oa.repository.ApprovalProcessInstanceRepository;
import com.hxcoe.oa.service.ApprovalProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审批流程实例ServiceImpl
 */
@Service
public class ApprovalProcessInstanceServiceImpl implements ApprovalProcessInstanceService {

    @Autowired
    private ApprovalProcessInstanceRepository approvalProcessInstanceRepository;

    /**
     * 创建审批流程实例
     */
    @Override
    public ApprovalProcessInstanceEntity createApprovalProcessInstance(ApprovalProcessInstanceEntity instance) {
        return approvalProcessInstanceRepository.save(instance);
    }

    /**
     * 更新审批流程实例
     */
    @Override
    public ApprovalProcessInstanceEntity updateApprovalProcessInstance(Long id, ApprovalProcessInstanceEntity instance) {
        ApprovalProcessInstanceEntity existingInstance = approvalProcessInstanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程实例不存在"));
        instance.setId(id);
        return approvalProcessInstanceRepository.save(instance);
    }

    /**
     * 删除审批流程实例
     */
    @Override
    public void deleteApprovalProcessInstance(Long id) {
        ApprovalProcessInstanceEntity instance = approvalProcessInstanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程实例不存在"));
        approvalProcessInstanceRepository.delete(instance);
    }

    /**
     * 根据ID获取审批流程实例
     */
    @Override
    public ApprovalProcessInstanceEntity getApprovalProcessInstanceById(Long id) {
        return approvalProcessInstanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程实例不存在"));
    }

    /**
     * 根据业务ID获取审批流程实例。
     * <p>businessId以JSON形式存储于processVariables中，取最新一条匹配记录。</p>
     */
    @Override
    public ApprovalProcessInstanceEntity getApprovalProcessInstanceByBusinessId(String businessId) {
        return approvalProcessInstanceRepository.findByBusinessIdInVariables(businessId)
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("审批流程实例不存在"));
    }

    /**
     * 获取审批流程实例列表
     */
    @Override
    public List<ApprovalProcessInstanceEntity> getApprovalProcessInstanceList() {
        return approvalProcessInstanceRepository.findAll();
    }

    /**
     * 分页获取审批流程实例列表
     */
    @Override
    public Page<ApprovalProcessInstanceEntity> getApprovalProcessInstancePage(Pageable pageable) {
        return approvalProcessInstanceRepository.findAll(pageable);
    }

    /**
     * 根据流程定义ID获取审批流程实例列表
     */
    @Override
    public List<ApprovalProcessInstanceEntity> getApprovalProcessInstanceByProcessId(Long processId) {
        return approvalProcessInstanceRepository.findByProcessId(processId);
    }

    /**
     * 根据状态获取审批流程实例列表
     */
    @Override
    public List<ApprovalProcessInstanceEntity> getApprovalProcessInstanceByStatus(String status) {
        return approvalProcessInstanceRepository.findByStatus(status);
    }

    /**
     * 根据申请人ID获取审批流程实例列表
     */
    @Override
    public List<ApprovalProcessInstanceEntity> getApprovalProcessInstanceByApplicantId(Long applicantId) {
        return approvalProcessInstanceRepository.findByInitiatorId(applicantId);
    }

    /**
     * 启动审批流程实例
     */
    @Override
    public ApprovalProcessInstanceEntity startApprovalProcessInstance(Long id) {
        ApprovalProcessInstanceEntity instance = approvalProcessInstanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程实例不存在"));
        instance.setStatus("RUNNING");
        return approvalProcessInstanceRepository.save(instance);
    }

    /**
     * 暂停审批流程实例
     */
    @Override
    public ApprovalProcessInstanceEntity suspendApprovalProcessInstance(Long id) {
        ApprovalProcessInstanceEntity instance = approvalProcessInstanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程实例不存在"));
        instance.setStatus("SUSPENDED");
        return approvalProcessInstanceRepository.save(instance);
    }

    /**
     * 终止审批流程实例
     */
    @Override
    public ApprovalProcessInstanceEntity terminateApprovalProcessInstance(Long id) {
        ApprovalProcessInstanceEntity instance = approvalProcessInstanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程实例不存在"));
        instance.setStatus("TERMINATED");
        return approvalProcessInstanceRepository.save(instance);
    }

    /**
     * 完成审批流程实例
     */
    @Override
    public ApprovalProcessInstanceEntity completeApprovalProcessInstance(Long id) {
        ApprovalProcessInstanceEntity instance = approvalProcessInstanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程实例不存在"));
        instance.setStatus("COMPLETED");
        return approvalProcessInstanceRepository.save(instance);
    }
}