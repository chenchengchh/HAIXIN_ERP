package com.hxcoe.scrm.service.douyin;

import com.hxcoe.scrm.dto.douyin.DouyinStatsDTO;
import com.hxcoe.scrm.entity.douyin.DouyinStatsEntity;
import com.hxcoe.scrm.exception.douyin.DouyinException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 抖音获客统计服务接口
 */
public interface DouyinStatsService {

    /**
     * 更新统计数据
     * @param taskId 任务ID
     * @param videosFound 采集视频数量
     * @param customersFound 发现意向客户数量
     * @param messagesSent 发送私信数量
     * @param convertedCustomers 成功转化客户数量
     * @param failedCustomers 无效客户数量
     * @throws DouyinException 抖音统计异常
     */
    void updateStats(Long taskId, int videosFound, int customersFound, int messagesSent, int convertedCustomers, int failedCustomers) throws DouyinException;

    /**
     * 获取今日统计数据
     * @return 今日统计数据列表
     * @throws DouyinException 抖音统计异常
     */
    List<DouyinStatsDTO> getTodayStats() throws DouyinException;

    /**
     * 获取指定日期的统计数据
     * @param statsDate 统计日期
     * @return 统计数据列表
     * @throws DouyinException 抖音统计异常
     */
    List<DouyinStatsDTO> getStatsByDate(LocalDate statsDate) throws DouyinException;

    /**
     * 获取指定日期范围的统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     * @throws DouyinException 抖音统计异常
     */
    List<DouyinStatsDTO> getStatsByDateRange(LocalDate startDate, LocalDate endDate) throws DouyinException;

    /**
     * 获取指定任务的统计数据
     * @param taskId 任务ID
     * @return 统计数据列表
     * @throws DouyinException 抖音统计异常
     */
    List<DouyinStatsDTO> getStatsByTaskId(Long taskId) throws DouyinException;

    /**
     * 获取指定任务在日期范围内的统计数据
     * @param taskId 任务ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     * @throws DouyinException 抖音统计异常
     */
    List<DouyinStatsDTO> getStatsByTaskIdAndDateRange(Long taskId, LocalDate startDate, LocalDate endDate) throws DouyinException;

    /**
     * 获取所有任务的总统计数据
     * @return 总统计数据
     * @throws DouyinException 抖音统计异常
     */
    Map<String, Object> getTotalStats() throws DouyinException;

    /**
     * 获取指定任务的总统计数据
     * @param taskId 任务ID
     * @return 总统计数据
     * @throws DouyinException 抖音统计异常
     */
    Map<String, Object> getTotalStatsByTaskId(Long taskId) throws DouyinException;

    /**
     * 生成统计报表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计报表数据
     * @throws DouyinException 抖音统计异常
     */
    Map<String, Object> generateStatsReport(LocalDate startDate, LocalDate endDate) throws DouyinException;
}
