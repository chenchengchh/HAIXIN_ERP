package com.hxcoe.oa.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 会议实体类
 */
@Data
@Entity
@Table(name = "oa_meeting")
@EntityListeners(AuditingEntityListener.class)
public class MeetingEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会议编号
     */
    @Column(nullable = false, unique = true, length = 50)
    private String meetingNo;

    /**
     * 会议标题
     */
    @Column(nullable = false, length = 200)
    private String meetingTitle;

    /**
     * 会议室ID
     */
    @Column(nullable = false)
    private Long roomId;

    /**
     * 会议室名称
     */
    @Column(nullable = false, length = 100)
    private String roomName;

    /**
     * 会议开始时间
     */
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 会议结束时间
     */
    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 会议时长（分钟）
     */
    @Column(nullable = false)
    private Integer duration;

    /**
     * 会议组织者ID
     */
    @Column(nullable = false)
    private Long organizerId;

    /**
     * 会议组织者名称
     */
    @Column(nullable = false, length = 50)
    private String organizerName;

    /**
     * 参会人员ID列表，以逗号分隔
     */
    @Column(length = 500)
    private String attendees;

    /**
     * 参会人员名称列表，以逗号分隔
     */
    @Column(length = 1000)
    private String attendeeNames;

    /**
     * 会议议题
     */
    @Column(length = 500)
    private String agenda;

    /**
     * 会议状态：pending-待开始，ongoing-进行中，completed-已完成，cancelled-已取消
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * 会议备注
     */
    @Column(length = 500)
    private String remark;

    /**
     * 创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    private LocalDateTime updateTime;
}