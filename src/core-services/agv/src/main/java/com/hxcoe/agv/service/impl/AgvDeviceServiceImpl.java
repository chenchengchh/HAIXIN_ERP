package com.hxcoe.agv.service.impl;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.agv.entity.AgvDeviceEntity;
import com.hxcoe.agv.repository.AgvDeviceRepository;
import com.hxcoe.agv.service.AgvDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

@Service
public class AgvDeviceServiceImpl implements AgvDeviceService {

    private static final String DEVICE_ID_CANNOT_BE_NULL = "设备ID不能为空";
    private static final String AGV_DEVICE_NOT_EXIST = "AGV设备不存在";
    
    private final AgvDeviceRepository deviceRepository;

    @Autowired
    public AgvDeviceServiceImpl(AgvDeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Result<AgvDeviceEntity> createDevice(AgvDeviceEntity device) {
        device.setCreatedTime(LocalDateTime.now());
        AgvDeviceEntity savedDevice = deviceRepository.save(device);
        return Result.success(savedDevice);
    }

    @Override
    public Result<AgvDeviceEntity> updateDevice(Long id, AgvDeviceEntity device) {
        if (id == null) {
            return Result.error(DEVICE_ID_CANNOT_BE_NULL);
        }
        AgvDeviceEntity existingDevice = deviceRepository.findById(id).orElse(null);
        if (existingDevice == null) {
            return Result.error(AGV_DEVICE_NOT_EXIST);
        }
        device.setId(id);
        device.setUpdatedTime(LocalDateTime.now());
        AgvDeviceEntity updatedDevice = deviceRepository.save(device);
        return Result.success(updatedDevice);
    }

    @Override
    public Result<Void> deleteDevice(Long id) {
        if (id == null) {
            return Result.error(DEVICE_ID_CANNOT_BE_NULL);
        }
        if (!deviceRepository.existsById(id)) {
            return Result.error("AGV设备不存在");
        }
        deviceRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<AgvDeviceEntity> getDeviceById(Long id) {
        if (id == null) {
            return Result.error(DEVICE_ID_CANNOT_BE_NULL);
        }
        AgvDeviceEntity device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            return Result.error("AGV设备不存在");
        }
        return Result.success(device);
    }

    @Override
    public Result<PageResult<AgvDeviceEntity>> getDevicesByPage(@NonNull Pageable pageable) {
        Page<AgvDeviceEntity> page = deviceRepository.findAll(pageable);
        PageResult<AgvDeviceEntity> pageResult = PageResult.build(
            page.getTotalElements(), 
            Math.toIntExact(page.getSize()), 
            page.getNumber() + 1, 
            page.getContent()
        );
        return Result.success(pageResult);
    }
}