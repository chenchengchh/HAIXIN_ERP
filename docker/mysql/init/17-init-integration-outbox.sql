USE wms_db;

CREATE TABLE IF NOT EXISTS `wms_integration_outbox` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `event_type` varchar(100) NOT NULL COMMENT '事件类型',
  `ref_no` varchar(100) NOT NULL COMMENT '业务引用号（如 receiptNo）',
  `entity_id` bigint(20) NOT NULL COMMENT '业务实体ID',
  `event_id` varchar(64) DEFAULT NULL COMMENT '事件ID（用于追踪；有值时全局唯一）',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID（用于检索）',
  `producer` varchar(64) DEFAULT NULL COMMENT '生产者服务名',
  `event_version` int(11) DEFAULT '1' COMMENT '事件版本',
  `partition_key` varchar(128) DEFAULT NULL COMMENT '分区键（主业务键）',
  `idempotency_key` varchar(200) DEFAULT NULL COMMENT '业务幂等键',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `retry_count` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
  `next_retry_at` datetime DEFAULT NULL COMMENT '下次重试时间',
  `last_error` varchar(1000) DEFAULT NULL COMMENT '最后错误',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wms_outbox_event_ref` (`event_type`, `ref_no`),
  UNIQUE KEY `uk_wms_outbox_event_id` (`event_id`),
  KEY `idx_wms_outbox_status_next` (`status`, `next_retry_at`),
  KEY `idx_wms_outbox_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS→下游事实回传 Outbox（去重与重试）';

-- QMS 运行时代码通过 qms-service 自身数据源维护 Outbox，因此首批先收口到 owner 库。
USE qms_db;

CREATE TABLE IF NOT EXISTS `qms_integration_outbox` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `event_type` varchar(100) NOT NULL COMMENT '事件类型',
  `ref_no` varchar(100) NOT NULL COMMENT '业务引用号（如 qcNo）',
  `entity_id` bigint(20) NOT NULL COMMENT '业务实体ID',
  `event_id` varchar(64) DEFAULT NULL COMMENT '事件ID（用于追踪；有值时全局唯一）',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID（用于检索）',
  `producer` varchar(64) DEFAULT NULL COMMENT '生产者服务名',
  `event_version` int(11) DEFAULT '1' COMMENT '事件版本',
  `partition_key` varchar(128) DEFAULT NULL COMMENT '分区键（主业务键）',
  `idempotency_key` varchar(200) DEFAULT NULL COMMENT '业务幂等键',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `retry_count` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
  `next_retry_at` datetime DEFAULT NULL COMMENT '下次重试时间',
  `last_error` varchar(1000) DEFAULT NULL COMMENT '最后错误',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qms_outbox_event_ref` (`event_type`, `ref_no`),
  UNIQUE KEY `uk_qms_outbox_event_id` (`event_id`),
  KEY `idx_qms_outbox_status_next` (`status`, `next_retry_at`),
  KEY `idx_qms_outbox_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='QMS→下游事实回传 Outbox（去重与重试）';
