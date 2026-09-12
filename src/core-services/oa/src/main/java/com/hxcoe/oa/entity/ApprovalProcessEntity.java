package com.hxcoe.oa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 审批流程定义实体类
 */
@Data
@Entity
@Table(name = "oa_approval_process")
@EntityListeners(AuditingEntityListener.class)
public class ApprovalProcessEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 流程名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 流程编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 流程描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 流程状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 流程类型
     */
    @Column(nullable = false, length = 50)
    private String processType;

    /**
     * 流程定义JSON
     */
    @Column(nullable = false, columnDefinition = "text")
    private String processDefinition;

    /**
     * 表单配置JSON
     */
    @Column(columnDefinition = "text")
    private String formConfig;

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
     * 创建人ID
     */
    @Column(nullable = false)
    private Long creatorId;

    /**
     * 创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    @LastModifiedDate
    private LocalDateTime updateTime;
}