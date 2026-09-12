package com.hxcoe.scrm.dto.douyin;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 抖音获客统计数据传输对象
 */
@Data
public class DouyinStatsDTO {
    /**
     * 统计ID
     */
    private Long id;

    /**
     * 统计日期
     */
    private LocalDate statsDate;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 搜索关键词
     */
    private String searchKeyword;

    /**
     * 采集视频数量
     */
    private Integer videosFound;

    /**
     * 发现意向客户数量
     */
    private Integer customersFound;

    /**
     * 发送私信数量
     */
    private Integer messagesSent;

    /**
     * 成功转化客户数量
     */
    private Integer convertedCustomers;

    /**
     * 无效客户数量
     */
    private Integer failedCustomers;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 转化成功率
     */
    private Double conversionRate;

    /**
     * 触达成功率
     */
    private Double reachRate;
}
