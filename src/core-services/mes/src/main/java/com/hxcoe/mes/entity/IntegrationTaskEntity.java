package com.hxcoe.mes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * MES 集成任务实体（Outbox 表，B6 推送端）。
 *
 * <p>对应表 mes_integration_task。MES 工单完工后，完工事件先同事务入此表，
 * 由 {@code MesCompletionOutboxRetryJob} 异步推送 SCM，解耦 MES 与 SCM。
 * 结构与 ERP/SCM 的 IntegrationTaskEntity 对齐。
 */
@Data
@Entity
@Table(name = "mes_integration_task")
public class IntegrationTaskEntity {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 动作类型（如 SCM_COMPLETION_FACT） */
    private String actionType;

    /** 事件ID */
    @Column(name = "event_id")
    private String eventId;

    /** 链路ID */
    @Column(name = "trace_id")
    private String traceId;

    /** 生产者 */
    private String producer;

    /** 事件版本 */
    private Integer eventVersion;

    /** 分区键 */
    private String partitionKey;

    /** 幂等键 */
    private String idempotencyKey;

    /** 请求体（JSON） */
    @Column(columnDefinition = "LONGTEXT")
    private String requestBody;

    /** 状态（PENDING/CONFIRMED/FAILED） */
    private String status;

    /** 外部引用号（如 erpProductionNo） */
    private String externalRefNo;

    /** 重试次数 */
    private Integer retryCount;

    /** 最近错误信息 */
    @Column(columnDefinition = "TEXT")
    private String lastError;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;

    /**
     * 新增前设置时间戳与默认值。
     */
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    /**
     * 更新前刷新时间戳。
     */
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
