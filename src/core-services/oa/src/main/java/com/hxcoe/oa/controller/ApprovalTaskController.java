package com.hxcoe.oa.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.ApprovalTaskEntity;
import com.hxcoe.oa.service.ApprovalTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批任务控制器
 */
@RestController
@RequestMapping("/api/v1/oa/approval/tasks")
@Tag(name = "审批任务管理", description = "审批任务相关API接口")
@PreAuthorize("hasAnyAuthority('oa:approval:task:read','oa:approval:task:write')")
public class ApprovalTaskController {

    @Autowired
    private ApprovalTaskService approvalTaskService;

    /**
     * 获取审批任务列表
     * @param page 页码
     * @param size 每页数量
     * @param status 状态
     * @param assigneeId 审批人ID
     * @param processType 流程类型
     * @param initiator 发起人
     * @param result 审批结果
     * @return 审批任务列表
     */
    @Operation(summary = "获取审批任务列表", description = "分页获取审批任务列表，支持多种查询条件")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    public Result<PageResult<ApprovalTaskEntity>> getApprovalTaskList(
            @Parameter(description = "页码，默认1", required = false) @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10", required = false) @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "状态", required = false) @RequestParam(required = false) String status,
            @Parameter(description = "审批人ID", required = false) @RequestParam(required = false) Long assigneeId,
            @Parameter(description = "流程类型", required = false) @RequestParam(required = false) String processType,
            @Parameter(description = "发起人", required = false) @RequestParam(required = false) String initiator,
            @Parameter(description = "审批结果", required = false) @RequestParam(required = false) String result) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size); // 转换为0-based页码
            Page<ApprovalTaskEntity> approvalTaskPage = approvalTaskService.getApprovalTaskPage(
                    pageable, status, assigneeId, processType, initiator, result);
            
            PageResult<ApprovalTaskEntity> pageResult = PageResult.build(
                    approvalTaskPage.getTotalElements(),
                    size,
                    page,
                    approvalTaskPage.getContent());
            
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("获取审批任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据实例ID获取审批任务列表
     * @param instanceId 实例ID
     * @param page 页码
     * @param size 每页数量
     * @return 审批任务列表
     */
    @Operation(summary = "根据实例ID获取审批任务列表", description = "根据审批流程实例ID分页获取审批任务列表")
    @GetMapping("/instance/{instanceId}")
    public Result<PageResult<ApprovalTaskEntity>> getApprovalTaskByInstanceId(
            @Parameter(description = "实例ID", required = true) @PathVariable Long instanceId,
            @Parameter(description = "页码，默认1", required = false) @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10", required = false) @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size); // 转换为0-based页码
            Page<ApprovalTaskEntity> approvalTaskPage = approvalTaskService.getApprovalTaskByInstanceIdPage(
                    instanceId, pageable);
            
            PageResult<ApprovalTaskEntity> pageResult = PageResult.build(
                    approvalTaskPage.getTotalElements(),
                    size,
                    page,
                    approvalTaskPage.getContent());
            
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("获取审批任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据审批人ID和状态获取审批任务列表
     * @param assigneeId 审批人ID
     * @param status 状态
     * @param page 页码
     * @param size 每页数量
     * @return 审批任务列表
     */
    @Operation(summary = "根据审批人ID和状态获取审批任务列表", description = "根据审批人ID和状态分页获取审批任务列表")
    @GetMapping("/assignee/{assigneeId}")
    public Result<PageResult<ApprovalTaskEntity>> getApprovalTaskByAssigneeIdAndStatus(
            @Parameter(description = "审批人ID", required = true) @PathVariable Long assigneeId,
            @Parameter(description = "状态", required = true) @RequestParam String status,
            @Parameter(description = "页码，默认1", required = false) @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10", required = false) @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size); // 转换为0-based页码
            Page<ApprovalTaskEntity> approvalTaskPage = approvalTaskService.getApprovalTaskByAssigneeIdAndStatusPage(
                    assigneeId, status, pageable);
            
            PageResult<ApprovalTaskEntity> pageResult = PageResult.build(
                    approvalTaskPage.getTotalElements(),
                    size,
                    page,
                    approvalTaskPage.getContent());
            
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("获取审批任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 审批任务
     * @param id 任务ID
     * @param result 审批结果
     * @param comment 审批意见
     * @return 审批结果
     */
    @Operation(summary = "审批任务", description = "审批指定ID的任务")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('oa:approval:task:write')")
    public Result<ApprovalTaskEntity> approveTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long id,
            @Parameter(description = "审批结果", required = true) @RequestParam String result,
            @Parameter(description = "审批意见", required = false) @RequestParam(required = false) String comment) {
        
        try {
            ApprovalTaskEntity task = approvalTaskService.approve(id, result, comment);
            
            return Result.success(task);
        } catch (Exception e) {
            return Result.error("审批任务失败: " + e.getMessage());
        }
    }

    /**
     * 取消审批任务
     * @param id 任务ID
     * @return 取消结果
     */
    @Operation(summary = "取消审批任务", description = "取消指定ID的审批任务")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('oa:approval:task:write')")
    public Result<ApprovalTaskEntity> cancelTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        
        try {
            // 使用现有的updateApprovalTask方法来取消审批任务
            ApprovalTaskEntity task = approvalTaskService.getApprovalTaskById(id);
            task.setStatus("cancelled");
            task = approvalTaskService.updateApprovalTask(id, task);
            
            return Result.success(task);
        } catch (Exception e) {
            return Result.error("取消审批任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取审批任务详情
     * @param id 任务ID
     * @return 审批任务详情
     */
    @Operation(summary = "根据ID获取审批任务详情", description = "获取指定ID的审批任务详情")
    @GetMapping("/{id}")
    public Result<ApprovalTaskEntity> getApprovalTaskById(
            @Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        
        try {
            ApprovalTaskEntity task = approvalTaskService.getApprovalTaskById(id);
            
            return Result.success(task);
        } catch (Exception e) {
            return Result.error("获取审批任务详情失败: " + e.getMessage());
        }
    }
}
