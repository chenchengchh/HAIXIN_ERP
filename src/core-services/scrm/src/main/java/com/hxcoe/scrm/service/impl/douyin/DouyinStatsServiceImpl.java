package com.hxcoe.scrm.service.impl.douyin;

import com.hxcoe.scrm.dto.douyin.DouyinStatsDTO;
import com.hxcoe.scrm.entity.douyin.DouyinStatsEntity;
import com.hxcoe.scrm.entity.douyin.DouyinTaskEntity;
import com.hxcoe.scrm.exception.douyin.DouyinException;
import com.hxcoe.scrm.repository.douyin.DouyinStatsRepository;
import com.hxcoe.scrm.repository.douyin.DouyinTaskRepository;
import com.hxcoe.scrm.service.douyin.DouyinStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抖音获客统计服务实现类
 */
@Slf4j
@Service
public class DouyinStatsServiceImpl implements DouyinStatsService {

    @Autowired
    private DouyinStatsRepository statsRepository;

    @Autowired
    private DouyinTaskRepository taskRepository;

    @Override
    public void updateStats(Long taskId, int videosFound, int customersFound, int messagesSent, int convertedCustomers, int failedCustomers) throws DouyinException {
        try {
            // 获取任务信息
            DouyinTaskEntity task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new DouyinException("任务不存在", "TASK_NOT_FOUND", 404));

            LocalDate today = LocalDate.now();
            
            // 查询今日统计数据
            DouyinStatsEntity stats = statsRepository.findByTaskIdAndStatsDate(taskId, today);
            
            if (stats == null) {
                // 创建新的统计数据
                stats = DouyinStatsEntity.builder()
                        .statsDate(today)
                        .taskId(taskId)
                        .taskName(task.getName())
                        .searchKeyword(task.getSearchKeyword())
                        .videosFound(videosFound)
                        .customersFound(customersFound)
                        .messagesSent(messagesSent)
                        .convertedCustomers(convertedCustomers)
                        .failedCustomers(failedCustomers)
                        .build();
            } else {
                // 更新现有统计数据
                stats.setVideosFound(stats.getVideosFound() + videosFound);
                stats.setCustomersFound(stats.getCustomersFound() + customersFound);
                stats.setMessagesSent(stats.getMessagesSent() + messagesSent);
                stats.setConvertedCustomers(stats.getConvertedCustomers() + convertedCustomers);
                stats.setFailedCustomers(stats.getFailedCustomers() + failedCustomers);
            }
            
            statsRepository.save(stats);
            log.info("更新抖音统计数据成功，任务ID: {}, 日期: {}", taskId, today);
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新抖音统计数据失败: {}", e.getMessage(), e);
            throw new DouyinException("更新统计数据失败", "STATS_UPDATE_FAILED", 500, e);
        }
    }

    @Override
    public List<DouyinStatsDTO> getTodayStats() throws DouyinException {
        try {
            List<DouyinStatsEntity> entities = statsRepository.findTodayStats();
            return convertToDTOList(entities);
        } catch (Exception e) {
            log.error("获取今日统计数据失败: {}", e.getMessage(), e);
            throw new DouyinException("获取今日统计数据失败", "STATS_GET_FAILED", 500, e);
        }
    }

    @Override
    public List<DouyinStatsDTO> getStatsByDate(LocalDate statsDate) throws DouyinException {
        try {
            List<DouyinStatsEntity> entities = statsRepository.findByStatsDate(statsDate);
            return convertToDTOList(entities);
        } catch (Exception e) {
            log.error("获取指定日期统计数据失败: {}", e.getMessage(), e);
            throw new DouyinException("获取指定日期统计数据失败", "STATS_GET_FAILED", 500, e);
        }
    }

    @Override
    public List<DouyinStatsDTO> getStatsByDateRange(LocalDate startDate, LocalDate endDate) throws DouyinException {
        try {
            List<DouyinStatsEntity> entities = statsRepository.findByStatsDateBetween(startDate, endDate);
            return convertToDTOList(entities);
        } catch (Exception e) {
            log.error("获取日期范围统计数据失败: {}", e.getMessage(), e);
            throw new DouyinException("获取日期范围统计数据失败", "STATS_GET_FAILED", 500, e);
        }
    }

    @Override
    public List<DouyinStatsDTO> getStatsByTaskId(Long taskId) throws DouyinException {
        try {
            // 验证任务是否存在
            taskRepository.findById(taskId)
                    .orElseThrow(() -> new DouyinException("任务不存在", "TASK_NOT_FOUND", 404));
            
            // 查询该任务的所有统计数据
            List<DouyinStatsEntity> entities = statsRepository.findByTaskId(taskId);
            return convertToDTOList(entities);
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取任务统计数据失败: {}", e.getMessage(), e);
            throw new DouyinException("获取任务统计数据失败", "STATS_GET_FAILED", 500, e);
        }
    }

    @Override
    public List<DouyinStatsDTO> getStatsByTaskIdAndDateRange(Long taskId, LocalDate startDate, LocalDate endDate) throws DouyinException {
        try {
            // 验证任务是否存在
            taskRepository.findById(taskId)
                    .orElseThrow(() -> new DouyinException("任务不存在", "TASK_NOT_FOUND", 404));
            
            List<DouyinStatsEntity> entities = statsRepository.findByTaskIdAndStatsDateBetween(taskId, startDate, endDate);
            return convertToDTOList(entities);
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取任务日期范围统计数据失败: {}", e.getMessage(), e);
            throw new DouyinException("获取任务日期范围统计数据失败", "STATS_GET_FAILED", 500, e);
        }
    }

    @Override
    public Map<String, Object> getTotalStats() throws DouyinException {
        try {
            Map<String, Object> totalStats = statsRepository.findTotalStats();
            
            // 计算转化率等指标
            calculateDerivedMetrics(totalStats);
            
            return totalStats;
        } catch (Exception e) {
            log.error("获取总统计数据失败: {}", e.getMessage(), e);
            throw new DouyinException("获取总统计数据失败", "STATS_GET_FAILED", 500, e);
        }
    }

    @Override
    public Map<String, Object> getTotalStatsByTaskId(Long taskId) throws DouyinException {
        try {
            // 验证任务是否存在
            taskRepository.findById(taskId)
                    .orElseThrow(() -> new DouyinException("任务不存在", "TASK_NOT_FOUND", 404));
            
            Map<String, Object> totalStats = statsRepository.findTotalStatsByTaskId(taskId);
            
            // 计算转化率等指标
            calculateDerivedMetrics(totalStats);
            
            return totalStats;
        } catch (DouyinException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取任务总统计数据失败: {}", e.getMessage(), e);
            throw new DouyinException("获取任务总统计数据失败", "STATS_GET_FAILED", 500, e);
        }
    }

    @Override
    public Map<String, Object> generateStatsReport(LocalDate startDate, LocalDate endDate) throws DouyinException {
        try {
            List<DouyinStatsEntity> entities = statsRepository.findByStatsDateBetween(startDate, endDate);
            List<DouyinStatsDTO> statsList = convertToDTOList(entities);
            
            // 计算总统计数据
            Map<String, Object> totalStats = calculateTotalStats(entities);
            
            // 生成报表
            Map<String, Object> report = new HashMap<>();
            report.put("startDate", startDate);
            report.put("endDate", endDate);
            report.put("totalDays", entities.size());
            report.put("totalStats", totalStats);
            report.put("dailyStats", statsList);
            
            return report;
        } catch (Exception e) {
            log.error("生成统计报表失败: {}", e.getMessage(), e);
            throw new DouyinException("生成统计报表失败", "REPORT_GENERATE_FAILED", 500, e);
        }
    }

    /**
     * 将实体列表转换为DTO列表
     */
    private List<DouyinStatsDTO> convertToDTOList(List<DouyinStatsEntity> entities) {
        List<DouyinStatsDTO> dtoList = new ArrayList<>();
        
        for (DouyinStatsEntity entity : entities) {
            DouyinStatsDTO dto = new DouyinStatsDTO();
            BeanUtils.copyProperties(entity, dto);
            
            // 计算转化率
            calculateDerivedMetrics(dto);
            
            dtoList.add(dto);
        }
        
        return dtoList;
    }

    /**
     * 计算衍生指标（转化率等）
     */
    private void calculateDerivedMetrics(DouyinStatsDTO dto) {
        // 计算触达成功率
        if (dto.getCustomersFound() > 0) {
            dto.setReachRate((double) dto.getMessagesSent() / dto.getCustomersFound());
        } else {
            dto.setReachRate(0.0);
        }
        
        // 计算转化成功率
        if (dto.getMessagesSent() > 0) {
            dto.setConversionRate((double) dto.getConvertedCustomers() / dto.getMessagesSent());
        } else {
            dto.setConversionRate(0.0);
        }
    }

    /**
     * 计算总统计数据的衍生指标
     */
    private void calculateDerivedMetrics(Map<String, Object> stats) {
        // 确保所有值都不为null
        Long customersFound = (Long) stats.getOrDefault("totalCustomersFound", 0L);
        Long messagesSent = (Long) stats.getOrDefault("totalMessagesSent", 0L);
        Long convertedCustomers = (Long) stats.getOrDefault("totalConvertedCustomers", 0L);
        
        // 计算触达成功率
        double reachRate = customersFound > 0 ? (double) messagesSent / customersFound : 0.0;
        stats.put("reachRate", reachRate);
        
        // 计算转化成功率
        double conversionRate = messagesSent > 0 ? (double) convertedCustomers / messagesSent : 0.0;
        stats.put("conversionRate", conversionRate);
    }

    /**
     * 计算实体列表的总统计数据
     */
    private Map<String, Object> calculateTotalStats(List<DouyinStatsEntity> entities) {
        Map<String, Object> totalStats = new HashMap<>();
        
        // 初始化统计数据
        long totalVideosFound = 0;
        long totalCustomersFound = 0;
        long totalMessagesSent = 0;
        long totalConvertedCustomers = 0;
        long totalFailedCustomers = 0;
        
        // 累加统计数据
        for (DouyinStatsEntity entity : entities) {
            totalVideosFound += entity.getVideosFound();
            totalCustomersFound += entity.getCustomersFound();
            totalMessagesSent += entity.getMessagesSent();
            totalConvertedCustomers += entity.getConvertedCustomers();
            totalFailedCustomers += entity.getFailedCustomers();
        }
        
        // 设置总统计数据
        totalStats.put("totalVideosFound", totalVideosFound);
        totalStats.put("totalCustomersFound", totalCustomersFound);
        totalStats.put("totalMessagesSent", totalMessagesSent);
        totalStats.put("totalConvertedCustomers", totalConvertedCustomers);
        totalStats.put("totalFailedCustomers", totalFailedCustomers);
        
        // 计算转化率等指标
        calculateDerivedMetrics(totalStats);
        
        return totalStats;
    }
}
