package com.hxcoe.oa.service;

import com.hxcoe.oa.entity.MeetingRoomEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 会议室管理Service
 */
public interface MeetingRoomService {

    /**
     * 创建会议室
     */
    MeetingRoomEntity createMeetingRoom(MeetingRoomEntity room);

    /**
     * 更新会议室
     */
    MeetingRoomEntity updateMeetingRoom(Long id, MeetingRoomEntity room);

    /**
     * 删除会议室
     */
    void deleteMeetingRoom(Long id);

    /**
     * 根据ID获取会议室
     */
    MeetingRoomEntity getMeetingRoomById(Long id);

    /**
     * 根据会议室编码获取会议室
     */
    MeetingRoomEntity getMeetingRoomByCode(String code);

    /**
     * 获取会议室列表
     */
    List<MeetingRoomEntity> getMeetingRoomList();

    /**
     * 分页获取会议室列表
     */
    Page<MeetingRoomEntity> getMeetingRoomPage(Pageable pageable);

    /**
     * 根据状态获取会议室列表
     */
    List<MeetingRoomEntity> getMeetingRoomByStatus(Integer status);

    /**
     * 根据容量获取会议室列表
     */
    List<MeetingRoomEntity> getMeetingRoomByCapacityGreaterThanEqual(Integer capacity);

    /**
     * 根据设备获取会议室列表
     */
    List<MeetingRoomEntity> getMeetingRoomByEquipmentContaining(String equipment);

    /**
     * 启用会议室
     */
    MeetingRoomEntity enableMeetingRoom(Long id);

    /**
     * 禁用会议室
     */
    MeetingRoomEntity disableMeetingRoom(Long id);
}