-- =============================================================================
-- 集成事件收件箱（Integration Inbox）缺失表补全
-- 背景：ERP/CRM/SCM 消费跨服务事件时需先落 inbox 幂等去重，
--      缺失导致 INSERT 报 bad SQL grammar，事件回流全部失败。
-- 结构对齐 wms_db.wms_integration_inbox（既有可用范式）。
-- =============================================================================

USE erp_db;

CREATE TABLE IF NOT EXISTS `erp_integration_inbox` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_id` varchar(64) DEFAULT NULL COMMENT '事件ID（全局唯一）',
  `event_key` varchar(200) NOT NULL COMMENT '事件业务键',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型',
  `event_version` int DEFAULT NULL COMMENT '事件版本',
  `idempotency_key` varchar(200) DEFAULT NULL COMMENT '业务幂等键',
  `partition_key` varchar(128) DEFAULT NULL COMMENT '分区键',
  `producer` varchar(64) DEFAULT NULL COMMENT '生产者服务名',
  `received_time` datetime(6) NOT NULL COMMENT '接收时间',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_inbox_event_key` (`event_key`),
  UNIQUE KEY `uk_erp_inbox_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci COMMENT='ERP集成事件收件箱（幂等去重）';

USE crm_db;

CREATE TABLE IF NOT EXISTS `crm_integration_inbox` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_id` varchar(64) DEFAULT NULL COMMENT '事件ID（全局唯一）',
  `event_key` varchar(200) NOT NULL COMMENT '事件业务键',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型',
  `event_version` int DEFAULT NULL COMMENT '事件版本',
  `idempotency_key` varchar(200) DEFAULT NULL COMMENT '业务幂等键',
  `partition_key` varchar(128) DEFAULT NULL COMMENT '分区键',
  `producer` varchar(64) DEFAULT NULL COMMENT '生产者服务名',
  `received_time` datetime(6) NOT NULL COMMENT '接收时间',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_crm_inbox_event_key` (`event_key`),
  UNIQUE KEY `uk_crm_inbox_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci COMMENT='CRM集成事件收件箱（幂等去重）';

USE scm_db;

CREATE TABLE IF NOT EXISTS `scm_integration_inbox` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_id` varchar(64) DEFAULT NULL COMMENT '事件ID（全局唯一）',
  `event_key` varchar(200) NOT NULL COMMENT '事件业务键',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型',
  `event_version` int DEFAULT NULL COMMENT '事件版本',
  `idempotency_key` varchar(200) DEFAULT NULL COMMENT '业务幂等键',
  `partition_key` varchar(128) DEFAULT NULL COMMENT '分区键',
  `producer` varchar(64) DEFAULT NULL COMMENT '生产者服务名',
  `received_time` datetime(6) NOT NULL COMMENT '接收时间',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scm_inbox_event_key` (`event_key`),
  UNIQUE KEY `uk_scm_inbox_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_unicode_ci COMMENT='SCM集成事件收件箱（幂等去重）';
