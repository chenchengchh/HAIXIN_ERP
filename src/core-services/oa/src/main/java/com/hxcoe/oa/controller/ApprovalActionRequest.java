package com.hxcoe.oa.controller;

import lombok.Data;

/**
 * 审批操作请求DTO。
 * <p>审批人通过此DTO提交审批结果（通过/拒绝/退回）。</p>
 */
@Data
public class ApprovalActionRequest {

    /**
     * 审批动作：approve（通过）/ reject（拒绝）/ return（退回）
     */
    private String action;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人名称
     */
    private String approverName;
}
