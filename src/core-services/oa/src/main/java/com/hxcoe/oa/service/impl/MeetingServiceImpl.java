package com.hxcoe.oa.service.impl;

import com.hxcoe.oa.entity.MeetingEntity;
import com.hxcoe.oa.repository.MeetingRepository;
import com.hxcoe.oa.service.MeetingService;
import com.hxcoe.oa.util.OaSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会议管理ServiceImpl
 */
@Service
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private MeetingRepository meetingRepository;

    /**
     * 创建会议。
     * <p>组织者ID/名称未传时自动从当前登录上下文填充；
     * 时长未传时按开始/结束时间自动计算（分钟）；状态缺省为pending。</p>
     */
    @Override
    public MeetingEntity createMeeting(MeetingEntity meeting) {
        if (meeting.getOrganizerId() == null) {
            meeting.setOrganizerId(OaSecurityUtils.getCurrentUserId());
        }
        if (meeting.getOrganizerName() == null || meeting.getOrganizerName().isBlank()) {
            String username = OaSecurityUtils.getCurrentUsername();
            meeting.setOrganizerName(username != null ? username : "system");
        }
        if (meeting.getDuration() == null && meeting.getStartTime() != null && meeting.getEndTime() != null) {
            meeting.setDuration((int) java.time.Duration.between(meeting.getStartTime(), meeting.getEndTime()).toMinutes());
        }
        if (meeting.getStatus() == null || meeting.getStatus().isBlank()) {
            meeting.setStatus("pending");
        }
        return meetingRepository.save(meeting);
    }

    /**
     * 更新会议
     */
    @Override
    public MeetingEntity updateMeeting(Long id, MeetingEntity meeting) {
        MeetingEntity existingMeeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议不存在"));
        meeting.setId(id);
        return meetingRepository.save(meeting);
    }

    /**
     * 删除会议
     */
    @Override
    public void deleteMeeting(Long id) {
        MeetingEntity meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议不存在"));
        meetingRepository.delete(meeting);
    }

    /**
     * 根据ID获取会议
     */
    @Override
    public MeetingEntity getMeetingById(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议不存在"));
    }

    /**
     * 获取会议列表
     */
    @Override
    public List<MeetingEntity> getMeetingList() {
        return meetingRepository.findAll();
    }

    /**
     * 分页获取会议列表
     */
    @Override
    public Page<MeetingEntity> getMeetingPage(Pageable pageable) {
        return meetingRepository.findAll(pageable);
    }

    /**
     * 根据会议室ID获取会议列表
     */
    @Override
    public List<MeetingEntity> getMeetingByRoomId(Long roomId) {
        return meetingRepository.findByRoomId(roomId);
    }

    /**
     * 根据发起人ID获取会议列表
     */
    @Override
    public List<MeetingEntity> getMeetingByCreatorId(Long creatorId) {
        return meetingRepository.findByOrganizerId(creatorId);
    }

    /**
     * 根据状态获取会议列表
     */
    @Override
    public List<MeetingEntity> getMeetingByStatus(String status) {
        return meetingRepository.findByStatus(status);
    }

    /**
     * 取消会议
     */
    @Override
    public MeetingEntity cancelMeeting(Long id) {
        MeetingEntity meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议不存在"));
        meeting.setStatus("cancelled"); // cancelled-已取消
        return meetingRepository.save(meeting);
    }

    /**
     * 开始会议
     */
    @Override
    public MeetingEntity startMeeting(Long id) {
        MeetingEntity meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议不存在"));
        meeting.setStatus("ongoing"); // ongoing-进行中
        return meetingRepository.save(meeting);
    }

    /**
     * 结束会议
     */
    @Override
    public MeetingEntity endMeeting(Long id) {
        MeetingEntity meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议不存在"));
        meeting.setStatus("completed"); // completed-已完成
        return meetingRepository.save(meeting);
    }
}