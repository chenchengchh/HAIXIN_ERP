package com.hxcoe.oa.service;

import com.hxcoe.oa.service.dto.ApproverCandidateDTO;

import java.util.List;

/**
 * 审批人解析服务。
 * <p>根据流程节点要求的角色编码 + 发起人信息，解析会签审批人候选列表，
 * 实现部门优先策略与角色权限校验。</p>
 */
public interface ApproverResolverService {

    /**
     * 按角色+发起人解析会签审批人列表。
     * <p>解析算法：</p>
     * <ol>
     *   <li>按角色编码查角色ID</li>
     *   <li>反查所有具备该角色的用户ID</li>
     *   <li>批量查账号，过滤 ACTIVE 状态</li>
     *   <li>解析发起人部门ID（admin 无 employeeId 时为 null）</li>
     *   <li>部门优先：筛选与发起人同部门的候选</li>
     *   <li>全局回退：本部门无人时使用全部候选</li>
     *   <li>按账号ID去重</li>
     * </ol>
     *
     * @param assigneeRole    节点要求的角色编码
     * @param initiatorUserId 发起人账号ID
     * @return 审批人候选列表（非空）
     * @throws RuntimeException 当角色不存在或无可用审批人时抛出
     */
    List<ApproverCandidateDTO> resolveApprovers(String assigneeRole, Long initiatorUserId);

    /**
     * 校验用户是否具备指定角色。
     * <p>用于审批操作时的权限校验：审批人必须具备任务节点的 assigneeRole。</p>
     *
     * @param userId   用户账号ID
     * @param roleCode 角色编码
     * @return 具备角色返回 true，否则 false
     */
    boolean hasRole(Long userId, String roleCode);
}
