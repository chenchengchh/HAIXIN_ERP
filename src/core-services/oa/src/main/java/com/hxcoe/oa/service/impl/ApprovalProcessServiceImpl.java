package com.hxcoe.oa.service.impl;

import com.hxcoe.oa.entity.ApprovalProcessEntity;
import com.hxcoe.oa.repository.ApprovalProcessRepository;
import com.hxcoe.oa.service.ApprovalProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审批流程定义ServiceImpl
 */
@Service
public class ApprovalProcessServiceImpl implements ApprovalProcessService {

    @Autowired
    private ApprovalProcessRepository approvalProcessRepository;

    /**
     * 创建审批流程
     */
    @Override
    public ApprovalProcessEntity createApprovalProcess(ApprovalProcessEntity process) {
        return approvalProcessRepository.save(process);
    }

    /**
     * 更新审批流程
     */
    @Override
    public ApprovalProcessEntity updateApprovalProcess(Long id, ApprovalProcessEntity process) {
        ApprovalProcessEntity existingProcess = approvalProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程不存在"));
        process.setId(id);
        return approvalProcessRepository.save(process);
    }

    /**
     * 删除审批流程
     */
    @Override
    public void deleteApprovalProcess(Long id) {
        ApprovalProcessEntity process = approvalProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程不存在"));
        approvalProcessRepository.delete(process);
    }

    /**
     * 根据ID获取审批流程
     */
    @Override
    public ApprovalProcessEntity getApprovalProcessById(Long id) {
        return approvalProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程不存在"));
    }

    /**
     * 根据编码获取审批流程
     */
    @Override
    public ApprovalProcessEntity getApprovalProcessByCode(String code) {
        ApprovalProcessEntity process = approvalProcessRepository.findByCode(code);
        if (process == null) {
            throw new RuntimeException("审批流程不存在");
        }
        return process;
    }

    /**
     * 获取审批流程列表
     */
    @Override
    public List<ApprovalProcessEntity> getApprovalProcessList() {
        return approvalProcessRepository.findAll();
    }

    /**
     * 分页获取审批流程列表
     */
    @Override
    public Page<ApprovalProcessEntity> getApprovalProcessPage(Pageable pageable) {
        return approvalProcessRepository.findAll(pageable);
    }

    /**
     * 根据状态获取审批流程列表
     */
    @Override
    public List<ApprovalProcessEntity> getApprovalProcessByStatus(Integer status) {
        return approvalProcessRepository.findByStatus(status);
    }

    /**
     * 根据类型获取审批流程列表
     */
    @Override
    public List<ApprovalProcessEntity> getApprovalProcessByType(String type) {
        return approvalProcessRepository.findByProcessType(type);
    }

    /**
     * 启用审批流程
     */
    @Override
    public ApprovalProcessEntity enableApprovalProcess(Long id) {
        ApprovalProcessEntity process = approvalProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程不存在"));
        process.setStatus(1);
        return approvalProcessRepository.save(process);
    }

    /**
     * 禁用审批流程
     */
    @Override
    public ApprovalProcessEntity disableApprovalProcess(Long id) {
        ApprovalProcessEntity process = approvalProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程不存在"));
        process.setStatus(0);
        return approvalProcessRepository.save(process);
    }
    
    /**
     * 复制审批流程
     */
    @Override
    public ApprovalProcessEntity copyApprovalProcess(Long id) {
        // 获取原审批流程
        ApprovalProcessEntity originalProcess = approvalProcessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批流程不存在"));
        
        // 创建新的审批流程
        ApprovalProcessEntity newProcess = new ApprovalProcessEntity();
        
        // 复制原流程的属性
        newProcess.setName(originalProcess.getName() + "(复制)");
        newProcess.setCode(originalProcess.getCode() + "_COPY");
        newProcess.setDescription(originalProcess.getDescription());
        newProcess.setStatus(0); // 设置为禁用状态
        newProcess.setProcessType(originalProcess.getProcessType());
        newProcess.setProcessDefinition(originalProcess.getProcessDefinition());
        newProcess.setFormConfig(originalProcess.getFormConfig());
        newProcess.setCreatorId(originalProcess.getCreatorId());
        
        // 保存新流程
        return approvalProcessRepository.save(newProcess);
    }
}