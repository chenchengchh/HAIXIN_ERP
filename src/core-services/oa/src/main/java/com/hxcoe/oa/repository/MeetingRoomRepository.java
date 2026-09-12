package com.hxcoe.oa.repository;

import com.hxcoe.oa.entity.MeetingRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 会议室Repository
 */
@Repository
public interface MeetingRoomRepository extends JpaRepository<MeetingRoomEntity, Long> {

    /**
     * 根据会议室状态查询
     */
    List<MeetingRoomEntity> findByStatus(Integer status);

    /**
     * 根据会议室名称模糊查询
     */
    List<MeetingRoomEntity> findByRoomNameContaining(String roomName);

    /**
     * 根据会议室编号查询
     */
    MeetingRoomEntity findByRoomCode(String roomCode);

    /**
     * 根据位置查询
     */
    List<MeetingRoomEntity> findByLocationContaining(String location);
    
    /**
     * 根据容量查询会议室列表
     */
    List<MeetingRoomEntity> findByCapacityGreaterThanEqual(Integer capacity);
    
    /**
     * 根据设备查询会议室列表
     */
    List<MeetingRoomEntity> findByEquipmentContaining(String equipment);
}