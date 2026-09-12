package com.hxcoe.oa.service.impl;

import com.hxcoe.oa.entity.MeetingRoomEntity;
import com.hxcoe.oa.repository.MeetingRoomRepository;
import com.hxcoe.oa.service.MeetingRoomService;
import com.hxcoe.oa.util.OaSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会议室管理ServiceImpl
 */
@Service
public class MeetingRoomServiceImpl implements MeetingRoomService {

    @Autowired
    private MeetingRoomRepository meetingRoomRepository;

    /**
     * 创建会议室。
     * <p>创建人ID/名称未传时自动从当前登录上下文填充，避免非空约束违约。</p>
     */
    @Override
    public MeetingRoomEntity createMeetingRoom(MeetingRoomEntity room) {
        if (room.getCreatorId() == null) {
            room.setCreatorId(OaSecurityUtils.getCurrentUserId());
        }
        if (room.getCreatorName() == null || room.getCreatorName().isBlank()) {
            String username = OaSecurityUtils.getCurrentUsername();
            room.setCreatorName(username != null ? username : "system");
        }
        if (room.getStatus() == null) {
            room.setStatus(1);
        }
        return meetingRoomRepository.save(room);
    }

    /**
     * 更新会议室
     */
    @Override
    public MeetingRoomEntity updateMeetingRoom(Long id, MeetingRoomEntity room) {
        MeetingRoomEntity existingRoom = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议室不存在"));
        room.setId(id);
        return meetingRoomRepository.save(room);
    }

    /**
     * 删除会议室
     */
    @Override
    public void deleteMeetingRoom(Long id) {
        MeetingRoomEntity room = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议室不存在"));
        meetingRoomRepository.delete(room);
    }

    /**
     * 根据ID获取会议室
     */
    @Override
    public MeetingRoomEntity getMeetingRoomById(Long id) {
        return meetingRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议室不存在"));
    }

    /**
     * 根据会议室编码获取会议室
     */
    @Override
    public MeetingRoomEntity getMeetingRoomByCode(String code) {
        MeetingRoomEntity room = meetingRoomRepository.findByRoomCode(code);
        if (room == null) {
            throw new RuntimeException("会议室不存在");
        }
        return room;
    }

    /**
     * 获取会议室列表
     */
    @Override
    public List<MeetingRoomEntity> getMeetingRoomList() {
        return meetingRoomRepository.findAll();
    }

    /**
     * 分页获取会议室列表
     */
    @Override
    public Page<MeetingRoomEntity> getMeetingRoomPage(Pageable pageable) {
        return meetingRoomRepository.findAll(pageable);
    }

    /**
     * 根据状态获取会议室列表
     */
    @Override
    public List<MeetingRoomEntity> getMeetingRoomByStatus(Integer status) {
        return meetingRoomRepository.findByStatus(status);
    }

    /**
     * 根据容量获取会议室列表
     */
    @Override
    public List<MeetingRoomEntity> getMeetingRoomByCapacityGreaterThanEqual(Integer capacity) {
        return meetingRoomRepository.findByCapacityGreaterThanEqual(capacity);
    }

    /**
     * 根据设备获取会议室列表
     */
    @Override
    public List<MeetingRoomEntity> getMeetingRoomByEquipmentContaining(String equipment) {
        return meetingRoomRepository.findByEquipmentContaining(equipment);
    }

    /**
     * 启用会议室
     */
    @Override
    public MeetingRoomEntity enableMeetingRoom(Long id) {
        MeetingRoomEntity room = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议室不存在"));
        room.setStatus(1); // 1-启用
        return meetingRoomRepository.save(room);
    }

    /**
     * 禁用会议室
     */
    @Override
    public MeetingRoomEntity disableMeetingRoom(Long id) {
        MeetingRoomEntity room = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会议室不存在"));
        room.setStatus(0); // 0-禁用
        return meetingRoomRepository.save(room);
    }
}