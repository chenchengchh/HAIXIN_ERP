package com.hxcoe.scrm.repository.douyin;

import com.hxcoe.scrm.entity.douyin.DouyinStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 抖音获客统计数据访问接口
 */
@Repository
public interface DouyinStatsRepository extends JpaRepository<DouyinStatsEntity, Long> {

    /**
     * 根据日期查询统计数据
     * @param statsDate 统计日期
     * @return 统计数据列表
     */
    List<DouyinStatsEntity> findByStatsDate(LocalDate statsDate);

    /**
     * 根据任务ID和日期查询统计数据
     * @param taskId 任务ID
     * @param statsDate 统计日期
     * @return 统计数据
     */
    DouyinStatsEntity findByTaskIdAndStatsDate(Long taskId, LocalDate statsDate);

    /**
     * 查询指定日期范围内的统计数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<DouyinStatsEntity> findByStatsDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 查询指定任务在日期范围内的统计数据
     * @param taskId 任务ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据列表
     */
    List<DouyinStatsEntity> findByTaskIdAndStatsDateBetween(Long taskId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据任务ID查询所有统计数据
     * @param taskId 任务ID
     * @return 统计数据列表
     */
    List<DouyinStatsEntity> findByTaskId(Long taskId);

    /**
     * 查询今日统计数据
     * @return 今日统计数据列表
     */
    @Query("SELECT s FROM DouyinStatsEntity s WHERE s.statsDate = CURRENT_DATE")
    List<DouyinStatsEntity> findTodayStats();

    /**
     * 查询指定任务的总统计数据
     * @param taskId 任务ID
     * @return 总统计数据
     */
    @Query("SELECT new map(SUM(s.videosFound) as totalVideosFound, SUM(s.customersFound) as totalCustomersFound, SUM(s.messagesSent) as totalMessagesSent, SUM(s.convertedCustomers) as totalConvertedCustomers, SUM(s.failedCustomers) as totalFailedCustomers) FROM DouyinStatsEntity s WHERE s.taskId = :taskId")
    java.util.Map<String, Object> findTotalStatsByTaskId(@Param("taskId") Long taskId);

    /**
     * 查询所有任务的总统计数据
     * @return 总统计数据
     */
    @Query("SELECT new map(SUM(s.videosFound) as totalVideosFound, SUM(s.customersFound) as totalCustomersFound, SUM(s.messagesSent) as totalMessagesSent, SUM(s.convertedCustomers) as totalConvertedCustomers, SUM(s.failedCustomers) as totalFailedCustomers) FROM DouyinStatsEntity s")
    java.util.Map<String, Object> findTotalStats();
}
