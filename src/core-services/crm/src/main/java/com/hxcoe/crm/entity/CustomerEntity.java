package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 客户实体类
 */
@Entity
@Table(name = "crm_customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客户编号
     */
    @Column(name = "customer_no", unique = true, nullable = false, length = 32)
    private String customerNo;

    /**
     * 客户名称
     */
    @Column(name = "customer_name", nullable = false, length = 128)
    private String customerName;

    /**
     * 客户类型：enterprise/individual
     */
    @Column(name = "customer_type", nullable = false, length = 32)
    private String customerType;

    /**
     * 行业
     */
    @Column(name = "industry", length = 64)
    private String industry;

    /**
     * 规模：large/medium/small
     */
    @Column(name = "scale", length = 32)
    private String scale;

    /**
     * 级别：A/B/C
     */
    @Column(name = "level", length = 8)
    private String level;

    /**
     * 状态：potential/active/inactive/lost
     */
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    /**
     * 来源：website/exhibition/referral/ad
     */
    @Column(name = "source", length = 32)
    private String source;

    /**
     * 标签（逗号分隔）
     */
    @Column(name = "tags", length = 255)
    private String tags;

    /**
     * 地区
     */
    @Column(name = "region", length = 64)
    private String region;

    /**
     * 详细地址
     */
    @Column(name = "address", length = 255)
    private String address;

    /**
     * 网站
     */
    @Column(name = "website", length = 128)
    private String website;

    /**
     * 责任人ID
     */
    @Column(name = "owner_id")
    private Long ownerId;

    /**
     * 责任人姓名
     */
    @Column(name = "owner_name", length = 64)
    private String ownerName;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0-未删除，1-已删除
     */
    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted;
}
