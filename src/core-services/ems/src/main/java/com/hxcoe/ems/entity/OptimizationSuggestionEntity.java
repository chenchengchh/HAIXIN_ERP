package com.hxcoe.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优化建议实体类
 * 用于存储能源优化建议数据
 *
 * @author author
 * @date 2026-01-01
 */
@Data
@Entity
@Table(name = "ems_optimization_suggestion")
public class OptimizationSuggestionEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 建议标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 建议内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 目标区域
     */
    @Column(name = "target_area", nullable = false, length = 100)
    private String targetArea;

    /**
     * 预计效果
     */
    @Column(name = "estimated_effect", nullable = false, length = 100)
    private String estimatedEffect;

    /**
     * 状态（adopted: 已采纳, pending: 待处理）
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 自动设置创建和更新时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 自动更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}