package com.hxcoe.ems.repository;

import com.hxcoe.ems.entity.MeterDeviceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 采集设备Repository
 * 用于访问和操作采集设备表
 *
 * @author author
 * @date 2026-01-01
 */
@Repository
public interface MeterDeviceRepository extends JpaRepository<MeterDeviceEntity, Long> {

    /**
     * 根据设备类型查询采集设备
     *
     * @param type 设备类型
     * @return 采集设备列表
     */
    List<MeterDeviceEntity> findByType(String type);
    Page<MeterDeviceEntity> findByType(String type, Pageable pageable);

    /**
     * 根据状态查询采集设备
     *
     * @param status 状态
     * @return 采集设备列表
     */
    List<MeterDeviceEntity> findByStatus(String status);
    Page<MeterDeviceEntity> findByStatus(String status, Pageable pageable);

    /**
     * 根据设备类型和状态查询采集设备
     *
     * @param type   设备类型
     * @param status 状态
     * @return 采集设备列表
     */
    List<MeterDeviceEntity> findByTypeAndStatus(String type, String status);
    Page<MeterDeviceEntity> findByTypeAndStatus(String type, String status, Pageable pageable);
}
