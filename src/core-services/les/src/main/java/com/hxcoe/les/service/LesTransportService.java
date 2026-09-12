package com.hxcoe.les.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesSalesOrderDto;
import com.hxcoe.les.entity.LesDriverEntity;
import com.hxcoe.les.entity.LesRouteEntity;
import com.hxcoe.les.entity.LesTransportPlanEntity;
import com.hxcoe.les.entity.LesTransportTaskEntity;
import com.hxcoe.les.entity.LesVehicleEntity;

public interface LesTransportService {

    /**
     * 分页查询运输计划
     *
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @param keyword 关键字（planNo/salesOrderNo）
     * @param status 计划状态（可选）
     * @return 分页结果
     */
    Result<PageResult<LesTransportPlanEntity>> fetchTransportPlans(int page, int size, String keyword, Integer status);

    /**
     * 查询运输计划详情
     *
     * @param id 计划ID
     * @return 计划详情
     */
    Result<LesTransportPlanEntity> getTransportPlanDetail(Long id);

    /**
     * 创建运输计划
     *
     * @param plan 计划数据
     * @return 创建后的计划
     */
    Result<LesTransportPlanEntity> createTransportPlan(LesTransportPlanEntity plan);

    /**
     * 更新运输计划
     *
     * @param id   计划ID
     * @param plan 更新数据
     * @return 更新后的计划
     */
    Result<LesTransportPlanEntity> updateTransportPlan(Long id, LesTransportPlanEntity plan);

    /**
     * 删除运输计划
     *
     * @param id 计划ID
     * @return 删除结果
     */
    Result<Void> deleteTransportPlan(Long id);

    /**
     * 分页查询车辆
     *
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @param keyword 关键字（licensePlate）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Result<PageResult<LesVehicleEntity>> fetchVehicles(int page, int size, String keyword, String status);

    /**
     * 分页查询司机
     *
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @param keyword 关键字（name/phone）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Result<PageResult<LesDriverEntity>> fetchDrivers(int page, int size, String keyword, String status);

    /**
     * 分页查询路线
     *
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @param keyword 关键字（routeName）
     * @return 分页结果
     */
    Result<PageResult<LesRouteEntity>> fetchRoutes(int page, int size, String keyword);

    /**
     * 分页查询销售订单来源（首版可返回本地物流订单镜像，后续再聚合WMS/CRM）
     *
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @param keyword 关键字（orderNo/address）
     * @return 分页结果
     */
    Result<PageResult<LesSalesOrderDto>> fetchSalesOrders(int page, int size, String keyword);

    /**
     * 创建运输任务
     *
     * @param task 任务数据
     * @return 创建后的任务
     */
    Result<LesTransportTaskEntity> createTransportTask(LesTransportTaskEntity task);

    /**
     * 分页查询运输任务
     *
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @param planId 计划ID（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Result<PageResult<LesTransportTaskEntity>> fetchTransportTasks(int page, int size, Long planId, String status);

    /**
     * 创建车辆
     *
     * @param vehicle 车辆数据
     * @return 创建后的车辆
     */
    Result<LesVehicleEntity> createVehicle(LesVehicleEntity vehicle);

    /**
     * 更新车辆
     *
     * @param id      车辆ID
     * @param vehicle 更新数据
     * @return 更新后的车辆
     */
    Result<LesVehicleEntity> updateVehicle(Long id, LesVehicleEntity vehicle);

    /**
     * 删除车辆
     *
     * @param id 车辆ID
     * @return 删除结果
     */
    Result<Void> deleteVehicle(Long id);

    /**
     * 创建司机
     *
     * @param driver 司机数据
     * @return 创建后的司机
     */
    Result<LesDriverEntity> createDriver(LesDriverEntity driver);

    /**
     * 更新司机
     *
     * @param id     司机ID
     * @param driver 更新数据
     * @return 更新后的司机
     */
    Result<LesDriverEntity> updateDriver(Long id, LesDriverEntity driver);

    /**
     * 删除司机
     *
     * @param id 司机ID
     * @return 删除结果
     */
    Result<Void> deleteDriver(Long id);

    /**
     * 创建路线
     *
     * @param route 路线数据
     * @return 创建后的路线
     */
    Result<LesRouteEntity> createRoute(LesRouteEntity route);

    /**
     * 更新路线
     *
     * @param id    路线ID
     * @param route 更新数据
     * @return 更新后的路线
     */
    Result<LesRouteEntity> updateRoute(Long id, LesRouteEntity route);

    /**
     * 删除路线
     *
     * @param id 路线ID
     * @return 删除结果
     */
    Result<Void> deleteRoute(Long id);
}
