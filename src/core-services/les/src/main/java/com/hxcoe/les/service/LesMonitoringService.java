package com.hxcoe.les.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesVehicleLocationDto;
import com.hxcoe.les.entity.LesAnomalyEventEntity;
import com.hxcoe.les.entity.LesMonitorLogEntity;

import java.util.List;

public interface LesMonitoringService {

    /**
     * 分页查询在途监控日志
     *
     * @param page      页码（从1开始）
     * @param size      每页条数
     * @param planId    计划ID（可选）
     * @param vehicleId 车辆ID（可选）
     * @return 分页结果
     */
    Result<PageResult<LesMonitorLogEntity>> fetchMonitorLogs(int page, int size, Long planId, Long vehicleId);

    /**
     * 分页查询指定计划的监控日志
     *
     * @param planId 计划ID
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @return 分页结果
     */
    Result<PageResult<LesMonitorLogEntity>> fetchPlanMonitorLogs(Long planId, int page, int size);

    /**
     * 分页查询异常事件
     *
     * @param page      页码（从1开始）
     * @param size      每页条数
     * @param planId    计划ID（可选）
     * @param vehicleId 车辆ID（可选）
     * @param status    处理状态（可选）
     * @return 分页结果
     */
    Result<PageResult<LesAnomalyEventEntity>> fetchAnomalyEvents(int page, int size, Long planId, Long vehicleId, String status);

    /**
     * 更新异常事件处理结果
     *
     * @param id             事件ID
     * @param handlingStatus 处理状态
     * @param handlingResult 处理结果
     * @return 更新后的事件
     */
    Result<LesAnomalyEventEntity> handleAnomalyEvent(Long id, String handlingStatus, String handlingResult);

    /**
     * 获取车辆实时位置（返回每个车辆最新一条记录）
     *
     * @param vehicleIds 车辆ID列表（可为空，为空则返回所有车辆的最新位置）
     * @return 位置列表
     */
    Result<List<LesVehicleLocationDto>> fetchVehicleLocations(List<Long> vehicleIds);

    /**
     * 获取车辆历史轨迹
     *
     * @param vehicleId 车辆ID
     * @param startTime 开始时间（可为空）
     * @param endTime   结束时间（可为空）
     * @return 轨迹点列表
     */
    Result<List<LesVehicleLocationDto>> fetchVehicleHistory(Long vehicleId, String startTime, String endTime);

    /**
     * 获取运输计划轨迹回放
     *
     * @param planId    计划ID
     * @param startTime 开始时间（可为空）
     * @param endTime   结束时间（可为空）
     * @return 轨迹点列表
     */
    Result<List<LesVehicleLocationDto>> fetchPlanTrack(Long planId, String startTime, String endTime);
}

