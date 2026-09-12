package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 离职申请实体类
 */
@Data
@Entity
@Table(name = "hr_resignation_request")
@EntityListeners(AuditingEntityListener.class)
public class ResignationRequestEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工ID
     */
    @Column(name = "employee_id")
    private Long employeeId;

    /**
     * 离职类型（0：主动离职，1：被动离职）
     */
    @Column(name = "resignation_type")
    private Integer resignationType;

    /**
     * 离职原因
     */
    @Column(name = "reason")
    private String reason;

    /**
     * 申请日期
     */
    @Column(name = "apply_date")
    private LocalDateTime applyDate;

    /**
     * 预计离职日期
     */
    @Column(name = "expected_resign_date")
    private LocalDateTime expectedResignDate;

    /**
     * 实际离职日期
     */
    @Column(name = "actual_resign_date")
    private LocalDateTime actualResignDate;

    /**
     * 状态（0：申请中，1：已批准，2：已拒绝，3：已完成）
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}