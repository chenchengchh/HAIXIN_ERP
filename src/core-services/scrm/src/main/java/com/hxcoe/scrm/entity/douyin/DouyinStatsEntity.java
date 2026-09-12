package com.hxcoe.scrm.entity.douyin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 抖音获客统计实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "scrm_douyin_stats")
public class DouyinStatsEntity {
    /**
     * 统计ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 统计日期
     */
    @Column(name = "stats_date", nullable = false)
    private LocalDate statsDate;

    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * 任务名称
     */
    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

    /**
     * 搜索关键词
     */
    @Column(name = "search_keyword", length = 200)
    private String searchKeyword;

    /**
     * 采集视频数量
     */
    @Column(name = "videos_found", nullable = false, columnDefinition = "int default 0")
    private Integer videosFound;

    /**
     * 发现意向客户数量
     */
    @Column(name = "customers_found", nullable = false, columnDefinition = "int default 0")
    private Integer customersFound;

    /**
     * 发送私信数量
     */
    @Column(name = "messages_sent", nullable = false, columnDefinition = "int default 0")
    private Integer messagesSent;

    /**
     * 成功转化客户数量
     */
    @Column(name = "converted_customers", nullable = false, columnDefinition = "int default 0")
    private Integer convertedCustomers;

    /**
     * 无效客户数量
     */
    @Column(name = "failed_customers", nullable = false, columnDefinition = "int default 0")
    private Integer failedCustomers;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 自动设置创建时间
     */
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 自动更新时间
     */
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
