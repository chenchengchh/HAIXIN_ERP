package com.hxcoe.ems.service;

import com.hxcoe.ems.entity.RealTimeDataEntity;
import com.hxcoe.ems.repository.RealTimeDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 实时数据采集服务类
 * 实现实时数据采集相关业务逻辑
 *
 * @author author
 * @date 2026-01-01
 */
@Service
public class RealTimeDataService {

    private final RealTimeDataRepository realTimeDataRepository;
    private static final Logger logger = LoggerFactory.getLogger(RealTimeDataService.class);

    @Autowired
    public RealTimeDataService(RealTimeDataRepository realTimeDataRepository) {
        this.realTimeDataRepository = realTimeDataRepository;
    }

    /**
     * 获取所有实时数据
     *
     * @return 实时数据列表
     */
    public List<RealTimeDataEntity> getAllRealTimeData() {
        return realTimeDataRepository.findAll();
    }

    /**
     * 根据ID获取实时数据
     *
     * @param id 数据ID
     * @return 实时数据
     */
    public Optional<RealTimeDataEntity> getRealTimeDataById(Long id) {
        return realTimeDataRepository.findById(id);
    }

    /**
     * 保存实时数据
     *
     * @param realTimeData 实时数据
     * @return 保存后的实时数据
     */
    public RealTimeDataEntity saveRealTimeData(RealTimeDataEntity realTimeData) {
        return realTimeDataRepository.save(realTimeData);
    }

    /**
     * 批量保存实时数据
     *
     * @param realTimeDataList 实时数据列表
     * @return 保存后的实时数据列表
     */
    public List<RealTimeDataEntity> saveAllRealTimeData(List<RealTimeDataEntity> realTimeDataList) {
        return realTimeDataRepository.saveAll(realTimeDataList);
    }

    /**
     * 根据条件查询实时数据
     *
     * @param energyType 能源类型
     * @param area       区域
     * @param limit      数量限制
     * @return 实时数据列表
     */
    public List<RealTimeDataEntity> getRealTimeDataByConditions(String energyType, String area, Integer limit) {
        List<RealTimeDataEntity> result = new ArrayList<>();
        int actualLimit = limit != null ? limit : 50;

        try {
            // 获取所有数据
            List<RealTimeDataEntity> allData = realTimeDataRepository.findAll();
            
            if (allData != null && !allData.isEmpty()) {
                // 根据条件过滤
                if (energyType != null && area != null) {
                    result = allData.stream()
                            .filter(data -> data != null && energyType.equals(data.getEnergyType()) && area.equals(data.getArea()))
                            .toList();
                } else if (energyType != null) {
                    result = allData.stream()
                            .filter(data -> data != null && energyType.equals(data.getEnergyType()))
                            .toList();
                } else if (area != null) {
                    result = allData.stream()
                            .filter(data -> data != null && area.equals(data.getArea()))
                            .toList();
                } else {
                    result = allData;
                }
                
                // 按采集时间降序排序
                result.sort((a, b) -> {
                    if (a == null || b == null) return 0;
                    if (a.getCollectionTime() == null || b.getCollectionTime() == null) return 0;
                    return b.getCollectionTime().compareTo(a.getCollectionTime());
                });
                
                // 如果有数量限制，截取结果
                if (result.size() > actualLimit) {
                    result = result.subList(0, actualLimit);
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while getting real time data: {}", e.getMessage(), e);
            throw new RuntimeException("获取实时数据失败");
        }

        return result;
    }

    public Page<RealTimeDataEntity> getRealTimeDataPage(String energyType, String area, Pageable pageable) {
        if (energyType != null && !energyType.isBlank() && area != null && !area.isBlank()) {
            return realTimeDataRepository.findByEnergyTypeAndArea(energyType, area, pageable);
        }
        if (energyType != null && !energyType.isBlank()) {
            return realTimeDataRepository.findByEnergyType(energyType, pageable);
        }
        if (area != null && !area.isBlank()) {
            return realTimeDataRepository.findByArea(area, pageable);
        }
        return realTimeDataRepository.findAll(pageable);
    }

    /**
     * 获取最新的实时数据
     *
     * @param limit 数量限制
     * @return 实时数据列表
     */
    public List<RealTimeDataEntity> getLatestRealTimeData(int limit) {
        List<RealTimeDataEntity> allData = realTimeDataRepository.findAll();
        
        // 按采集时间降序排序
        allData.sort((a, b) -> b.getCollectionTime().compareTo(a.getCollectionTime()));
        
        // 截取结果
        if (allData.size() > limit) {
            return allData.subList(0, limit);
        }
        
        return allData;
    }

    /**
     * 获取指定时间范围内的实时数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 实时数据列表
     */
    public List<RealTimeDataEntity> getRealTimeDataByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return realTimeDataRepository.findByCollectionTimeBetween(startTime, endTime);
    }

    /**
     * 根据状态获取实时数据
     *
     * @param status 状态
     * @return 实时数据列表
     */
    public List<RealTimeDataEntity> getRealTimeDataByStatus(String status) {
        return realTimeDataRepository.findByStatus(status);
    }

    /**
     * 删除实时数据
     *
     * @param id 数据ID
     */
    public void deleteRealTimeData(Long id) {
        realTimeDataRepository.deleteById(id);
    }

    /**
     * 清理指定时间之前的历史数据
     *
     * @param cutoffTime 截止时间
     */
    public void cleanHistoricalData(LocalDateTime cutoffTime) {
        List<RealTimeDataEntity> oldData = realTimeDataRepository.findByCollectionTimeBetween(
                LocalDateTime.of(2000, 1, 1, 0, 0, 0),
                cutoffTime
        );
        realTimeDataRepository.deleteAll(oldData);
    }
}
