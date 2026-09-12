package com.hxcoe.scada.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scada.entity.ScadaDeviceEntity;
import com.hxcoe.scada.service.ScadaDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/scada/devices", "/api/v1/scada/devices"})
public class ScadaDeviceController {

    @Autowired
    private ScadaDeviceService deviceService;

    @PostMapping
    public ApiResponse<ScadaDeviceEntity> createDevice(@RequestBody ScadaDeviceEntity device) {
        return deviceService.createDevice(device);
    }

    @PutMapping("/{id}")
    public ApiResponse<ScadaDeviceEntity> updateDevice(@PathVariable Long id, @RequestBody ScadaDeviceEntity device) {
        return deviceService.updateDevice(id, device);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDevice(@PathVariable Long id) {
        return deviceService.deleteDevice(id);
    }

    @GetMapping("/{id}")
    public ApiResponse<ScadaDeviceEntity> getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id);
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<ScadaDeviceEntity>> getDevicesByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return deviceService.getDevicesByPage(pageable);
    }
}
