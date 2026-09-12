package com.hxcoe.oa.service;

import com.hxcoe.oa.controller.ApprovalActionRequest;
import com.hxcoe.oa.controller.UnifiedApprovalRequest;
import com.hxcoe.oa.entity.ApprovalProcessInstanceEntity;
import com.hxcoe.oa.entity.ApprovalTaskEntity;

import java.util.Map;

/**
 * 统一审批服务接口。
 * <p>提供跨模块统一审批能力，包括审批提交、审批操作、审批详情查询等。</p>
 */
public interface UnifiedApprovalService {

    /**
     * 提交审批申请。
     * <p>根据来源系统和业务类型自动匹配审批流程模板，创建审批实例。</p>
     *
     * @param request 审批申请请求
     * @return 审批实例
     */
    ApprovalProcessInstanceEntity submitApproval(UnifiedApprovalRequest request);

    /**
     * 处理审批操作（通过/拒绝/退回）。
     * <p>审批完成后自动触发回调通知发起方模块。</p>
     *
     * @param taskId  审批任务ID
     * @param request 审批操作请求
     * @return 审批任务
     */
    ApprovalTaskEntity processApproval(Long taskId, ApprovalActionRequest request);

    /**
     * 查询审批详情（含审批历史）。
     *
     * @param instanceId 审批实例ID
     * @return 审批详情（含实例信息和审批任务列表）
     */
    Map<String, Object> getApprovalDetail(Long instanceId);

    /**
     * 查询各模块审批统计。
     *
     * @return 统计数据
     */
    Map<String, Object> getStatistics();
}
