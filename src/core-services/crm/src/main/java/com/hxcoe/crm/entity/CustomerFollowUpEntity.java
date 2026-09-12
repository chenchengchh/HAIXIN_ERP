package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户跟进记录实体类
 */
@Entity
@Table(name = "crm_customer_follow_up")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFollowUpEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客户ID
     */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /**
     * 跟进人ID
     */
    @Column(name = "follow_up_user_id", nullable = false)
    private Long followUpUserId;

    /**
     * 跟进方式：call/email/visit/wechat
     */
    @Column(name = "follow_up_type", nullable = false, length = 32)
    private String followUpType;

    /**
     * 跟进内容
     */
    @Column(name = "content", nullable = false, length = 1024)
    private String content;

    /**
     * 下次计划
     */
    @Column(name = "next_plan", length = 512)
    private String nextPlan;

    /**
     * 跟进时间
     */
    @Column(name = "follow_up_time", nullable = false)
    private LocalDateTime followUpTime;

    /**
     * 下次跟进时间
     */
    @Column(name = "next_time")
    private LocalDateTime nextTime;

    /**
     * 客户名称（非数据库字段，查询列表时关联客户表填充）
     */
    @Transient
    private String customerName;
}