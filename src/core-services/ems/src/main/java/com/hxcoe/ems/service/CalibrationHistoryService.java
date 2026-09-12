package com.hxcoe.ems.service;

import com.hxcoe.ems.entity.CalibrationHistoryEntity;
import com.hxcoe.ems.repository.CalibrationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 校准历史服务类
 * 实现校准历史相关业务逻辑
 *
 * @author author
 * @date 2026-01-01
 */
@Service
public class CalibrationHistoryService {

    private final CalibrationHistoryRepository calibrationHistoryRepository;

    @Autowired
    public CalibrationHistoryService(CalibrationHistoryRepository calibrationHistoryRepository) {
        this.calibrationHistoryRepository = calibrationHistoryRepository;
    }

    /**
     * 获取所有校准历史记录
     *
     * @return 校准历史记录列表
     */
    public List<CalibrationHistoryEntity> getAllCalibrationHistory() {
        return calibrationHistoryRepository.findAll();
    }

    /**
     * 根据设备ID获取校准历史记录
     *
     * @param meterId 设备ID
     * @return 校准历史记录列表
     */
    public List<CalibrationHistoryEntity> getCalibrationHistoryByMeterId(Long meterId) {
        return calibrationHistoryRepository.findByMeterId(meterId);
    }

    /**
     * 根据条件获取校准历史记录
     *
     * @param meterId   设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 校准历史记录列表
     */
    public List<CalibrationHistoryEntity> getCalibrationHistoryByConditions(Long meterId, LocalDateTime startTime, LocalDateTime endTime) {
        if (meterId != null && startTime != null && endTime != null) {
            return calibrationHistoryRepository.findByMeterIdAndCalibrationTimeBetween(meterId, startTime, endTime);
        } else if (meterId != null) {
            return getCalibrationHistoryByMeterId(meterId);
        } else if (startTime != null && endTime != null) {
            return calibrationHistoryRepository.findByCalibrationTimeBetween(startTime, endTime);
        } else {
            return getAllCalibrationHistory();
        }
    }

    /**
     * 保存校准历史记录
     *
     * @param calibrationHistory 校准历史记录
     * @return 保存后的校准历史记录
     */
    public CalibrationHistoryEntity saveCalibrationHistory(CalibrationHistoryEntity calibrationHistory) {
        return calibrationHistoryRepository.save(calibrationHistory);
    }

    /**
     * 执行数据校准并保存校准历史
     *
     * @param meterId     设备ID
     * @param meterName   设备名称
     * @param rawValue    原始值
     * @param coefficient 校准系数
     * @param reason      校准原因
     * @return 保存后的校准历史记录
     */
    public CalibrationHistoryEntity executeCalibration(Long meterId, String meterName, Double rawValue, Double coefficient, String reason) {
        Double safeRawValue = rawValue == null ? 0.0 : rawValue;
        Double safeCoefficient = coefficient == null ? 1.0 : coefficient;
        CalibrationHistoryEntity calibrationHistory = new CalibrationHistoryEntity();
        calibrationHistory.setMeterId(meterId);
        calibrationHistory.setMeterName(meterName == null ? "" : meterName);
        calibrationHistory.setRawValue(safeRawValue);
        calibrationHistory.setCalibratedValue(safeRawValue * safeCoefficient);
        calibrationHistory.setCoefficient(safeCoefficient);
        calibrationHistory.setReason(reason);
        calibrationHistory.setCalibrationTime(LocalDateTime.now());
        return calibrationHistoryRepository.save(calibrationHistory);
    }
}
