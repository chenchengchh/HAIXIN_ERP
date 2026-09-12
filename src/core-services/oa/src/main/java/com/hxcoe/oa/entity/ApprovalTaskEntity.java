package com.hxcoe.oa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 审批任务实体类
 */
@Data
@Entity
@Table(name = "oa_approval_task", indexes = {
    @Index(name = "idx_task_assignee_status", columnList = "assignee_id, status"),
    @Index(name = "idx_task_instance_id", columnList = "instance_id"),
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_instance_node", columnList = "instance_id, node_id")
})
@EntityListeners(AuditingEntityListener.class)
public class ApprovalTaskEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 实例ID
     */
    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    /**
     * 任务名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 任务描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 节点ID
     */
    @Column(name = "node_id", nullable = false, length = 50)
    private String nodeId;

    /**
     * 节点名称
     */
    @Column(name = "node_name", nullable = false, length = 100)
    private String nodeName;

    /**
     * 审批节点要求的角色编码（用于权限校验，对应 process_definition.assigneeRole）
     */
    @Column(name = "assignee_role", length = 64)
    private String assigneeRole;

    /**
     * 节点序号，对应 nodes 数组下标，用于审批历史按节点排序展示
     */
    @Column(name = "node_index")
    private Integer nodeIndex;

    /**
     * 审批人ID
     */
    @Column(name = "assignee_id", nullable = false)
    private Long assigneeId;

    /**
     * 审批人名称
     */
    @Column(name = "assignee_name", nullable = false, length = 50)
    private String assigneeName;

    /**
     * 审批人所属部门ID（审计/统计用）
     */
    @Column(name = "assignee_dept_id")
    private Long assigneeDeptId;

    /**
     * 任务状态：pending-待审批，approved-已通过，rejected-已拒绝，cancelled-已取消
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 审批结果
     */
    @Column(name = "result", length = 20)
    private String result;

    /**
     * 审批意见
     */
    @Column(name = "comment", length = 500)
    private String comment;

    /**
     * 审批时间
     */
    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 到期时间
     */
    @Column(name = "due_time")
    private LocalDateTime dueTime;
}