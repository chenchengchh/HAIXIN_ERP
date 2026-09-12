package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 面试实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hr_interview")
public class InterviewEntity {

    /**
     * 面试ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 简历ID
     */
    @ManyToOne
    @JoinColumn(name = "resume_id")
    private ResumeEntity resume;

    /**
     * 面试官ID
     */
    @ManyToOne
    @JoinColumn(name = "interviewer_id")
    private EmployeeEntity interviewer;

    /**
     * 面试时间
     */
    @Column(name = "interview_time")
    private LocalDateTime interviewTime;

    /**
     * 面试类型
     */
    @Column(name = "interview_type", length = 20)
    private String interviewType;

    /**
     * 面试结果
     */
    @Column(name = "interview_result", length = 20)
    private String interviewResult;

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