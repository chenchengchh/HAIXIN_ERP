package com.hxcoe.oa.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.ApprovalProcessInstanceEntity;
import com.hxcoe.oa.service.ApprovalProcessInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批流程实例Controller
 */
@RestController
@RequestMapping("/api/v1/oa/approval/instance")
@Tag(name = "审批流程实例管理", description = "审批流程实例相关API")
@PreAuthorize("hasAnyAuthority('oa:approval:instance:read','oa:approval:instance:write')")
public class ApprovalProcessInstanceController {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalProcessInstanceController.class);

    @Autowired
    private ApprovalProcessInstanceService approvalProcessInstanceService;

    /**
     * 创建审批流程实例
     */
    @PostMapping
    @Operation(summary = "创建审批流程实例", description = "创建新的审批流程实例")
    @PreAuthorize("hasAuthority('oa:approval:instance:write')")
    public Result<ApprovalProcessInstanceEntity> createApprovalProcessInstance(@RequestBody ApprovalProcessInstanceEntity instance) {
        try {
            logger.debug("创建审批流程实例: {}", instance);
            ApprovalProcessInstanceEntity createdInstance = approvalProcessInstanceService.createApprovalProcessInstance(instance);
            return Result.success(createdInstance);
        } catch (Exception e) {
            logger.error("创建审批流程实例失败: {}", e.getMessage(), e);
            return Result.error("创建审批流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 更新审批流程实例
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新审批流程实例", description = "更新指定ID的审批流程实例")
    @PreAuthorize("hasAuthority('oa:approval:instance:write')")
    public Result<ApprovalProcessInstanceEntity> updateApprovalProcessInstance(@PathVariable Long id, @RequestBody ApprovalProcessInstanceEntity instance) {
        try {
            logger.debug("更新审批流程实例: id={}, {}", id, instance);
            ApprovalProcessInstanceEntity updatedInstance = approvalProcessInstanceService.updateApprovalProcessInstance(id, instance);
            return Result.success(updatedInstance);
        } catch (Exception e) {
            logger.error("更新审批流程实例失败: {}", e.getMessage(), e);
            return Result.error("更新审批流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 删除审批流程实例
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除审批流程实例", description = "删除指定ID的审批流程实例")
    @PreAuthorize("hasAuthority('oa:approval:instance:write')")
    public Result<Void> deleteApprovalProcessInstance(@PathVariable Long id) {
        try {
            logger.debug("删除审批流程实例: id={}", id);
            approvalProcessInstanceService.deleteApprovalProcessInstance(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除审批流程实例失败: {}", e.getMessage(), e);
            return Result.error("删除审批流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取审批流程实例
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取审批流程实例", description = "根据ID获取审批流程实例详情")
    public Result<ApprovalProcessInstanceEntity> getApprovalProcessInstanceById(@PathVariable Long id) {
        try {
            logger.debug("获取审批流程实例: id={}", id);
            ApprovalProcessInstanceEntity instance = approvalProcessInstanceService.getApprovalProcessInstanceById(id);
            return Result.success(instance);
        } catch (Exception e) {
            logger.error("获取审批流程实例失败: {}", e.getMessage(), e);
            return Result.error("获取审批流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 根据业务ID获取审批流程实例
     */
    @GetMapping("/business/{businessId}")
    @Operation(summary = "根据业务ID获取审批流程实例", description = "根据业务ID获取审批流程实例详情")
    public Result<ApprovalProcessInstanceEntity> getApprovalProcessInstanceByBusinessId(@PathVariable String businessId) {
        try {
            logger.debug("根据业务ID获取审批流程实例: businessId={}", businessId);
            ApprovalProcessInstanceEntity instance = approvalProcessInstanceService.getApprovalProcessInstanceByBusinessId(businessId);
            return Result.success(instance);
        } catch (Exception e) {
            logger.error("根据业务ID获取审批流程实例失败: {}", e.getMessage(), e);
            return Result.error("根据业务ID获取审批流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 获取审批流程实例列表
     */
    @GetMapping
    @Operation(summary = "获取审批流程实例列表", description = "获取所有审批流程实例列表")
    public Result<List<ApprovalProcessInstanceEntity>> getApprovalProcessInstanceList() {
        try {
            logger.debug("获取审批流程实例列表");
            List<ApprovalProcessInstanceEntity> instances = approvalProcessInstanceService.getApprovalProcessInstanceList();
            return Result.success(instances);
        } catch (Exception e) {
            logger.error("获取审批流程实例列表失败: {}", e.getMessage(), e);
            return Result.error("获取审批流程实例列表失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取审批流程实例列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取审批流程实例列表", description = "分页获取审批流程实例列表")
    public Result<PageResult<ApprovalProcessInstanceEntity>> getApprovalProcessInstancePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.debug("分页获取审批流程实例列表: page={}, size={}", page, size);
            Pageable pageable = PageRequest.of(page - 1, size); // 转换为0-based页码
            Page<ApprovalProcessInstanceEntity> instancePage = approvalProcessInstanceService.getApprovalProcessInstancePage(pageable);
            
            PageResult<ApprovalProcessInstanceEntity> pageResult = PageResult.build(
                    instancePage.getTotalElements(),
                    size,
                    page,
                    instancePage.getContent());
            
            return Result.success(pageResult);
        } catch (Exception e) {
            logger.error("分页获取审批流程实例列表失败: {}", e.getMessage(), e);
            return Result.error("分页获取审批流程实例列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据流程定义ID获取审批流程实例列表
     */
    @GetMapping("/process/{processId}")
    @Operation(summary = "根据流程定义ID获取审批流程实例列表", description = "根据流程定义ID获取审批流程实例列表")
    public Result<List<ApprovalProcessInstanceEntity>> getApprovalProcessInstanceByProcessId(@PathVariable Long processId) {
        try {
            logger.debug("根据流程定义ID获取审批流程实例列表: processId={}", processId);
            List<ApprovalProcessInstanceEntity> instances = approvalProcessInstanceService.getApprovalProcessInstanceByProcessId(processId);
            return Result.success(instances);
        } catch (Exception e) {
            logger.error("根据流程定义ID获取审批流程实例列表失败: {}", e.getMessage(), e);
            return Result.error("根据流程定义ID获取审批流程实例列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态获取审批流程实例列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取审批流程实例列表", description = "根据状态获取审批流程实例列表")
    public Result<List<ApprovalProcessInstanceEntity>> getApprovalProcessInstanceByStatus(@PathVariable String status) {
        try {
            logger.debug("根据状态获取审批流程实例列表: status={}", status);
            List<ApprovalProcessInstanceEntity> instances = approvalProcessInstanceService.getApprovalProcessInstanceByStatus(status);
            return Result.success(instances);
        } catch (Exception e) {
            logger.error("根据状态获取审批流程实例列表失败: {}", e.getMessage(), e);
            return Result.error("根据状态获取审批流程实例列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据申请人ID获取审批流程实例列表
     */
    @GetMapping("/applicant/{applicantId}")
    @Operation(summary = "根据申请人ID获取审批流程实例列表", description = "根据申请人ID获取审批流程实例列表")
    public Result<List<ApprovalProcessInstanceEntity>> getApprovalProcessInstanceByApplicantId(@PathVariable Long applicantId) {
        try {
            logger.debug("根据申请人ID获取审批流程实例列表: applicantId={}", applicantId);
            List<ApprovalProcessInstanceEntity> instances = approvalProcessInstanceService.getApprovalProcessInstanceByApplicantId(applicantId);
            return Result.success(instances);
        } catch (Exception e) {
            logger.error("根据申请人ID获取审批流程实例列表失败: {}", e.getMessage(), e);
            return Result.error("根据申请人ID获取审批流程实例列表失败: " + e.getMessage());
        }
    }

    /**
     * 启动审批流程实例
     */
    @PutMapping("/{id}/start")
    @Operation(summary = "启动审批流程实例", description = "启动指定ID的审批流程实例")
    @PreAuthorize("hasAuthority('oa:approval:instance:write')")
    public Result<ApprovalProcessInstanceEntity> startApprovalProcessInstance(@PathVariable Long id) {
        try {
            logger.debug("启动审批流程实例: id={}", id);
            ApprovalProcessInstanceEntity instance = approvalProcessInstanceService.startApprovalProcessInstance(id);
            return Result.success(instance);
        } catch (Exception e) {
            logger.error("启动审批流程实例失败: {}", e.getMessage(), e);
            return Result.error("启动审批流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 暂停审批流程实例
     */
    @PutMapping("/{id}/suspend")
    @Operation(summary = "暂停审批流程实例", description = "暂停指定ID的审批流程实例")
    @PreAuthorize("hasAuthority('oa:approval:instance:write')")
    public Result<ApprovalProcessInstanceEntity> suspendApprovalProcessInstance(@PathVariable Long id) {
        try {
            logger.debug("暂停审批流程实例: id={}", id);
            ApprovalProcessInstanceEntity instance = approvalProcessInstanceService.suspendApprovalProcessInstance(id);
            return Result.success(instance);
        } catch (Exception e) {
            logger.error("暂停审批流程实例失败: {}", e.getMessage(), e);
            return Result.error("暂停审批流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 终止审批流程实例
     */
    @PutMapping("/{id}/terminate")
    @Operation(summary = "终止审批流程实例", description = "终止指定ID的审批流程实例")
    @PreAuthorize("hasAuthority('oa:approval:instance:write')")
    public Result<ApprovalProcessInstanceEntity> terminateApprovalProcessInstance(@PathVariable Long id) {
        try {
            logger.debug("终止审批流程实例: id={}", id);
            ApprovalProcessInstanceEntity instance = approvalProcessInstanceService.terminateApprovalProcessInstance(id);
            return Result.success(instance);
        } catch (Exception e) {
            logger.error("终止审批流程实例失败: {}", e.getMessage(), e);
            return Result.error("终止审批流程实例失败: " + e.getMessage());
        }
    }

    /**
     * 完成审批流程实例
     */
    @PutMapping("/{id}/complete")
    @Operation(summary = "完成审批流程实例", description = "完成指定ID的审批流程实例")
    @PreAuthorize("hasAuthority('oa:approval:instance:write')")
    public Result<ApprovalProcessInstanceEntity> completeApprovalProcessInstance(@PathVariable Long id) {
        try {
            logger.debug("完成审批流程实例: id={}", id);
            ApprovalProcessInstanceEntity instance = approvalProcessInstanceService.completeApprovalProcessInstance(id);
            return Result.success(instance);
        } catch (Exception e) {
            logger.error("完成审批流程实例失败: {}", e.getMessage(), e);
            return Result.error("完成审批流程实例失败: " + e.getMessage());
        }
    }
}
