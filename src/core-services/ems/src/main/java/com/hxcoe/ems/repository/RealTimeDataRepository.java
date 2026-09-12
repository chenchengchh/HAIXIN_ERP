package com.hxcoe.ems.repository;

import com.hxcoe.ems.entity.RealTimeDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 实时数据采集Repository
 * 用于访问和操作实时数据采集表
 *
 * @author author
 * @date 2026-01-01
 */
@Repository
public interface RealTimeDataRepository extends JpaRepository<RealTimeDataEntity, Long> {

    /**
     * 根据能源类型和区域查询实时数据
     *
     * @param energyType 能源类型
     * @param area       区域
     * @return 实时数据列表
     */
    List<RealTimeDataEntity> findByEnergyTypeAndArea(String energyType, String area);
    Page<RealTimeDataEntity> findByEnergyTypeAndArea(String energyType, String area, Pageable pageable);

    /**
     * 根据能源类型查询实时数据
     *
     * @param energyType 能源类型
     * @return 实时数据列表
     */
    List<RealTimeDataEntity> findByEnergyType(String energyType);
    Page<RealTimeDataEntity> findByEnergyType(String energyType, Pageable pageable);

    /**
     * 根据区域查询实时数据
     *
     * @param area 区域
     * @return 实时数据列表
     */
    List<RealTimeDataEntity> findByArea(String area);
    Page<RealTimeDataEntity> findByArea(String area, Pageable pageable);

    /**
     * 查询指定时间范围内的实时数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 实时数据列表
     */
    List<RealTimeDataEntity> findByCollectionTimeBetween(LocalDateTime startTime, LocalDateTime endTime);



    /**
     * 根据状态查询实时数据
     *
     * @param status 状态
     * @return 实时数据列表
     */
    List<RealTimeDataEntity> findByStatus(String status);

    /**
     * 判断指定能源类型、设备ID、采集时间的记录是否已存在（幂等查重）
     *
     * @param energyType     能源类型
     * @param deviceId       设备ID
     * @param collectionTime 采集时间
     * @return true-已存在
     */
    boolean existsByEnergyTypeAndDeviceIdAndCollectionTime(String energyType, Long deviceId, LocalDateTime collectionTime);

    /**
     * 判断指定能源类型、采集时间且设备ID为空的记录是否已存在（幂等查重）
     *
     * @param energyType     能源类型
     * @param collectionTime 采集时间
     * @return true-已存在
     */
    boolean existsByEnergyTypeAndDeviceIdIsNullAndCollectionTime(String energyType, LocalDateTime collectionTime);
}
