USE erp_db;

CREATE TABLE IF NOT EXISTS `erp_integration_inbox` (
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
  UNIQUE KEY `uk_erp_inbox_event_key` (`event_key`),
  UNIQUE KEY `uk_erp_inbox_event_id` (`event_id`),
  KEY `idx_erp_inbox_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 集成事件 Inbox（幂等去重）';

CREATE TABLE IF NOT EXISTS `erp_location` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `location_code` varchar(64) NOT NULL,
  `location_name` varchar(128) DEFAULT NULL,
  `warehouse_code` varchar(64) DEFAULT NULL,
  `zone_code` varchar(64) DEFAULT NULL,
  `location_type_code` varchar(64) DEFAULT NULL,
  `status` varchar(8) DEFAULT NULL,
  `remark` varchar(512) DEFAULT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_location_code` (`location_code`),
  KEY `idx_erp_location_warehouse` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 库位镜像（Owner=WMS）';
