package com.hxcoe.les.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesVehicleLocationDto;
import com.hxcoe.les.entity.LesAnomalyEventEntity;
import com.hxcoe.les.entity.LesMonitorLogEntity;
import com.hxcoe.les.entity.LesVehicleEntity;
import com.hxcoe.les.entity.LesVehicleLocationEntity;
import com.hxcoe.les.repository.LesAnomalyEventRepository;
import com.hxcoe.les.repository.LesMonitorLogRepository;
import com.hxcoe.les.repository.LesVehicleLocationRepository;
import com.hxcoe.les.repository.LesVehicleRepository;
import com.hxcoe.les.service.LesMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LesMonitoringServiceImpl implements LesMonitoringService {

    @Autowired
    private LesMonitorLogRepository monitorLogRepository;

    @Autowired
    private LesAnomalyEventRepository anomalyEventRepository;

    @Autowired
    private LesVehicleLocationRepository vehicleLocationRepository;

    @Autowired
    private LesVehicleRepository vehicleRepository;

    @Override
    public Result<PageResult<LesMonitorLogEntity>> fetchMonitorLogs(int page, int size, Long planId, Long vehicleId) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "recordTime"));
        Page<LesMonitorLogEntity> result;
        if (planId != null && vehicleId != null) {
            result = monitorLogRepository.findByPlanIdAndVehicleId(planId, vehicleId, pageable);
        } else if (planId != null) {
            result = monitorLogRepository.findByPlanId(planId, pageable);
        } else if (vehicleId != null) {
            result = monitorLogRepository.findByVehicleId(vehicleId, pageable);
        } else {
            result = monitorLogRepository.findAll(pageable);
        }
        PageResult<LesMonitorLogEntity> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, result.getContent());
        return Result.success("在途监控日志查询成功", pageResult);
    }

    @Override
    public Result<PageResult<LesMonitorLogEntity>> fetchPlanMonitorLogs(Long planId, int page, int size) {
        return fetchMonitorLogs(page, size, planId, null);
    }

    @Override
    public Result<PageResult<LesAnomalyEventEntity>> fetchAnomalyEvents(int page, int size, Long planId, Long vehicleId, String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "eventTime"));
        Page<LesAnomalyEventEntity> result;
        boolean hasStatus = status != null && !status.trim().isEmpty();
        if (planId != null && hasStatus) {
            result = anomalyEventRepository.findByPlanIdAndHandlingStatus(planId, status.trim(), pageable);
        } else if (vehicleId != null && hasStatus) {
            result = anomalyEventRepository.findByVehicleIdAndHandlingStatus(vehicleId, status.trim(), pageable);
        } else if (planId != null) {
            result = anomalyEventRepository.findByPlanId(planId, pageable);
        } else if (vehicleId != null) {
            result = anomalyEventRepository.findByVehicleId(vehicleId, pageable);
        } else if (hasStatus) {
            result = anomalyEventRepository.findByHandlingStatus(status.trim(), pageable);
        } else {
            result = anomalyEventRepository.findAll(pageable);
        }
        PageResult<LesAnomalyEventEntity> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, result.getContent());
        return Result.success("异常事件查询成功", pageResult);
    }

    @Override
    public Result<LesAnomalyEventEntity> handleAnomalyEvent(Long id, String handlingStatus, String handlingResult) {
        LesAnomalyEventEntity entity = anomalyEventRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.fail("异常事件不存在");
        }
        if (handlingStatus != null && !handlingStatus.trim().isEmpty()) {
            entity.setHandlingStatus(handlingStatus.trim());
        }
        if (handlingResult != null) {
            entity.setHandlingResult(handlingResult);
        }
        LesAnomalyEventEntity saved = anomalyEventRepository.save(entity);
        return Result.success("异常事件处理成功", saved);
    }

    @Override
    public Result<List<LesVehicleLocationDto>> fetchVehicleLocations(List<Long> vehicleIds) {
        List<Long> ids = vehicleIds == null ? List.of() : vehicleIds.stream().filter(v -> v != null).toList();
        if (ids.isEmpty()) {
            ids = vehicleRepository.findAll().stream().map(LesVehicleEntity::getId).filter(v -> v != null).toList();
        }
        List<LesVehicleLocationDto> result = new ArrayList<>();
        for (Long vehicleId : ids) {
            Optional<LesVehicleLocationEntity> latest = vehicleLocationRepository.findTopByVehicleIdOrderByRecordTimeDesc(vehicleId);
            latest.ifPresent(entity -> result.add(toDto(entity)));
        }
        return Result.success("车辆位置查询成功", result);
    }

    @Override
    public Result<List<LesVehicleLocationDto>> fetchVehicleHistory(Long vehicleId, String startTime, String endTime) {
        if (vehicleId == null) {
            return Result.fail("vehicleId不能为空");
        }
        LocalDateTime end = parseDateTimeOrDefault(endTime, LocalDateTime.now());
        LocalDateTime start = parseDateTimeOrDefault(startTime, end.minusDays(7));
        List<LesVehicleLocationEntity> points = vehicleLocationRepository.findByVehicleIdAndRecordTimeBetweenOrderByRecordTimeAsc(vehicleId, start, end);
        List<LesVehicleLocationDto> result = points.stream().map(this::toDto).toList();
        return Result.success("车辆历史轨迹查询成功", result);
    }

    @Override
    public Result<List<LesVehicleLocationDto>> fetchPlanTrack(Long planId, String startTime, String endTime) {
        if (planId == null) {
            return Result.fail("planId不能为空");
        }
        LocalDateTime end = parseDateTimeOrDefault(endTime, LocalDateTime.now());
        LocalDateTime start = parseDateTimeOrDefault(startTime, end.minusDays(7));
        List<LesVehicleLocationEntity> points = vehicleLocationRepository.findByPlanIdAndRecordTimeBetweenOrderByRecordTimeAsc(planId, start, end);
        List<LesVehicleLocationDto> result = points.stream().map(this::toDto).toList();
        return Result.success("计划轨迹查询成功", result);
    }

    private LesVehicleLocationDto toDto(LesVehicleLocationEntity entity) {
        LesVehicleLocationDto dto = new LesVehicleLocationDto();
        dto.setVehicleId(entity.getVehicleId());
        dto.setLicensePlate(entity.getLicensePlate());
        dto.setLongitude(entity.getLongitude());
        dto.setLatitude(entity.getLatitude());
        dto.setSpeed(entity.getSpeed());
        dto.setDirection(entity.getDirection());
        dto.setTimestamp(formatDateTime(entity.getRecordTime()));
        return dto;
    }

    private static String formatDateTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static LocalDateTime parseDateTimeOrDefault(String text, LocalDateTime defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        String t = text.trim();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ISO_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(t, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return defaultValue;
    }
}

