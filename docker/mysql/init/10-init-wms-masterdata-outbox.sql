USE wms_db;

CREATE TABLE IF NOT EXISTS `wms_integration_task` (
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
  `next_retry_at` datetime DEFAULT NULL COMMENT '下次重试时间',
  `last_error` text DEFAULT NULL COMMENT '最后错误',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wms_task_idempotency_key` (`idempotency_key`),
  KEY `idx_wms_task_status_next` (`status`, `next_retry_at`),
  KEY `idx_wms_task_action_type` (`action_type`),
  KEY `idx_wms_task_event_id` (`event_id`),
  KEY `idx_wms_task_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 集成任务表（主数据 Outbox）';
