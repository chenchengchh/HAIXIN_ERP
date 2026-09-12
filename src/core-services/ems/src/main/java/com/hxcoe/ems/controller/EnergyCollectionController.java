package com.hxcoe.ems.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.ems.dto.CalibrationRequest;
import com.hxcoe.ems.entity.CalibrationHistoryEntity;
import com.hxcoe.ems.entity.MeterDeviceEntity;
import com.hxcoe.ems.entity.RealTimeDataEntity;
import com.hxcoe.ems.service.CalibrationHistoryService;
import com.hxcoe.ems.service.MeterDeviceService;
import com.hxcoe.ems.service.RealTimeDataService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 能源采集控制器
 * 处理实时数据采集相关的API请求
 *
 * @author author
 * @date 2026-01-01
 */
@RestController
@RequestMapping("/api/v1/ems/collection")
@Validated
public class EnergyCollectionController {

    private final RealTimeDataService realTimeDataService;
    private final MeterDeviceService meterDeviceService;
    private final CalibrationHistoryService calibrationHistoryService;

    @Autowired
    public EnergyCollectionController(RealTimeDataService realTimeDataService, 
                                     MeterDeviceService meterDeviceService,
                                     CalibrationHistoryService calibrationHistoryService) {
        this.realTimeDataService = realTimeDataService;
        this.meterDeviceService = meterDeviceService;
        this.calibrationHistoryService = calibrationHistoryService;
    }

    /**
     * 获取实时数据采集列表
     * GET /api/ems/collection/real-time
     *
     * @param energyType 能源类型
     * @param area       区域
     * @param limit      数量限制
     * @return 实时数据列表
     */
    @GetMapping("/real-time")
    public Result<List<RealTimeDataEntity>> getRealTimeData(
            @RequestParam(required = false) String energyType,
            @RequestParam(required = false) String area,
            @RequestParam(required = false, defaultValue = "50") @Min(1) @Max(500) Integer limit) {
        
        List<RealTimeDataEntity> realTimeData = realTimeDataService.getRealTimeDataByConditions(energyType, area, limit);
        for (RealTimeDataEntity item : realTimeData) {
            normalizeRealTimeData(item);
        }
        return Result.success(realTimeData);
    }

