package com.hxcoe.ems.service;

import com.hxcoe.ems.entity.EnergyAnomalyEntity;
import com.hxcoe.ems.repository.EnergyAnomalyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 能耗异常服务类
 * 实现能耗异常相关业务逻辑
 *
 * @author author
 * @date 2026-01-01
 */
@Service
public class EnergyAnomalyService {

    private final EnergyAnomalyRepository energyAnomalyRepository;
    private static final Logger logger = LoggerFactory.getLogger(EnergyAnomalyService.class);

    @Autowired
    public EnergyAnomalyService(EnergyAnomalyRepository energyAnomalyRepository) {
        this.energyAnomalyRepository = energyAnomalyRepository;
    }

    /**
     * 获取所有能耗异常
     *
     * @return 能耗异常列表
     */
    public List<EnergyAnomalyEntity> getAllEnergyAnomalies() {
        return energyAnomalyRepository.findAll();
    }

    /**
     * 根据ID获取能耗异常
     *
     * @param id 异常ID
     * @return 能耗异常
     */
    public Optional<EnergyAnomalyEntity> getEnergyAnomalyById(Long id) {
        return energyAnomalyRepository.findById(id);
    }

    /**
     * 保存能耗异常
     *
     * @param energyAnomaly 能耗异常
     * @return 保存后的能耗异常
     */
    public EnergyAnomalyEntity saveEnergyAnomaly(EnergyAnomalyEntity energyAnomaly) {
        return energyAnomalyRepository.save(energyAnomaly);
    }

    /**
     * 批量保存能耗异常
     *
     * @param energyAnomalyList 能耗异常列表
     * @return 保存后的能耗异常列表
     */
    public List<EnergyAnomalyEntity> saveAllEnergyAnomalies(List<EnergyAnomalyEntity> energyAnomalyList) {
        return energyAnomalyRepository.saveAll(energyAnomalyList);
    }

    /**
     * 根据条件查询能耗异常
     *
     * @param energyType 能源类型
     * @param area       区域
     * @param status     状态
     * @return 能耗异常列表
     */
    public List<EnergyAnomalyEntity> getEnergyAnomaliesByConditions(String energyType, String area, String status) {
        String e = energyType == null || energyType.isBlank() ? null : energyType;
        String a = area == null || area.isBlank() ? null : area;
        String s = status == null || status.isBlank() ? null : status;

        if (e != null && a != null) {
            List<EnergyAnomalyEntity> base = energyAnomalyRepository.findByEnergyTypeAndArea(e, a);
            if (s == null) return base;
            return base.stream().filter(x -> s.equals(x.getStatus())).toList();
        }
        if (e != null) {
            List<EnergyAnomalyEntity> base = energyAnomalyRepository.findByEnergyType(e);
            if (s == null) return base;
            return base.stream().filter(x -> s.equals(x.getStatus())).toList();
        }
        if (a != null) {
            List<EnergyAnomalyEntity> base = energyAnomalyRepository.findByArea(a);
            if (s == null) return base;
            return base.stream().filter(x -> s.equals(x.getStatus())).toList();
        }
        if (s != null) {
            return energyAnomalyRepository.findByStatus(s);
        }
        return energyAnomalyRepository.findAll();
    }

    public Page<EnergyAnomalyEntity> getEnergyAnomaliesPage(String energyType, String area, String status, Pageable pageable) {
        String e = energyType == null || energyType.isBlank() ? null : energyType;
        String a = area == null || area.isBlank() ? null : area;
        String s = status == null || status.isBlank() ? null : status;

        if (e != null && a != null) {
            if (s == null) return energyAnomalyRepository.findByEnergyTypeAndArea(e, a, pageable);
            return energyAnomalyRepository.findByEnergyTypeAndAreaAndStatus(e, a, s, pageable);
        }
        if (e != null) {
            if (s == null) return energyAnomalyRepository.findByEnergyType(e, pageable);
            return energyAnomalyRepository.findByEnergyTypeAndStatus(e, s, pageable);
        }
        if (a != null) {
            if (s == null) return energyAnomalyRepository.findByArea(a, pageable);
            return energyAnomalyRepository.findByAreaAndStatus(a, s, pageable);
        }
        if (s != null) {
            return energyAnomalyRepository.findByStatus(s, pageable);
        }
        return energyAnomalyRepository.findAll(pageable);
    }

    /**
     * 处理能耗异常
     *
     * @param id 异常ID
     * @return 处理后的能耗异常
     */
    public Optional<EnergyAnomalyEntity> processEnergyAnomaly(Long id) {
        Optional<EnergyAnomalyEntity> anomalyOpt = energyAnomalyRepository.findById(id);
        if (anomalyOpt.isPresent()) {
            EnergyAnomalyEntity anomaly = anomalyOpt.get();
            anomaly.setStatus("processed");
            anomaly.setProcessedTime(LocalDateTime.now());
            return Optional.of(energyAnomalyRepository.save(anomaly));
        }
        return Optional.empty();
    }

    /**
     * 处理能耗异常（带处理信息）
     *
     * @param id             异常ID
     * @param processedBy    处理人
     * @param processedRemark 处理备注
     * @return 处理后的能耗异常
     */
    public Optional<EnergyAnomalyEntity> processEnergyAnomaly(Long id, String processedBy, String processedRemark) {
        Optional<EnergyAnomalyEntity> anomalyOpt = energyAnomalyRepository.findById(id);
        if (anomalyOpt.isPresent()) {
            EnergyAnomalyEntity anomaly = anomalyOpt.get();
            anomaly.setStatus("processed");
            anomaly.setProcessedTime(LocalDateTime.now());
            anomaly.setProcessedBy(processedBy);
            anomaly.setProcessedRemark(processedRemark);
            return Optional.of(energyAnomalyRepository.save(anomaly));
        }
        return Optional.empty();
    }

    /**
     * 将所有异常标记为已处理
     *
     * @return 处理结果
     */
    public void markAllAnomaliesAsProcessed() {
        List<EnergyAnomalyEntity> pendingAnomalies = energyAnomalyRepository.findByStatus("pending");
        LocalDateTime now = LocalDateTime.now();
        
        pendingAnomalies.forEach(anomaly -> {
            anomaly.setStatus("processed");
            anomaly.setProcessedTime(now);
        });
        
        energyAnomalyRepository.saveAll(pendingAnomalies);
    }

    /**
     * 删除能耗异常
     *
     * @param id 异常ID
     */
    public void deleteEnergyAnomaly(Long id) {
        energyAnomalyRepository.deleteById(id);
    }

    /**
     * 统计待处理异常数量
     *
     * @return 待处理异常数量
     */
    public long countPendingAnomalies() {
        return energyAnomalyRepository.countByStatus("pending");
    }

    /**
     * 获取指定时间范围内的能耗异常
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 能耗异常列表
     */
    public List<EnergyAnomalyEntity> getAnomaliesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return energyAnomalyRepository.findByDetectionTimeBetween(startTime, endTime);
    }
}
