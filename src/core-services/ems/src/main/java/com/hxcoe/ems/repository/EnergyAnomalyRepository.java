package com.hxcoe.ems.repository;

import com.hxcoe.ems.entity.EnergyAnomalyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 能耗异常Repository
 * 用于访问和操作能耗异常数据表
 *
 * @author author
 * @date 2026-01-01
 */
@Repository
public interface EnergyAnomalyRepository extends JpaRepository<EnergyAnomalyEntity, Long> {

    /**
     * 根据能源类型和区域查询能耗异常
     *
     * @param energyType 能源类型
     * @param area       区域
     * @return 能耗异常列表
     */
    List<EnergyAnomalyEntity> findByEnergyTypeAndArea(String energyType, String area);
    Page<EnergyAnomalyEntity> findByEnergyTypeAndArea(String energyType, String area, Pageable pageable);
    Page<EnergyAnomalyEntity> findByEnergyTypeAndAreaAndStatus(String energyType, String area, String status, Pageable pageable);

    /**
     * 根据能源类型查询能耗异常
     *
     * @param energyType 能源类型
     * @return 能耗异常列表
     */
    List<EnergyAnomalyEntity> findByEnergyType(String energyType);
    Page<EnergyAnomalyEntity> findByEnergyType(String energyType, Pageable pageable);
    Page<EnergyAnomalyEntity> findByEnergyTypeAndStatus(String energyType, String status, Pageable pageable);

    /**
     * 根据区域查询能耗异常
     *
     * @param area 区域
     * @return 能耗异常列表
     */
    List<EnergyAnomalyEntity> findByArea(String area);
    Page<EnergyAnomalyEntity> findByArea(String area, Pageable pageable);
    Page<EnergyAnomalyEntity> findByAreaAndStatus(String area, String status, Pageable pageable);

    /**
     * 根据状态查询能耗异常
     *
     * @param status 状态
     * @return 能耗异常列表
     */
    List<EnergyAnomalyEntity> findByStatus(String status);
    Page<EnergyAnomalyEntity> findByStatus(String status, Pageable pageable);

    /**
     * 查询指定时间范围内的能耗异常
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 能耗异常列表
     */
    List<EnergyAnomalyEntity> findByDetectionTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据异常类型查询能耗异常
     *
     * @param anomalyType 异常类型
     * @return 能耗异常列表
     */
    List<EnergyAnomalyEntity> findByAnomalyType(String anomalyType);

    /**
     * 统计待处理异常数量
     *
     * @return 待处理异常数量
     */
    long countByStatus(String status);
}
