package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 服务工单实体类
 */
@Data
@Entity
@Table(name = "crm_service_ticket")
@EntityListeners(AuditingEntityListener.class)
public class ServiceTicket {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 工单编号
     */
    @Column(name = "ticket_no")
    private String ticketNo;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 客户ID
     */
    @Column(name = "customer_id")
    private Long customerId;

    /**
     * 客户名称
     */
    @Column(name = "customer_name")
    private String customerName;

    /**
     * 联系人
     */
    @Column(name = "contact_name")
    private String contactName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 工单分类
     */
    private String category;

    /**
     * 优先级：LOW-低，MEDIUM-中，HIGH-高，URGENT-紧急
     */
    private String priority = "MEDIUM";

    /**
     * 状态：OPEN-待处理，ASSIGNED-已分配，REPLIED-已回复，CLOSED-已关闭
     */
    private String status = "OPEN";

    /**
     * 问题描述
     */
    private String description;

    /**
     * 处理人ID
     */
    @Column(name = "assignee_id")
    private Long assigneeId;

    /**
     * 处理人姓名
     */
    @Column(name = "assignee_name")
    private String assigneeName;

    /**
     * 回复记录（JSON数组字符串）
     */
    @Column(columnDefinition = "TEXT")
    private String replies;

    /**
     * 关闭时间
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

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
     * 创建人
     */
    @CreatedBy
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Column(name = "is_deleted")
    private Integer isDeleted = 0;
}