    @GetMapping("/real-time/page")
    public Result<Map<String, Object>> getRealTimeDataPage(
            @RequestParam(required = false) String energyType,
            @RequestParam(required = false) String area,
            @RequestParam(required = false, defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false, defaultValue = "50") @Min(1) @Max(500) int size
    ) {
        Page<RealTimeDataEntity> p = realTimeDataService.getRealTimeDataPage(
                energyType,
                area,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "collectionTime"))
        );
        for (RealTimeDataEntity item : p.getContent()) {
            normalizeRealTimeData(item);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", p.getTotalElements());
        data.put("list", p.getContent());
        data.put("page", p.getNumber());
        data.put("size", p.getSize());
        return Result.success(data);
    }

    /**
     * 根据ID获取实时数据
     * GET /api/ems/collection/real-time/{id}
     *
     * @param id 数据ID
     * @return 实时数据
     */
    @GetMapping("/real-time/{id}")
    public Result<RealTimeDataEntity> getRealTimeDataById(@PathVariable Long id) {
        RealTimeDataEntity data = realTimeDataService.getRealTimeDataById(id).orElse(null);
        if (data == null) {
            return Result.error(404, "实时数据不存在");
        }
        normalizeRealTimeData(data);
        return Result.success(data);
    }

    /**
     * 保存实时数据
     * POST /api/ems/collection/real-time
     *
     * @param realTimeData 实时数据
     * @return 保存后的实时数据
     */
    @PostMapping("/real-time")
    public Result<RealTimeDataEntity> saveRealTimeData(@RequestBody RealTimeDataEntity realTimeData) {
        RealTimeDataEntity savedData = realTimeDataService.saveRealTimeData(realTimeData);
        return Result.success("保存成功", savedData);
    }

    /**
     * 批量保存实时数据
     * POST /api/ems/collection/real-time/batch
     *
     * @param realTimeDataList 实时数据列表
     * @return 保存后的实时数据列表
     */
    @PostMapping("/real-time/batch")
    public Result<List<RealTimeDataEntity>> saveAllRealTimeData(@RequestBody List<RealTimeDataEntity> realTimeDataList) {
        List<RealTimeDataEntity> savedDataList = realTimeDataService.saveAllRealTimeData(realTimeDataList);
        return Result.success("保存成功", savedDataList);
    }

    /**
     * 删除实时数据
     * DELETE /api/ems/collection/real-time/{id}
     *
     * @param id 数据ID
     * @return 响应结果
     */
    @DeleteMapping("/real-time/{id}")
    public Result<Void> deleteRealTimeData(@PathVariable Long id) {
        realTimeDataService.deleteRealTimeData(id);
        return Result.success("删除成功");
    }

    /**
     * 获取采集设备列表
     * GET /api/ems/collection/devices
     *
     * @param type   设备类型
     * @param status 状态
     * @return 采集设备列表
     */
    @GetMapping("/devices")
    public Result<List<MeterDeviceEntity>> getMeterDevices(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        List<MeterDeviceEntity> devices = meterDeviceService.getMeterDevicesByConditions(type, status);
        for (MeterDeviceEntity d : devices) {
            normalizeMeterDevice(d);
        }
        return Result.success(devices);
    }

    @GetMapping("/devices/page")
    public Result<Map<String, Object>> getMeterDevicesPage(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(200) int size
    ) {
        Page<MeterDeviceEntity> p = meterDeviceService.getMeterDevicesPage(
                type,
                status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        );
        for (MeterDeviceEntity d : p.getContent()) {
            normalizeMeterDevice(d);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", p.getTotalElements());
        data.put("list", p.getContent());
        data.put("page", p.getNumber());
        data.put("size", p.getSize());
        return Result.success(data);
    }

    /**
     * 根据ID获取采集设备
     * GET /api/ems/collection/devices/{id}
     *
     * @param id 设备ID
     * @return 采集设备
     */
    @GetMapping("/devices/{id}")
    public Result<MeterDeviceEntity> getMeterDeviceById(@PathVariable Long id) {
        MeterDeviceEntity device = meterDeviceService.getMeterDeviceById(id).orElse(null);
        if (device == null) {
            return Result.error(404, "采集设备不存在");
        }
        normalizeMeterDevice(device);
        return Result.success(device);
    }

    /**
     * 添加采集设备
     * POST /api/ems/collection/devices
     *
     * @param meterDevice 采集设备
     * @return 添加后的采集设备
     */
    @PostMapping("/devices")
    public Result<MeterDeviceEntity> addMeterDevice(@RequestBody MeterDeviceEntity meterDevice) {
        MeterDeviceEntity savedDevice = meterDeviceService.saveMeterDevice(meterDevice);
        return Result.success("创建成功", savedDevice);
    }

    /**
     * 更新采集设备
     * PUT /api/ems/collection/devices/{id}
     *
     * @param id          设备ID
     * @param meterDevice 更新的设备信息
     * @return 更新后的采集设备
     */
    @PutMapping("/devices/{id}")
    public Result<MeterDeviceEntity> updateMeterDevice(@PathVariable Long id, @RequestBody MeterDeviceEntity meterDevice) {
        MeterDeviceEntity updated = meterDeviceService.updateMeterDevice(id, meterDevice).orElse(null);
        if (updated == null) {
            return Result.error(404, "采集设备不存在");
        }
        return Result.success("更新成功", updated);
    }

    /**
     * 删除采集设备
     * DELETE /api/ems/collection/devices/{id}
     *
     * @param id 设备ID
     * @return 响应结果
     */
    @DeleteMapping("/devices/{id}")
    public Result<Void> deleteMeterDevice(@PathVariable Long id) {
        meterDeviceService.deleteMeterDevice(id);
        return Result.success("删除成功");
    }

    /**
     * 执行数据校准
     * POST /api/ems/collection/calibrate
     *
     * @param calibrationData 校准数据
     * @return 校准结果
     */
    @PostMapping("/calibrate")
    public Result<CalibrationHistoryEntity> executeCalibration(@Valid @RequestBody CalibrationRequest calibrationData) {
        CalibrationHistoryEntity calibrationHistory = calibrationHistoryService.executeCalibration(
                calibrationData.getMeterId(),
                calibrationData.getMeterName(),
                calibrationData.getRawValue(),
                calibrationData.getCoefficient(),
                calibrationData.getReason()
        );
        return Result.success("校准成功", calibrationHistory);
    }

    private void normalizeRealTimeData(RealTimeDataEntity entity) {
        if (entity == null) return;
        entity.setEnergyType(displayEnergyType(entity.getEnergyType()));
    }

    private void normalizeMeterDevice(MeterDeviceEntity entity) {
        if (entity == null) return;
        entity.setType(displayDeviceType(entity.getType()));
    }

    private String displayEnergyType(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isBlank()) return v;
        String lower = v.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "electric", "electricity", "电能", "电力" -> "电力";
            case "water", "水", "水资源" -> "水资源";
            case "gas", "燃气" -> "燃气";
            case "heat", "蒸汽", "热能" -> "热能";
            default -> v;
        };
    }

    private String displayDeviceType(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isBlank()) return v;
        String lower = v.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "electric", "electricity", "电表" -> "电表";
            case "water", "水表" -> "水表";
            case "gas", "气表" -> "气表";
            case "heat", "热表" -> "热表";
            default -> v;
        };
    }

    /**
     * 获取校准历史记录
     * GET /api/ems/collection/calibration-history
     *
     * @param meterId 设备ID
     * @return 校准历史记录列表
     */
    @GetMapping("/calibration-history")
    public Result<List<CalibrationHistoryEntity>> getCalibrationHistory(
            @RequestParam(required = false) Long meterId) {
        List<CalibrationHistoryEntity> history = calibrationHistoryService.getCalibrationHistoryByConditions(
                meterId, null, null
        );
        return Result.success(history);
    }

}
