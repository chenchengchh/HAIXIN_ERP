package com.hxcoe.scrm.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 客户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "scrm_customer")
public class CustomerEntity {

    /**
     * 客户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客户编号
     */
    @Column(name = "customer_code", unique = true, length = 30)
    private String customerCode;

    /**
     * 客户姓名
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * 手机号码
     */
    @Column(name = "phone", length = 11)
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 性别
     */
    @Column(name = "gender", length = 10)
    private String gender;

    /**
     * 生日
     */
    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    /**
     * 客户来源
     */
    @Column(name = "source", length = 50)
    private String source;

    /**
     * 客户等级
     */
    @Column(name = "level", length = 20)
    private String level;

    /**
     * 客户状态
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 自动设置创建时间
     */
    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
    }

    /**
     * 自动更新时间
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
}
