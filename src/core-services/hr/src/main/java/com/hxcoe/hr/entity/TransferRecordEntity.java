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
 * 转岗记录实体类
 */
@Data
@Entity
@Table(name = "hr_transfer_record")
@EntityListeners(AuditingEntityListener.class)
public class TransferRecordEntity {

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
     * 原部门ID
     */
    @Column(name = "old_department_id")
    private Long oldDepartmentId;

    /**
     * 新部门ID
     */
    @Column(name = "new_department_id")
    private Long newDepartmentId;

    /**
     * 原岗位ID
     */
    @Column(name = "old_position_id")
    private Long oldPositionId;

    /**
     * 新岗位ID
     */
    @Column(name = "new_position_id")
    private Long newPositionId;

    /**
     * 转岗原因
     */
    @Column(name = "reason")
    private String reason;

    /**
     * 转岗日期
     */
    @Column(name = "transfer_date")
    private LocalDateTime transferDate;

    /**
     * 状态（0：申请中，1：已通过，2：已拒绝）
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