package com.hxcoe.crm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 客户联系人实体类
 */
@Entity
@Table(name = "crm_customer_contact")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerContactEntity {

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
     * 姓名
     */
    @Column(name = "contact_name", nullable = false, length = 64)
    private String contactName;

    /**
     * 职位
     */
    @Column(name = "position", length = 64)
    private String position;

    /**
     * 电话
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 手机
     */
    @Column(name = "mobile", length = 20)
    private String mobile;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 64)
    private String email;

    /**
     * 微信
     */
    @Column(name = "wechat", length = 64)
    private String wechat;

    /**
     * 是否主要联系人
     */
    @Column(name = "is_primary", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isPrimary;

    /**
     * 备注
     */
    @Column(name = "remark", length = 255)
    private String remark;

    /**
     * 客户名称（非数据库字段，查询列表时关联客户表填充）
     */
    @Transient
    private String customerName;
}