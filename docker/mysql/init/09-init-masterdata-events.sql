USE erp_db;

CREATE TABLE IF NOT EXISTS `erp_integration_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `action_type` varchar(64) DEFAULT NULL COMMENT '动作类型',
  `event_id` varchar(64) DEFAULT NULL COMMENT '事件ID（用于追踪）',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID（用于检索）',
  `producer` varchar(64) DEFAULT NULL COMMENT '生产者服务名',
  `event_version` int(11) DEFAULT '1' COMMENT '事件版本',
  `partition_key` varchar(128) DEFAULT NULL COMMENT '分区键（主业务键）',
  `idempotency_key` varchar(200) DEFAULT NULL COMMENT '幂等键',
  `request_body` longtext DEFAULT NULL COMMENT '请求体',
  `status` varchar(32) DEFAULT NULL COMMENT '状态',
  `external_ref_no` varchar(128) DEFAULT NULL COMMENT '外部引用编号',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `last_error` text DEFAULT NULL COMMENT '最后错误',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_task_idempotency_key` (`idempotency_key`),
  KEY `idx_erp_task_status` (`status`),
  KEY `idx_erp_task_action_type` (`action_type`),
  KEY `idx_erp_task_event_id` (`event_id`),
  KEY `idx_erp_task_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 集成任务表（Outbox）';

-- BOM 运行时代码通过 bom-service 自身数据源直接写入 Inbox，因此首批先收口到 owner 库。
USE bom_db;

CREATE TABLE IF NOT EXISTS `bom_integration_inbox` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `event_key` varchar(200) NOT NULL COMMENT '幂等键（唯一）',
  `event_id` varchar(64) DEFAULT NULL COMMENT '事件ID（用于追踪；有值时全局唯一）',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID（用于检索）',
  `producer` varchar(64) DEFAULT NULL COMMENT '生产者服务名',
  `event_version` int(11) DEFAULT '1' COMMENT '事件版本',
  `partition_key` varchar(128) DEFAULT NULL COMMENT '分区键（主业务键）',
  `idempotency_key` varchar(200) DEFAULT NULL COMMENT '业务幂等键（兼容 event_key）',
  `event_type` varchar(50) NOT NULL COMMENT '事件类型',
  `received_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bom_inbox_event_key` (`event_key`),
  UNIQUE KEY `uk_bom_inbox_event_id` (`event_id`),
  KEY `idx_bom_inbox_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BOM 集成事件 Inbox（幂等去重）';
