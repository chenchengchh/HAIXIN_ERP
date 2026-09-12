package com.hxcoe.oa.service.dto;

import lombok.Data;

/**
 * 流程节点DTO。
 * <p>从审批流程定义JSON（oa_approval_process.process_definition）解析得到的单个节点信息，
 * 用于多节点会签审批流转的节点定位与权限校验。</p>
 * <p>对应JSON结构：{"id":"node_1","name":"销售经理审批","assigneeRole":"sales_manager","action":"approve"}</p>
 */
@Data
public class ProcessNodeDTO {

    /**
     * 节点ID（如 node_1、node_2），流程内唯一
     */
    private String id;

    /**
     * 节点名称（如 销售经理审批），用于任务展示
     */
    private String name;

    /**
     * 节点要求的审批角色编码（如 sales_manager），用于解析会签审批人及权限校验
     */
    private String assigneeRole;

    /**
     * 节点动作（如 approve），预留扩展
     */
    private String action;

    /**
     * 节点序号，对应 nodes 数组下标，解析时填充，用于审批历史按节点顺序展示
     */
    private Integer nodeIndex;
}
