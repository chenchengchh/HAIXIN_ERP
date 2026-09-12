package com.hxcoe.agv.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.agv.entity.AgvDeviceEntity;
import org.springframework.data.domain.Pageable;

public interface AgvDeviceService {
    Result<AgvDeviceEntity> createDevice(AgvDeviceEntity device);
    Result<AgvDeviceEntity> updateDevice(Long id, AgvDeviceEntity device);
    Result<Void> deleteDevice(Long id);
    Result<AgvDeviceEntity> getDeviceById(Long id);
    Result<PageResult<AgvDeviceEntity>> getDevicesByPage(Pageable pageable);
}