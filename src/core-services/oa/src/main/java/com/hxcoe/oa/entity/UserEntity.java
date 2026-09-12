package com.hxcoe.oa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Entity
@Table(name = "hr_employee")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工编号（作为登录名）
     */
    @Column(name = "employee_code", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码
     */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String realName;

    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Column(length = 100)
    private String departmentName;

    /**
     * 职位
     */
    @Column(length = 100)
    private String position;

    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 用户状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 角色列表，以逗号分隔
     */
    @Column(length = 200)
    private String roles;

    /**
     * HR员工ID，关联HR模块的employee表
     */
    private Long employeeId;

    @Column(name = "created_time")
    @CreatedDate
    private LocalDateTime createTime;

    @Column(name = "updated_time")
    @LastModifiedDate
    private LocalDateTime updateTime;
}