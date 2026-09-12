package com.hxcoe.oa.service.impl;

import com.hxcoe.oa.entity.ApprovalTaskEntity;
import com.hxcoe.oa.repository.ApprovalTaskRepository;
import com.hxcoe.oa.service.ApprovalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * 审批任务ServiceImpl
 */
@Service
public class ApprovalTaskServiceImpl implements ApprovalTaskService {

    @Autowired
    private ApprovalTaskRepository approvalTaskRepository;

    /**
     * 创建审批任务
     */
    @Override
    public ApprovalTaskEntity createApprovalTask(ApprovalTaskEntity task) {
        return approvalTaskRepository.save(task);
    }

    /**
     * 更新审批任务
     */
    @Override
    public ApprovalTaskEntity updateApprovalTask(Long id, ApprovalTaskEntity task) {
        ApprovalTaskEntity existingTask = approvalTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批任务不存在"));
        task.setId(id);
        return approvalTaskRepository.save(task);
    }

    /**
     * 删除审批任务
     */
    @Override
    public void deleteApprovalTask(Long id) {
        ApprovalTaskEntity task = approvalTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批任务不存在"));
        approvalTaskRepository.delete(task);
    }

    /**
     * 根据ID获取审批任务
     */
    @Override
    public ApprovalTaskEntity getApprovalTaskById(Long id) {
        return approvalTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批任务不存在"));
    }

    /**
     * 获取审批任务列表
     */
    @Override
    public List<ApprovalTaskEntity> getApprovalTaskList() {
        return approvalTaskRepository.findAll();
    }

    /**
     * 分页获取审批任务列表
     */
    @Override
    public Page<ApprovalTaskEntity> getApprovalTaskPage(Pageable pageable) {
        return approvalTaskRepository.findAll(pageable);
    }
    
