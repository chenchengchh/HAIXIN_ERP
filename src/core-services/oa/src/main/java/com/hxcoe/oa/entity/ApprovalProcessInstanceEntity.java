package com.hxcoe.oa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 审批流程实例实体类
 */
@Data
@Entity
@Table(name = "oa_approval_process_instance")
@EntityListeners(AuditingEntityListener.class)
public class ApprovalProcessInstanceEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 流程ID
     */
    @Column(nullable = false)
    private Long processId;

    /**
     * 流程编码
     */
    @Column(nullable = false, length = 50)
    private String processCode;

    /**
     * 流程标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 流程描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 发起人ID
     */
    @Column(nullable = false)
    private Long initiatorId;

    /**
     * 发起人名称
     */
    @Column(nullable = false, length = 50)
    private String initiatorName;

    /**
     * 当前节点ID
     */
    @Column(length = 50)
    private String currentNodeId;

    /**
     * 当前节点名称
     */
    @Column(length = 100)
    private String currentNodeName;

    /**
     * 流程状态
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * 表单数据JSON
     */
    @Column(nullable = false, columnDefinition = "text")
    private String formData;

    /**
     * 流程变量JSON
     */
    @Column(columnDefinition = "text")
    private String processVariables;

    /**
     * 关联ERP订单ID
     */
    private Long erpOrderId;

    /**
     * 关联SCM供应商ID
     */
    private Long scmSupplierId;

    /**
     * 关联MES车间ID
     */
    private Long mesWorkshopId;

    /**
     * 流程开始时间
     */
    private LocalDateTime startTime;

    /**
     * 流程结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    private LocalDateTime updateTime;
}