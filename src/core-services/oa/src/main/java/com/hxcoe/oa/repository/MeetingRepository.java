package com.hxcoe.oa.repository;

import com.hxcoe.oa.entity.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议Repository
 */
@Repository
public interface MeetingRepository extends JpaRepository<MeetingEntity, Long> {

    /**
     * 根据会议室ID和时间范围查询会议
     */
    List<MeetingEntity> findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long roomId, LocalDateTime endTime, LocalDateTime startTime);

    /**
     * 根据组织者ID查询会议列表
     */
    List<MeetingEntity> findByOrganizerId(Long organizerId);

    /**
     * 根据会议状态查询会议列表
     */
    List<MeetingEntity> findByStatus(String status);

    /**
     * 根据会议编号查询
     */
    MeetingEntity findByMeetingNo(String meetingNo);
    
    /**
     * 根据会议室ID查询会议列表
     */
    List<MeetingEntity> findByRoomId(Long roomId);
}