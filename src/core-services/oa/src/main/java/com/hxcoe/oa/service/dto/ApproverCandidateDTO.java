package com.hxcoe.oa.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 审批人候选DTO。
 * <p>由 {@link com.hxcoe.oa.service.ApproverResolverService} 根据节点角色+发起人部门解析得到，
 * 表示一个可执行当前节点审批操作的用户候选。</p>
 */
@Data
@AllArgsConstructor
public class ApproverCandidateDTO {

    /**
     * 审批人账号ID（对应 oa_user_account.id）
     */
    private Long userId;

    /**
     * 审批人显示名称（优先取 hr_employee.name 真实姓名，无则用 username 兜底）
     */
    private String displayName;

    /**
     * 审批人所属部门ID（对应 hr_employee.department_id，审计/统计及部门优先筛选用）
     */
    private Long departmentId;
}
