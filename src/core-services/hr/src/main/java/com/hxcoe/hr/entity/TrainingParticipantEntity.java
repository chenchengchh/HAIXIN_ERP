package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 培训参与实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_training_participant")
public class TrainingParticipantEntity {

    /**
     * 培训参与记录ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 培训计划ID
     */
    @Column(name = "training_id", nullable = false)
    private Long trainingId;

    /**
     * 员工ID
     */
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    /**
     * 参与状态：0已报名 1已参加 2未参加
     */
    @Column(name = "attendance_status")
    private Integer attendanceStatus;

    /**
     * 完成状态：0未完成 1已完成
     */
    @Column(name = "completion_status")
    private Integer completionStatus;

    /**
     * 培训成绩
     */
    @Column(name = "score")
    private BigDecimal score;

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
