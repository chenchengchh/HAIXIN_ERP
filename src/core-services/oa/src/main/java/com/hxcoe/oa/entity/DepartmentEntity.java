package com.hxcoe.oa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 部门实体类
 */
@Data
@Entity
@Table(name = "hr_department")
@EntityListeners(AuditingEntityListener.class)
public class DepartmentEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 部门名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 部门编码
     */
    @Column(name = "department_code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 部门描述
     */
    @Column(length = 500)
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 父部门名称
     */
    @Column(length = 100)
    private String parentName;

    /**
     * 部门层级
     */
    @Column(nullable = false)
    private Integer level;

    /**
     * 部门排序
     */
    private Integer sort;

    /**
     * 部门状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status;

    @Column(name = "manager_id")
    private Long managerId;

    /**
     * 部门负责人名称
     */
    @Column(length = 50)
    private String managerName;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    @CreatedDate
    private LocalDateTime createTime;

    @Column(name = "updated_time")
    @LastModifiedDate
    private LocalDateTime updateTime;
}