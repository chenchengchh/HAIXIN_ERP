package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 招聘需求实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_recruitment_demand")
public class RecruitmentDemandEntity {

    /**
     * 招聘需求ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 职位名称
     */
    @Column(name = "position_name", nullable = false, length = 50)
    private String positionName;

    /**
     * 部门ID
     */
    @ManyToOne
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private DepartmentEntity department;
    
    /**
     * 部门ID（直接映射到数据库字段）
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 需求人数
     */
    @Column(name = "demand_number")
    private Integer demandNumber;

    /**
     * 所需技能
     */
    @Column(name = "required_skills", length = 500)
    private String requiredSkills;

    /**
     * 期望薪资
     */
    @Column(name = "expected_salary", length = 20)
    private String expectedSalary;

    /**
     * 状态
     */
    @Column(name = "status", length = 20)
    private String status;

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
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

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