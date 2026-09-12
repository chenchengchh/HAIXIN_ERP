package com.hxcoe.scada.service.impl;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scada.entity.ScadaDeviceEntity;
import com.hxcoe.scada.repository.ScadaDeviceRepository;
import com.hxcoe.scada.service.ScadaDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ScadaDeviceServiceImpl implements ScadaDeviceService {

    @Autowired
    private ScadaDeviceRepository deviceRepository;

    @Override
    public ApiResponse<ScadaDeviceEntity> createDevice(ScadaDeviceEntity device) {
        device.setCreatedTime(LocalDateTime.now());
        ScadaDeviceEntity savedDevice = deviceRepository.save(device);
        return ApiResponse.success("创建成功", savedDevice);
    }

    @Override
    public ApiResponse<ScadaDeviceEntity> updateDevice(Long id, ScadaDeviceEntity device) {
        ScadaDeviceEntity existingDevice = deviceRepository.findById(id).orElse(null);
        if (existingDevice == null) {
            return ApiResponse.error(404, "SCADA设备不存在");
        }
        device.setId(id);
        device.setCreatedTime(existingDevice.getCreatedTime());
        device.setUpdatedTime(LocalDateTime.now());
        ScadaDeviceEntity updatedDevice = deviceRepository.save(device);
        return ApiResponse.success("更新成功", updatedDevice);
    }

    @Override
    public ApiResponse<Void> deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            return ApiResponse.error(404, "SCADA设备不存在");
        }
        deviceRepository.deleteById(id);
        return ApiResponse.success(null);
    }

    @Override
    public ApiResponse<ScadaDeviceEntity> getDeviceById(Long id) {
        ScadaDeviceEntity device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            return ApiResponse.error(404, "SCADA设备不存在");
        }
        return ApiResponse.success(device);
    }

    @Override
    public ApiResponse<PageResult<ScadaDeviceEntity>> getDevicesByPage(Pageable pageable) {
        Page<ScadaDeviceEntity> page = deviceRepository.findAll(pageable);
        PageResult<ScadaDeviceEntity> pageResult = PageResult.build(
            page.getTotalElements(), 
            (int) page.getSize(), 
            (int) (page.getNumber() + 1), 
            page.getContent()
        );
        return ApiResponse.success(pageResult);
    }
}
