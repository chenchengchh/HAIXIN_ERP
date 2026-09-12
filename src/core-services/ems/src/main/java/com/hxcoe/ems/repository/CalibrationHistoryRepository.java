package com.hxcoe.ems.repository;

import com.hxcoe.ems.entity.CalibrationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 校准历史Repository
 * 用于访问和操作校准历史表
 *
 * @author author
 * @date 2026-01-01
 */
@Repository
public interface CalibrationHistoryRepository extends JpaRepository<CalibrationHistoryEntity, Long> {

    /**
     * 根据设备ID查询校准历史记录
     *
     * @param meterId 设备ID
     * @return 校准历史记录列表
     */
    List<CalibrationHistoryEntity> findByMeterId(Long meterId);

    /**
     * 查询指定时间范围内的校准历史记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 校准历史记录列表
     */
    List<CalibrationHistoryEntity> findByCalibrationTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据设备ID和时间范围查询校准历史记录
     *
     * @param meterId   设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 校准历史记录列表
     */
    List<CalibrationHistoryEntity> findByMeterIdAndCalibrationTimeBetween(Long meterId, LocalDateTime startTime, LocalDateTime endTime);
}