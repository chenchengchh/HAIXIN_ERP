package com.hxcoe.oa.service;

import com.hxcoe.oa.entity.MeetingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 会议管理Service
 */
public interface MeetingService {

    /**
     * 创建会议
     */
    MeetingEntity createMeeting(MeetingEntity meeting);

    /**
     * 更新会议
     */
    MeetingEntity updateMeeting(Long id, MeetingEntity meeting);

    /**
     * 删除会议
     */
    void deleteMeeting(Long id);

    /**
     * 根据ID获取会议
     */
    MeetingEntity getMeetingById(Long id);

    /**
     * 获取会议列表
     */
    List<MeetingEntity> getMeetingList();

    /**
     * 分页获取会议列表
     */
    Page<MeetingEntity> getMeetingPage(Pageable pageable);

    /**
     * 根据会议室ID获取会议列表
     */
    List<MeetingEntity> getMeetingByRoomId(Long roomId);

    /**
     * 根据发起人ID获取会议列表
     */
    List<MeetingEntity> getMeetingByCreatorId(Long creatorId);

    /**
     * 根据状态获取会议列表
     */
    List<MeetingEntity> getMeetingByStatus(String status);

    /**
     * 取消会议
     */
    MeetingEntity cancelMeeting(Long id);

    /**
     * 开始会议
     */
    MeetingEntity startMeeting(Long id);

    /**
     * 结束会议
     */
    MeetingEntity endMeeting(Long id);
}