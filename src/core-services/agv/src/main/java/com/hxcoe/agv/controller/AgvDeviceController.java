package com.hxcoe.agv.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.agv.entity.AgvDeviceEntity;
import com.hxcoe.agv.service.AgvDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/agv/devices", "/api/v1/agv/devices"})
public class AgvDeviceController {

    @Autowired
    private AgvDeviceService deviceService;

    @PostMapping
    public Result<AgvDeviceEntity> createDevice(@RequestBody AgvDeviceEntity device) {
        return deviceService.createDevice(device);
    }

    @PutMapping("/{id}")
    public Result<AgvDeviceEntity> updateDevice(@PathVariable Long id, @RequestBody AgvDeviceEntity device) {
        return deviceService.updateDevice(id, device);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDevice(@PathVariable Long id) {
        return deviceService.deleteDevice(id);
    }

    @GetMapping("/{id}")
    public Result<AgvDeviceEntity> getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id);
    }

    /**
     * 分页查询AGV设备列表（页码1基）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<AgvDeviceEntity>> getDevicesByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));
        return deviceService.getDevicesByPage(pageable);
    }
}