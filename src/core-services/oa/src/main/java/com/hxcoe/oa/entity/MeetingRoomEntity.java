package com.hxcoe.oa.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 会议室实体类
 */
@Data
@Entity
@Table(name = "oa_meeting_room")
@EntityListeners(AuditingEntityListener.class)
public class MeetingRoomEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会议室编号
     */
    @Column(nullable = false, unique = true, length = 50)
    private String roomCode;

    /**
     * 会议室名称
     */
    @Column(nullable = false, length = 100)
    private String roomName;

    /**
     * 会议室位置
     */
    @Column(length = 200)
    private String location;

    /**
     * 容纳人数
     */
    private Integer capacity;

    /**
     * 设备配置
     */
    @Column(length = 500)
    private String equipment;

    /**
     * 会议室状态：0-禁用，1-可用，2-维护中
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 会议室描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 创建人ID
     */
    @Column(nullable = false)
    private Long creatorId;

    /**
     * 创建人名称
     */
    @Column(nullable = false, length = 50)
    private String creatorName;

    /**
     * 创建时间
     */
    @CreatedDate
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private Long updaterId;

    /**
     * 更新时间
     */
    @LastModifiedDate
    private LocalDateTime updateTime;
}