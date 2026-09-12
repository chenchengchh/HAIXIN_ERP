package com.hxcoe.scada.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.entity.ScadaDeviceEntity;
import org.springframework.data.domain.Pageable;

public interface ScadaDeviceService {
    ApiResponse<ScadaDeviceEntity> createDevice(ScadaDeviceEntity device);
    ApiResponse<ScadaDeviceEntity> updateDevice(Long id, ScadaDeviceEntity device);
    ApiResponse<Void> deleteDevice(Long id);
    ApiResponse<ScadaDeviceEntity> getDeviceById(Long id);
    ApiResponse<PageResult<ScadaDeviceEntity>> getDevicesByPage(Pageable pageable);
}
