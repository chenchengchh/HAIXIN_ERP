package com.hxcoe.oa.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.ApprovalProcessInstanceEntity;
import com.hxcoe.oa.entity.ApprovalTaskEntity;
import com.hxcoe.oa.service.ApprovalProcessInstanceService;
import com.hxcoe.oa.service.ApprovalTaskService;
import com.hxcoe.oa.service.UnifiedApprovalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统一审批入口控制器。
 * <p>各业务模块（CRM/SCM/ERP/WMS/MES等）通过此控制器统一提交审批申请，
 * OA审批完成后通过回调机制通知发起方模块，实现OA贯穿全系统的审批闭环。</p>
 */
@RestController
@RequestMapping("/api/v1/oa/unified-approval")
@Tag(name = "统一审批管理", description = "跨模块统一审批入口，支持各业务模块提交审批申请")
public class UnifiedApprovalController {

    private static final Logger logger = LoggerFactory.getLogger(UnifiedApprovalController.class);

    @Autowired
    private UnifiedApprovalService unifiedApprovalService;

    @Autowired
    private ApprovalProcessInstanceService approvalProcessInstanceService;

    @Autowired
    private ApprovalTaskService approvalTaskService;

    /**
     * 提交审批申请（统一入口）。
     * <p>各模块通过此接口提交审批，OA自动匹配流程模板并创建审批实例。</p>
     *
     * @param request 审批申请请求
     * @return 审批实例
     */
    @PostMapping("/submit")
    @Operation(summary = "提交审批申请", description = "各业务模块统一提交审批申请，自动匹配流程模板")
    public Result<ApprovalProcessInstanceEntity> submitApproval(@RequestBody UnifiedApprovalRequest request) {
        try {
            logger.info("收到审批申请: sourceSystem={}, businessType={}, businessId={}",
                    request.getSourceSystem(), request.getBusinessType(), request.getBusinessId());
            ApprovalProcessInstanceEntity instance = unifiedApprovalService.submitApproval(request);
            return Result.success("审批申请提交成功", instance);
        } catch (Exception e) {
            logger.error("提交审批申请失败: {}", e.getMessage(), e);
            return Result.error("提交审批申请失败: " + e.getMessage());
        }
    }

    /**
     * 审批通过/拒绝（统一审批操作）。
     *
     * @param taskId  审批任务ID
     * @param request 审批操作请求
     * @return 审批任务
     */
    @PutMapping("/tasks/{taskId}/process")
    @Operation(summary = "审批操作", description = "审批通过或拒绝指定任务")
    public Result<ApprovalTaskEntity> processApproval(
            @PathVariable Long taskId,
            @RequestBody ApprovalActionRequest request) {
        try {
            logger.info("审批操作: taskId={}, action={}, comment={}", taskId, request.getAction(), request.getComment());
            ApprovalTaskEntity task = unifiedApprovalService.processApproval(taskId, request);
            return Result.success("审批操作成功", task);
        } catch (Exception e) {
            logger.error("审批操作失败: {}", e.getMessage(), e);
            return Result.error("审批操作失败: " + e.getMessage());
        }
    }

    /**
     * 查询待办审批列表（按审批人）。
     *
     * @param assigneeId 审批人ID
     * @param page       页码
     * @param size       每页数量
     * @return 待办审批列表
     */
    @GetMapping("/todo/{assigneeId}")
    @Operation(summary = "查询待办审批", description = "按审批人ID查询待办审批任务")
    public Result<PageResult<ApprovalTaskEntity>> getTodoList(
            @PathVariable Long assigneeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<ApprovalTaskEntity> taskPage = approvalTaskService.getApprovalTaskByAssigneeIdAndStatusPage(
                    assigneeId, "pending", pageable);
            PageResult<ApprovalTaskEntity> pageResult = PageResult.build(
                    taskPage.getTotalElements(), size, page, taskPage.getContent());
            return Result.success(pageResult);
        } catch (Exception e) {
            logger.error("查询待办审批失败: {}", e.getMessage(), e);
            return Result.error("查询待办审批失败: " + e.getMessage());
        }
    }

    /**
     * 查询已办审批列表（按审批人）。
     *
     * @param assigneeId 审批人ID
     * @param page       页码
     * @param size       每页数量
     * @return 已办审批列表
     */
    @GetMapping("/done/{assigneeId}")
    @Operation(summary = "查询已办审批", description = "按审批人ID查询已办审批任务")
    public Result<PageResult<ApprovalTaskEntity>> getDoneList(
            @PathVariable Long assigneeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<ApprovalTaskEntity> taskPage = approvalTaskService.getApprovalTaskByAssigneeIdAndStatusPage(
                    assigneeId, "completed", pageable);
            PageResult<ApprovalTaskEntity> pageResult = PageResult.build(
                    taskPage.getTotalElements(), size, page, taskPage.getContent());
            return Result.success(pageResult);
        } catch (Exception e) {
            logger.error("查询已办审批失败: {}", e.getMessage(), e);
            return Result.error("查询已办审批失败: " + e.getMessage());
        }
    }

    /**
     * 查询我发起的审批列表。
     *
     * @param initiatorId 发起人ID
     * @param page        页码
     * @param size        每页数量
     * @return 审批实例列表
     */
    @GetMapping("/initiated/{initiatorId}")
    @Operation(summary = "查询我发起的审批", description = "按发起人ID查询审批实例列表")
    public Result<PageResult<ApprovalProcessInstanceEntity>> getInitiatedList(
            @PathVariable Long initiatorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<ApprovalProcessInstanceEntity> instances = approvalProcessInstanceService
                    .getApprovalProcessInstanceByApplicantId(initiatorId);
            // 手动分页
            int total = instances.size();
            int from = Math.min((page - 1) * size, total);
            int to = Math.min(from + size, total);
            List<ApprovalProcessInstanceEntity> pagedList = instances.subList(from, to);
            PageResult<ApprovalProcessInstanceEntity> pageResult = PageResult.build(
                    (long) total, size, page, pagedList);
            return Result.success(pageResult);
        } catch (Exception e) {
            logger.error("查询我发起的审批失败: {}", e.getMessage(), e);
            return Result.error("查询我发起的审批失败: " + e.getMessage());
        }
    }

    /**
     * 查询审批详情（含审批历史）。
     *
     * @param instanceId 审批实例ID
     * @return 审批详情
     */
    @GetMapping("/detail/{instanceId}")
    @Operation(summary = "查询审批详情", description = "查询审批实例详情及审批历史")
    public Result<Map<String, Object>> getApprovalDetail(@PathVariable Long instanceId) {
        try {
            Map<String, Object> detail = unifiedApprovalService.getApprovalDetail(instanceId);
            return Result.success(detail);
        } catch (Exception e) {
            logger.error("查询审批详情失败: {}", e.getMessage(), e);
            return Result.error("查询审批详情失败: " + e.getMessage());
        }
    }

    /**
     * 查询各模块审批统计。
     *
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "查询审批统计", description = "查询各模块审批数量统计")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = unifiedApprovalService.getStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("查询审批统计失败: {}", e.getMessage(), e);
            return Result.error("查询审批统计失败: " + e.getMessage());
        }
    }
}
