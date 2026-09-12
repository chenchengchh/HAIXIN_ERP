SET @crm_sales_order_delivery_status_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'crm_sales_order'
      AND COLUMN_NAME = 'delivery_status'
);
SET @crm_sales_order_delivery_status_sql = IF(
    @crm_sales_order_delivery_status_exists = 0,
    'ALTER TABLE crm_sales_order ADD COLUMN delivery_status VARCHAR(32) NULL COMMENT ''发货状态'' AFTER approval_status',
    'SELECT 1'
);
PREPARE stmt FROM @crm_sales_order_delivery_status_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @crm_sales_order_delivery_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'crm_sales_order'
      AND COLUMN_NAME = 'delivery_time'
);
SET @crm_sales_order_delivery_time_sql = IF(
    @crm_sales_order_delivery_time_exists = 0,
    'ALTER TABLE crm_sales_order ADD COLUMN delivery_time DATETIME NULL COMMENT ''发货时间'' AFTER delivery_status',
    'SELECT 1'
);
PREPARE stmt FROM @crm_sales_order_delivery_time_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `crm_integration_inbox` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `event_key` varchar(200) NOT NULL,
  `event_id` varchar(64) DEFAULT NULL,
  `trace_id` varchar(64) DEFAULT NULL,
  `producer` varchar(64) DEFAULT NULL,
  `event_version` int(11) DEFAULT '1',
  `partition_key` varchar(128) DEFAULT NULL,
  `idempotency_key` varchar(200) DEFAULT NULL,
  `event_type` varchar(50) NOT NULL,
  `received_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_crm_inbox_event_key` (`event_key`),
  UNIQUE KEY `uk_crm_inbox_event_id` (`event_id`),
  KEY `idx_crm_inbox_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CRM 集成事件 Inbox（幂等去重）';
