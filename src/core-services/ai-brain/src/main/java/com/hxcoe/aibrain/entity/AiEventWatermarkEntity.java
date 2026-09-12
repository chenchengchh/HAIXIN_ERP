package com.hxcoe.aibrain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 事件水位实体（P4-3 事件驱动触发）
 * 每个事件源一行，记录已消费的最大主键ID，实现"仅处理新增事件"的增量消费语义。
 * 首次消费时水位初始化为源表当前 MAX(id)，历史数据不回溯触发。
 */
@Data
@Entity
@Table(name = "ai_event_watermark")
public class AiEventWatermarkEntity {

    /** 事件源标识（如 EAM_FAULT_RECORD / SRM_PURCHASE_ORDER / WMS_OUTBOX） */
    @Id
    @Column(name = "source_key", length = 64)
    private String sourceKey;

    /** 已消费的最大主键ID */
    @Column(name = "watermark_id", nullable = false)
    private Long watermarkId = 0L;

    /** 最近消费时间 */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
