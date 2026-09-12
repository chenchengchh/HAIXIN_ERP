package com.hxcoe.oa.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.ApprovalProcessEntity;
import com.hxcoe.oa.service.ApprovalProcessService;
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
 * 审批流程定义Controller
 */
@RestController
@RequestMapping("/api/v1/oa/approval/process")
@Tag(name = "审批流程定义管理", description = "审批流程定义相关API")
@PreAuthorize("hasAnyAuthority('oa:approval:process:read','oa:approval:process:write')")
public class ApprovalProcessController {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalProcessController.class);

    @Autowired
    private ApprovalProcessService approvalProcessService;

    /**
     * 创建审批流程
     */
    @PostMapping
    @Operation(summary = "创建审批流程", description = "创建新的审批流程定义")
    @PreAuthorize("hasAuthority('oa:approval:process:write')")
    public Result<ApprovalProcessEntity> createApprovalProcess(@RequestBody ApprovalProcessEntity process) {
        try {
            logger.debug("创建审批流程: {}", process);
            ApprovalProcessEntity createdProcess = approvalProcessService.createApprovalProcess(process);
            return Result.success(createdProcess);
        } catch (Exception e) {
            logger.error("创建审批流程失败: {}", e.getMessage(), e);
            return Result.error("创建审批流程失败: " + e.getMessage());
        }
    }

    /**
     * 更新审批流程
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新审批流程", description = "更新指定ID的审批流程定义")
    @PreAuthorize("hasAuthority('oa:approval:process:write')")
    public Result<ApprovalProcessEntity> updateApprovalProcess(@PathVariable Long id, @RequestBody ApprovalProcessEntity process) {
        try {
            logger.debug("更新审批流程: id={}, {}", id, process);
            ApprovalProcessEntity updatedProcess = approvalProcessService.updateApprovalProcess(id, process);
            return Result.success(updatedProcess);
        } catch (Exception e) {
            logger.error("更新审批流程失败: {}", e.getMessage(), e);
            return Result.error("更新审批流程失败: " + e.getMessage());
        }
    }

    /**
     * 删除审批流程
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除审批流程", description = "删除指定ID的审批流程定义")
    @PreAuthorize("hasAuthority('oa:approval:process:write')")
    public Result<Void> deleteApprovalProcess(@PathVariable Long id) {
        try {
            logger.debug("删除审批流程: id={}", id);
            approvalProcessService.deleteApprovalProcess(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除审批流程失败: {}", e.getMessage(), e);
            return Result.error("删除审批流程失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取审批流程
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取审批流程", description = "根据ID获取审批流程定义详情")
    public Result<ApprovalProcessEntity> getApprovalProcessById(@PathVariable Long id) {
        try {
            logger.debug("获取审批流程: id={}", id);
            ApprovalProcessEntity process = approvalProcessService.getApprovalProcessById(id);
            return Result.success(process);
        } catch (Exception e) {
            logger.error("获取审批流程失败: {}", e.getMessage(), e);
            return Result.error("获取审批流程失败: " + e.getMessage());
        }
    }

    /**
     * 根据编码获取审批流程
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码获取审批流程", description = "根据编码获取审批流程定义详情")
    public Result<ApprovalProcessEntity> getApprovalProcessByCode(@PathVariable String code) {
        try {
            logger.debug("根据编码获取审批流程: code={}", code);
            ApprovalProcessEntity process = approvalProcessService.getApprovalProcessByCode(code);
            return Result.success(process);
        } catch (Exception e) {
            logger.error("根据编码获取审批流程失败: {}", e.getMessage(), e);
            return Result.error("根据编码获取审批流程失败: " + e.getMessage());
        }
    }

    /**
     * 获取审批流程列表
     */
    @GetMapping
    @Operation(summary = "获取审批流程列表", description = "获取所有审批流程定义列表")
    public Result<List<ApprovalProcessEntity>> getApprovalProcessList() {
        try {
            logger.debug("获取审批流程列表");
            List<ApprovalProcessEntity> processes = approvalProcessService.getApprovalProcessList();
            return Result.success(processes);
        } catch (Exception e) {
            logger.error("获取审批流程列表失败: {}", e.getMessage(), e);
            return Result.error("获取审批流程列表失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取审批流程列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取审批流程列表", description = "分页获取审批流程定义列表")
    public Result<PageResult<ApprovalProcessEntity>> getApprovalProcessPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.debug("分页获取审批流程列表: page={}, size={}", page, size);
            Pageable pageable = PageRequest.of(page - 1, size); // 转换为0-based页码
            Page<ApprovalProcessEntity> processPage = approvalProcessService.getApprovalProcessPage(pageable);
            
            PageResult<ApprovalProcessEntity> pageResult = PageResult.build(
                    processPage.getTotalElements(),
                    size,
                    page,
                    processPage.getContent());
            
            return Result.success(pageResult);
        } catch (Exception e) {
            logger.error("分页获取审批流程列表失败: {}", e.getMessage(), e);
            return Result.error("分页获取审批流程列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态获取审批流程列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取审批流程列表", description = "根据状态获取审批流程定义列表")
    public Result<List<ApprovalProcessEntity>> getApprovalProcessByStatus(@PathVariable Integer status) {
        try {
            logger.debug("根据状态获取审批流程列表: status={}", status);
            List<ApprovalProcessEntity> processes = approvalProcessService.getApprovalProcessByStatus(status);
            return Result.success(processes);
        } catch (Exception e) {
            logger.error("根据状态获取审批流程列表失败: {}", e.getMessage(), e);
            return Result.error("根据状态获取审批流程列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取审批流程列表
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型获取审批流程列表", description = "根据类型获取审批流程定义列表")
    public Result<List<ApprovalProcessEntity>> getApprovalProcessByType(@PathVariable String type) {
        try {
            logger.debug("根据类型获取审批流程列表: type={}", type);
            List<ApprovalProcessEntity> processes = approvalProcessService.getApprovalProcessByType(type);
            return Result.success(processes);
        } catch (Exception e) {
            logger.error("根据类型获取审批流程列表失败: {}", e.getMessage(), e);
            return Result.error("根据类型获取审批流程列表失败: " + e.getMessage());
        }
    }

    /**
     * 启用审批流程
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用审批流程", description = "启用指定ID的审批流程定义")
    public Result<ApprovalProcessEntity> enableApprovalProcess(@PathVariable Long id) {
        try {
            logger.debug("启用审批流程: id={}", id);
            ApprovalProcessEntity process = approvalProcessService.enableApprovalProcess(id);
            return Result.success(process);
        } catch (Exception e) {
            logger.error("启用审批流程失败: {}", e.getMessage(), e);
            return Result.error("启用审批流程失败: " + e.getMessage());
        }
    }

    /**
     * 禁用审批流程
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用审批流程", description = "禁用指定ID的审批流程定义")
    public Result<ApprovalProcessEntity> disableApprovalProcess(@PathVariable Long id) {
        try {
            logger.debug("禁用审批流程: id={}", id);
            ApprovalProcessEntity process = approvalProcessService.disableApprovalProcess(id);
            return Result.success(process);
        } catch (Exception e) {
            logger.error("禁用审批流程失败: {}", e.getMessage(), e);
            return Result.error("禁用审批流程失败: " + e.getMessage());
        }
    }
    
    /**
     * 复制审批流程
     */
    @PostMapping("/{id}/copy")
    @Operation(summary = "复制审批流程", description = "复制指定ID的审批流程定义")
    public Result<ApprovalProcessEntity> copyApprovalProcess(@PathVariable Long id) {
        try {
            logger.debug("复制审批流程: id={}", id);
            ApprovalProcessEntity process = approvalProcessService.copyApprovalProcess(id);
            return Result.success(process);
        } catch (Exception e) {
            logger.error("复制审批流程失败: {}", e.getMessage(), e);
            return Result.error("复制审批流程失败: " + e.getMessage());
        }
    }
}
