package com.hxcoe.ems.service;

import com.hxcoe.ems.entity.MeterDeviceEntity;
import com.hxcoe.ems.repository.MeterDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 采集设备服务类
 * 实现采集设备相关业务逻辑
 *
 * @author author
 * @date 2026-01-01
 */
@Service
public class MeterDeviceService {

    private final MeterDeviceRepository meterDeviceRepository;

    @Autowired
    public MeterDeviceService(MeterDeviceRepository meterDeviceRepository) {
        this.meterDeviceRepository = meterDeviceRepository;
    }

    /**
     * 获取所有采集设备
     *
     * @return 采集设备列表
     */
    public List<MeterDeviceEntity> getAllMeterDevices() {
        return meterDeviceRepository.findAll();
    }

    /**
     * 根据条件获取采集设备列表
     *
     * @param type   设备类型
     * @param status 状态
     * @return 采集设备列表
     */
    public List<MeterDeviceEntity> getMeterDevicesByConditions(String type, String status) {
        if (type != null && status != null) {
            return meterDeviceRepository.findByTypeAndStatus(type, status);
        } else if (type != null) {
            return meterDeviceRepository.findByType(type);
        } else if (status != null) {
            return meterDeviceRepository.findByStatus(status);
        } else {
            return getAllMeterDevices();
        }
    }

    public Page<MeterDeviceEntity> getMeterDevicesPage(String type, String status, Pageable pageable) {
        if (type != null && !type.isBlank() && status != null && !status.isBlank()) {
            return meterDeviceRepository.findByTypeAndStatus(type, status, pageable);
        } else if (type != null && !type.isBlank()) {
            return meterDeviceRepository.findByType(type, pageable);
        } else if (status != null && !status.isBlank()) {
            return meterDeviceRepository.findByStatus(status, pageable);
        } else {
            return meterDeviceRepository.findAll(pageable);
        }
    }

    /**
     * 根据ID获取采集设备
     *
     * @param id 设备ID
     * @return 采集设备
     */
    public Optional<MeterDeviceEntity> getMeterDeviceById(Long id) {
        return meterDeviceRepository.findById(id);
    }

    /**
     * 保存采集设备
     *
     * @param meterDevice 采集设备
     * @return 保存后的采集设备
     */
    public MeterDeviceEntity saveMeterDevice(MeterDeviceEntity meterDevice) {
        return meterDeviceRepository.save(meterDevice);
    }

    /**
     * 更新采集设备
     *
     * @param id          设备ID
     * @param meterDevice 更新的设备信息
     * @return 更新后的采集设备
     */
    public Optional<MeterDeviceEntity> updateMeterDevice(Long id, MeterDeviceEntity meterDevice) {
        return meterDeviceRepository.findById(id)
                .map(existingDevice -> {
                    // 更新设备信息
                    existingDevice.setName(meterDevice.getName());
                    existingDevice.setType(meterDevice.getType());
                    existingDevice.setIpAddress(meterDevice.getIpAddress());
                    existingDevice.setStatus(meterDevice.getStatus());
                    return meterDeviceRepository.save(existingDevice);
                });
    }

    /**
     * 删除采集设备
     *
     * @param id 设备ID
     */
    public void deleteMeterDevice(Long id) {
        meterDeviceRepository.deleteById(id);
    }

    /**
     * 更新设备状态
     *
     * @param id     设备ID
     * @param status 新状态
     * @return 更新后的设备
     */
    public Optional<MeterDeviceEntity> updateDeviceStatus(Long id, String status) {
        return meterDeviceRepository.findById(id)
                .map(device -> {
                    device.setStatus(status);
                    device.setLastUpdate(LocalDateTime.now());
                    return meterDeviceRepository.save(device);
                });
    }
}
