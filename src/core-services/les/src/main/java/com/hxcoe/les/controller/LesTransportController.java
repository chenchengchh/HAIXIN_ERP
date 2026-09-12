package com.hxcoe.les.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesSalesOrderDto;
import com.hxcoe.les.entity.LesDriverEntity;
import com.hxcoe.les.entity.LesRouteEntity;
import com.hxcoe.les.entity.LesTransportPlanEntity;
import com.hxcoe.les.entity.LesTransportTaskEntity;
import com.hxcoe.les.entity.LesVehicleEntity;
import com.hxcoe.les.service.LesTransportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/les/transport", "/les/transport"})
public class LesTransportController {

    @Autowired
    private LesTransportService transportService;

    /**
     * 获取运输计划列表
     */
    @GetMapping("/plans")
    public Result<PageResult<LesTransportPlanEntity>> fetchTransportPlans(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        return transportService.fetchTransportPlans(page, size, keyword, status);
    }

    /**
     * 获取运输计划详情
     */
    @GetMapping("/plans/{id}")
    public Result<LesTransportPlanEntity> getTransportPlanDetail(@PathVariable("id") Long id) {
        return transportService.getTransportPlanDetail(id);
    }

    /**
     * 创建运输计划
     */
    @PostMapping("/plans")
    public Result<LesTransportPlanEntity> createTransportPlan(@RequestBody LesTransportPlanEntity plan) {
        return transportService.createTransportPlan(plan);
    }

    /**
     * 更新运输计划
     */
    @PutMapping("/plans/{id}")
    public Result<LesTransportPlanEntity> updateTransportPlan(@PathVariable("id") Long id, @RequestBody LesTransportPlanEntity plan) {
        return transportService.updateTransportPlan(id, plan);
    }

    /**
     * 删除运输计划
     */
    @DeleteMapping("/plans/{id}")
    public Result<Void> deleteTransportPlan(@PathVariable("id") Long id) {
        return transportService.deleteTransportPlan(id);
    }

    /**
     * 获取车辆列表
     */
    @GetMapping("/vehicles")
    public Result<PageResult<LesVehicleEntity>> fetchVehicles(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status
    ) {
        return transportService.fetchVehicles(page, size, keyword, status);
    }

    /**
     * 创建车辆
     */
    @PostMapping("/vehicles")
    public Result<LesVehicleEntity> createVehicle(@RequestBody LesVehicleEntity vehicle) {
        return transportService.createVehicle(vehicle);
    }

    /**
     * 更新车辆
     */
    @PutMapping("/vehicles/{id}")
    public Result<LesVehicleEntity> updateVehicle(@PathVariable("id") Long id, @RequestBody LesVehicleEntity vehicle) {
        return transportService.updateVehicle(id, vehicle);
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/vehicles/{id}")
    public Result<Void> deleteVehicle(@PathVariable("id") Long id) {
        return transportService.deleteVehicle(id);
    }

    /**
     * 获取司机列表
     */
    @GetMapping("/drivers")
    public Result<PageResult<LesDriverEntity>> fetchDrivers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status
    ) {
        return transportService.fetchDrivers(page, size, keyword, status);
    }

    /**
     * 创建司机
     */
    @PostMapping("/drivers")
    public Result<LesDriverEntity> createDriver(@RequestBody LesDriverEntity driver) {
        return transportService.createDriver(driver);
    }

    /**
     * 更新司机
     */
    @PutMapping("/drivers/{id}")
    public Result<LesDriverEntity> updateDriver(@PathVariable("id") Long id, @RequestBody LesDriverEntity driver) {
        return transportService.updateDriver(id, driver);
    }

    /**
     * 删除司机
     */
    @DeleteMapping("/drivers/{id}")
    public Result<Void> deleteDriver(@PathVariable("id") Long id) {
        return transportService.deleteDriver(id);
    }

    /**
     * 获取路线列表
     */
    @GetMapping("/routes")
    public Result<PageResult<LesRouteEntity>> fetchRoutes(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return transportService.fetchRoutes(page, size, keyword);
    }

    /**
     * 创建路线
     */
    @PostMapping("/routes")
    public Result<LesRouteEntity> createRoute(@RequestBody LesRouteEntity route) {
        return transportService.createRoute(route);
    }

    /**
     * 更新路线
     */
    @PutMapping("/routes/{id}")
    public Result<LesRouteEntity> updateRoute(@PathVariable("id") Long id, @RequestBody LesRouteEntity route) {
        return transportService.updateRoute(id, route);
    }

    /**
     * 删除路线
     */
    @DeleteMapping("/routes/{id}")
    public Result<Void> deleteRoute(@PathVariable("id") Long id) {
        return transportService.deleteRoute(id);
    }

    /**
     * 获取销售订单列表
     */
    @GetMapping("/sales-orders")
    public Result<PageResult<LesSalesOrderDto>> fetchSalesOrders(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return transportService.fetchSalesOrders(page, size, keyword);
    }

    /**
     * 创建运输任务
     */
    @PostMapping("/tasks")
    public Result<LesTransportTaskEntity> createTransportTask(@RequestBody LesTransportTaskEntity task) {
        return transportService.createTransportTask(task);
    }

    /**
     * 获取运输任务列表
     */
    @GetMapping("/tasks")
    public Result<PageResult<LesTransportTaskEntity>> fetchTransportTasks(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "planId", required = false) Long planId,
            @RequestParam(value = "status", required = false) String status
    ) {
        return transportService.fetchTransportTasks(page, size, planId, status);
    }
}