    /**
     * 分页获取审批任务列表，支持多种查询条件
     */
    @Override
    public Page<ApprovalTaskEntity> getApprovalTaskPage(Pageable pageable, String processType, String status, 
                                                    String initiator, String result, String assigneeId) {
        // 构建动态查询条件
        return approvalTaskRepository.findAll((root, query, criteriaBuilder) -> {
            // 创建查询条件列表
            List<Predicate> predicates = new ArrayList<>();
            
            // 过滤状态
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            // 过滤审批结果
            if (result != null && !result.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("result"), result));
            }
            
            // 过滤审批人
            if (assigneeId != null && !assigneeId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("assigneeId"), Long.parseLong(assigneeId)));
            }
            
            // 组合所有查询条件
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, pageable);
    }

    /**
     * 根据流程实例ID获取审批任务列表
     */
    @Override
    public List<ApprovalTaskEntity> getApprovalTaskByInstanceId(Long instanceId) {
        return approvalTaskRepository.findByInstanceId(instanceId);
    }

    /**
     * 根据审批人ID获取审批任务列表
     */
    @Override
    public List<ApprovalTaskEntity> getApprovalTaskByApproverId(Long approverId) {
        return approvalTaskRepository.findByAssigneeId(approverId);
    }

    /**
     * 根据审批人ID和状态获取审批任务列表
     */
    @Override
    public List<ApprovalTaskEntity> getApprovalTaskByApproverIdAndStatus(Long approverId, String status) {
        return approvalTaskRepository.findByAssigneeIdAndStatus(approverId, status);
    }
    
    /**
     * 根据审批人ID和状态分页获取审批任务列表
     */
    @Override
    public Page<ApprovalTaskEntity> getApprovalTaskByAssigneeIdAndStatusPage(Long assigneeId, String status, Pageable pageable) {
        return approvalTaskRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 添加审批人ID和状态条件
            predicates.add(criteriaBuilder.equal(root.get("assigneeId"), assigneeId));
            predicates.add(criteriaBuilder.equal(root.get("status"), status));
            
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, pageable);
    }
    
    /**
     * 根据实例ID分页获取审批任务列表
     */
    @Override
    public Page<ApprovalTaskEntity> getApprovalTaskByInstanceIdPage(Long instanceId, Pageable pageable) {
        return approvalTaskRepository.findAll((root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("instanceId"), instanceId);
        }, pageable);
    }
    
    /**
     * 分页获取审批任务列表，支持多种查询条件（调整参数顺序以匹配控制器调用）
     */
    @Override
    public Page<ApprovalTaskEntity> getApprovalTaskPage(Pageable pageable, String status, Long assigneeId, 
                                                    String processType, String initiator, String result) {
        // 构建动态查询条件
        return approvalTaskRepository.findAll((root, query, criteriaBuilder) -> {
            // 创建查询条件列表
            List<Predicate> predicates = new ArrayList<>();
            
            // 过滤状态
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            // 过滤审批结果
            if (result != null && !result.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("result"), result));
            }
            
            // 过滤审批人
            if (assigneeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assigneeId"), assigneeId));
            }
            
            // 组合所有查询条件
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        }, pageable);
    }

    /**
     * 根据状态获取审批任务列表
     */
    @Override
    public List<ApprovalTaskEntity> getApprovalTaskByStatus(String status) {
        return approvalTaskRepository.findAll((root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("status"), status);
        });
    }
    
    /**
     * 根据审批人ID获取已审批任务列表
     */
    @Override
    public List<ApprovalTaskEntity> getApprovedTaskByApproverId(Long approverId) {
        // 使用Specification一次查询获取所有已通过和已拒绝的任务，减少数据库查询次数
        return approvalTaskRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 添加审批人ID条件
            predicates.add(criteriaBuilder.equal(root.get("assigneeId"), approverId));
            
            // 添加状态条件：已通过或已拒绝
            predicates.add(criteriaBuilder.or(
                criteriaBuilder.equal(root.get("status"), "approved"),
                criteriaBuilder.equal(root.get("status"), "rejected")
            ));
            
            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        });
    }
    
    /**
     * 根据审批人ID获取待审批任务列表
     */
    @Override
    public List<ApprovalTaskEntity> getPendingTaskByApproverId(Long approverId) {
        // 获取审批人所有待审批的任务
        return approvalTaskRepository.findByAssigneeIdAndStatus(approverId, "pending");
    }

    /**
     * 审批任务通过
     */
    @Override
    public ApprovalTaskEntity approveTask(Long id, String comment) {
        ApprovalTaskEntity task = approvalTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批任务不存在"));
        task.setStatus("approved");
        task.setComment(comment);
        return approvalTaskRepository.save(task);
    }

    /**
     * 审批任务拒绝
     */
    @Override
    public ApprovalTaskEntity rejectTask(Long id, String comment) {
        ApprovalTaskEntity task = approvalTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批任务不存在"));
        task.setStatus("rejected");
        task.setComment(comment);
        task.setResult("rejected");
        return approvalTaskRepository.save(task);
    }
    
    /**
     * 统一审批任务处理，支持通过和拒绝
     */
    @Override
    public ApprovalTaskEntity approve(Long id, String result, String comment) {
        ApprovalTaskEntity task = approvalTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批任务不存在"));
        
        // 更新任务状态
        task.setStatus(result);
        task.setResult(result);
        task.setComment(comment);
        task.setApproveTime(java.time.LocalDateTime.now());
        
        // 保存任务
        ApprovalTaskEntity savedTask = approvalTaskRepository.save(task);
        
        // 尝试更新流程实例状态
        try {
            // 查找同实例下的其他待处理任务
            List<ApprovalTaskEntity> remainingTasks = approvalTaskRepository.findAll((root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("instanceId"), task.getInstanceId()));
                predicates.add(criteriaBuilder.equal(root.get("status"), "pending"));
                return query.where(predicates.toArray(new Predicate[0])).getRestriction();
            });
            
            // 如果没有剩余待处理任务，更新流程实例状态
            if (remainingTasks.isEmpty()) {
                // 这里应该注入ApprovalProcessInstanceService并更新流程实例状态
                // 为了避免循环依赖，暂时注释掉
                /*
                ApprovalProcessInstanceEntity instance = approvalProcessInstanceService.getApprovalProcessInstanceById(task.getInstanceId());
                instance.setStatus("completed");
                instance.setEndTime(java.time.LocalDateTime.now());
                approvalProcessInstanceService.updateApprovalProcessInstance(task.getInstanceId(), instance);
                */
            }
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("更新流程实例状态失败: " + e.getMessage());
        }
        
        return savedTask;
    }

    /**
     * 转发审批任务
     */
    @Override
    public ApprovalTaskEntity forwardTask(Long id, Long newApproverId, String comment) {
        ApprovalTaskEntity task = approvalTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批任务不存在"));
        task.setAssigneeId(newApproverId);
        task.setComment(comment);
        return approvalTaskRepository.save(task);
    }

    /**
     * 认领审批任务
     */
    @Override
    public ApprovalTaskEntity claimTask(Long id, Long approverId) {
        ApprovalTaskEntity task = approvalTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批任务不存在"));
        task.setAssigneeId(approverId);
        task.setStatus("pending");
        return approvalTaskRepository.save(task);
    }

    /**
     * 取消认领审批任务
     */
    @Override
    public ApprovalTaskEntity unclaimTask(Long id) {
        ApprovalTaskEntity task = approvalTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("审批任务不存在"));
        task.setAssigneeId(null);
        task.setStatus("pending");
        return approvalTaskRepository.save(task);
    }
}