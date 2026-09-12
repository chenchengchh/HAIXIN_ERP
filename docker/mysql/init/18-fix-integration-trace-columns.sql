USE hxcoe003;

DROP PROCEDURE IF EXISTS `hx_add_column_if_missing`;
DROP PROCEDURE IF EXISTS `hx_add_index_if_missing`;

DELIMITER //
CREATE PROCEDURE `hx_add_column_if_missing`(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_column_ddl TEXT)
BEGIN
  DECLARE v_table_exists INT DEFAULT 0;
  DECLARE v_col_exists INT DEFAULT 0;
  SELECT COUNT(*) INTO v_table_exists
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table;
  IF v_table_exists > 0 THEN
    SELECT COUNT(*) INTO v_col_exists
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND COLUMN_NAME = p_column;
    IF v_col_exists = 0 THEN
      SET @s = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_column_ddl);
      PREPARE stmt FROM @s;
      EXECUTE stmt;
      DEALLOCATE PREPARE stmt;
    END IF;
  END IF;
END//

CREATE PROCEDURE `hx_add_index_if_missing`(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_create_sql TEXT)
BEGIN
  DECLARE v_table_exists INT DEFAULT 0;
  DECLARE v_idx_exists INT DEFAULT 0;
  SELECT COUNT(*) INTO v_table_exists
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table;
  IF v_table_exists > 0 THEN
    SELECT COUNT(*) INTO v_idx_exists
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = p_table AND INDEX_NAME = p_index;
    IF v_idx_exists = 0 THEN
      SET @s = p_create_sql;
      PREPARE stmt FROM @s;
      EXECUTE stmt;
      DEALLOCATE PREPARE stmt;
    END IF;
  END IF;
END//
DELIMITER ;

CALL hx_add_column_if_missing('scm_integration_task','event_id','`event_id` varchar(64) DEFAULT NULL COMMENT ''事件ID（用于追踪）''');
CALL hx_add_column_if_missing('scm_integration_task','trace_id','`trace_id` varchar(64) DEFAULT NULL COMMENT ''链路追踪ID（用于检索）''');
CALL hx_add_column_if_missing('scm_integration_task','producer','`producer` varchar(64) DEFAULT NULL COMMENT ''生产者服务名''');
CALL hx_add_column_if_missing('scm_integration_task','event_version','`event_version` int(11) DEFAULT ''1'' COMMENT ''事件版本''');
CALL hx_add_column_if_missing('scm_integration_task','partition_key','`partition_key` varchar(128) DEFAULT NULL COMMENT ''分区键（主业务键）''');

CALL hx_add_column_if_missing('srm_integration_task','event_id','`event_id` varchar(64) DEFAULT NULL COMMENT ''事件ID（用于追踪）''');
CALL hx_add_column_if_missing('srm_integration_task','trace_id','`trace_id` varchar(64) DEFAULT NULL COMMENT ''链路追踪ID（用于检索）''');
CALL hx_add_column_if_missing('srm_integration_task','producer','`producer` varchar(64) DEFAULT NULL COMMENT ''生产者服务名''');
CALL hx_add_column_if_missing('srm_integration_task','event_version','`event_version` int(11) DEFAULT ''1'' COMMENT ''事件版本''');
CALL hx_add_column_if_missing('srm_integration_task','partition_key','`partition_key` varchar(128) DEFAULT NULL COMMENT ''分区键（主业务键）''');

CALL hx_add_column_if_missing('erp_integration_task','event_id','`event_id` varchar(64) DEFAULT NULL COMMENT ''事件ID（用于追踪）''');
CALL hx_add_column_if_missing('erp_integration_task','trace_id','`trace_id` varchar(64) DEFAULT NULL COMMENT ''链路追踪ID（用于检索）''');
CALL hx_add_column_if_missing('erp_integration_task','producer','`producer` varchar(64) DEFAULT NULL COMMENT ''生产者服务名''');
CALL hx_add_column_if_missing('erp_integration_task','event_version','`event_version` int(11) DEFAULT ''1'' COMMENT ''事件版本''');
CALL hx_add_column_if_missing('erp_integration_task','partition_key','`partition_key` varchar(128) DEFAULT NULL COMMENT ''分区键（主业务键）''');

CALL hx_add_column_if_missing('wms_integration_task','event_id','`event_id` varchar(64) DEFAULT NULL COMMENT ''事件ID（用于追踪）''');
CALL hx_add_column_if_missing('wms_integration_task','trace_id','`trace_id` varchar(64) DEFAULT NULL COMMENT ''链路追踪ID（用于检索）''');
CALL hx_add_column_if_missing('wms_integration_task','producer','`producer` varchar(64) DEFAULT NULL COMMENT ''生产者服务名''');
CALL hx_add_column_if_missing('wms_integration_task','event_version','`event_version` int(11) DEFAULT ''1'' COMMENT ''事件版本''');
CALL hx_add_column_if_missing('wms_integration_task','partition_key','`partition_key` varchar(128) DEFAULT NULL COMMENT ''分区键（主业务键）''');

CALL hx_add_column_if_missing('scm_integration_inbox','event_id','`event_id` varchar(64) DEFAULT NULL COMMENT ''事件ID（用于追踪；有值时全局唯一）''');
CALL hx_add_column_if_missing('scm_integration_inbox','trace_id','`trace_id` varchar(64) DEFAULT NULL COMMENT ''链路追踪ID（用于检索）''');
CALL hx_add_column_if_missing('scm_integration_inbox','producer','`producer` varchar(64) DEFAULT NULL COMMENT ''生产者服务名''');
CALL hx_add_column_if_missing('scm_integration_inbox','event_version','`event_version` int(11) DEFAULT ''1'' COMMENT ''事件版本''');
CALL hx_add_column_if_missing('scm_integration_inbox','partition_key','`partition_key` varchar(128) DEFAULT NULL COMMENT ''分区键（主业务键）''');
CALL hx_add_column_if_missing('scm_integration_inbox','idempotency_key','`idempotency_key` varchar(200) DEFAULT NULL COMMENT ''业务幂等键（兼容 event_key）''');

CALL hx_add_column_if_missing('srm_integration_inbox','event_id','`event_id` varchar(64) DEFAULT NULL COMMENT ''事件ID（用于追踪；有值时全局唯一）''');
CALL hx_add_column_if_missing('srm_integration_inbox','trace_id','`trace_id` varchar(64) DEFAULT NULL COMMENT ''链路追踪ID（用于检索）''');
CALL hx_add_column_if_missing('srm_integration_inbox','producer','`producer` varchar(64) DEFAULT NULL COMMENT ''生产者服务名''');
CALL hx_add_column_if_missing('srm_integration_inbox','event_version','`event_version` int(11) DEFAULT ''1'' COMMENT ''事件版本''');
CALL hx_add_column_if_missing('srm_integration_inbox','partition_key','`partition_key` varchar(128) DEFAULT NULL COMMENT ''分区键（主业务键）''');
CALL hx_add_column_if_missing('srm_integration_inbox','idempotency_key','`idempotency_key` varchar(200) DEFAULT NULL COMMENT ''业务幂等键（兼容 event_key）''');

CALL hx_add_column_if_missing('erp_integration_inbox','event_id','`event_id` varchar(64) DEFAULT NULL COMMENT ''事件ID（用于追踪；有值时全局唯一）''');
CALL hx_add_column_if_missing('erp_integration_inbox','trace_id','`trace_id` varchar(64) DEFAULT NULL COMMENT ''链路追踪ID（用于检索）''');
CALL hx_add_column_if_missing('erp_integration_inbox','producer','`producer` varchar(64) DEFAULT NULL COMMENT ''生产者服务名''');
CALL hx_add_column_if_missing('erp_integration_inbox','event_version','`event_version` int(11) DEFAULT ''1'' COMMENT ''事件版本''');
CALL hx_add_column_if_missing('erp_integration_inbox','partition_key','`partition_key` varchar(128) DEFAULT NULL COMMENT ''分区键（主业务键）''');
CALL hx_add_column_if_missing('erp_integration_inbox','idempotency_key','`idempotency_key` varchar(200) DEFAULT NULL COMMENT ''业务幂等键（兼容 event_key）''');

CALL hx_add_column_if_missing('bom_integration_inbox','event_id','`event_id` varchar(64) DEFAULT NULL COMMENT ''事件ID（用于追踪；有值时全局唯一）''');
CALL hx_add_column_if_missing('bom_integration_inbox','trace_id','`trace_id` varchar(64) DEFAULT NULL COMMENT ''链路追踪ID（用于检索）''');
CALL hx_add_column_if_missing('bom_integration_inbox','producer','`producer` varchar(64) DEFAULT NULL COMMENT ''生产者服务名''');
CALL hx_add_column_if_missing('bom_integration_inbox','event_version','`event_version` int(11) DEFAULT ''1'' COMMENT ''事件版本''');
CALL hx_add_column_if_missing('bom_integration_inbox','partition_key','`partition_key` varchar(128) DEFAULT NULL COMMENT ''分区键（主业务键）''');
CALL hx_add_column_if_missing('bom_integration_inbox','idempotency_key','`idempotency_key` varchar(200) DEFAULT NULL COMMENT ''业务幂等键（兼容 event_key）''');

CALL hx_add_column_if_missing('wms_integration_outbox','event_id','`event_id` varchar(64) DEFAULT NULL COMMENT ''事件ID（用于追踪；有值时全局唯一）''');
CALL hx_add_column_if_missing('wms_integration_outbox','trace_id','`trace_id` varchar(64) DEFAULT NULL COMMENT ''链路追踪ID（用于检索）''');
CALL hx_add_column_if_missing('wms_integration_outbox','producer','`producer` varchar(64) DEFAULT NULL COMMENT ''生产者服务名''');
CALL hx_add_column_if_missing('wms_integration_outbox','event_version','`event_version` int(11) DEFAULT ''1'' COMMENT ''事件版本''');
CALL hx_add_column_if_missing('wms_integration_outbox','partition_key','`partition_key` varchar(128) DEFAULT NULL COMMENT ''分区键（主业务键）''');
CALL hx_add_column_if_missing('wms_integration_outbox','idempotency_key','`idempotency_key` varchar(200) DEFAULT NULL COMMENT ''业务幂等键''');

CALL hx_add_column_if_missing('qms_integration_outbox','event_id','`event_id` varchar(64) DEFAULT NULL COMMENT ''事件ID（用于追踪；有值时全局唯一）''');
CALL hx_add_column_if_missing('qms_integration_outbox','trace_id','`trace_id` varchar(64) DEFAULT NULL COMMENT ''链路追踪ID（用于检索）''');
CALL hx_add_column_if_missing('qms_integration_outbox','producer','`producer` varchar(64) DEFAULT NULL COMMENT ''生产者服务名''');
CALL hx_add_column_if_missing('qms_integration_outbox','event_version','`event_version` int(11) DEFAULT ''1'' COMMENT ''事件版本''');
CALL hx_add_column_if_missing('qms_integration_outbox','partition_key','`partition_key` varchar(128) DEFAULT NULL COMMENT ''分区键（主业务键）''');
CALL hx_add_column_if_missing('qms_integration_outbox','idempotency_key','`idempotency_key` varchar(200) DEFAULT NULL COMMENT ''业务幂等键''');

CALL hx_add_index_if_missing('scm_integration_inbox','uk_scm_inbox_event_id','CREATE UNIQUE INDEX `uk_scm_inbox_event_id` ON `scm_integration_inbox` (`event_id`)');
CALL hx_add_index_if_missing('scm_integration_inbox','idx_scm_inbox_trace_id','CREATE INDEX `idx_scm_inbox_trace_id` ON `scm_integration_inbox` (`trace_id`)');
CALL hx_add_index_if_missing('scm_integration_task','idx_scm_task_event_id','CREATE INDEX `idx_scm_task_event_id` ON `scm_integration_task` (`event_id`)');
CALL hx_add_index_if_missing('scm_integration_task','idx_scm_task_trace_id','CREATE INDEX `idx_scm_task_trace_id` ON `scm_integration_task` (`trace_id`)');

CALL hx_add_index_if_missing('srm_integration_inbox','uk_srm_inbox_event_id','CREATE UNIQUE INDEX `uk_srm_inbox_event_id` ON `srm_integration_inbox` (`event_id`)');
CALL hx_add_index_if_missing('srm_integration_inbox','idx_srm_inbox_trace_id','CREATE INDEX `idx_srm_inbox_trace_id` ON `srm_integration_inbox` (`trace_id`)');
CALL hx_add_index_if_missing('srm_integration_task','idx_srm_task_event_id','CREATE INDEX `idx_srm_task_event_id` ON `srm_integration_task` (`event_id`)');
CALL hx_add_index_if_missing('srm_integration_task','idx_srm_task_trace_id','CREATE INDEX `idx_srm_task_trace_id` ON `srm_integration_task` (`trace_id`)');

CALL hx_add_index_if_missing('erp_integration_inbox','uk_erp_inbox_event_id','CREATE UNIQUE INDEX `uk_erp_inbox_event_id` ON `erp_integration_inbox` (`event_id`)');
CALL hx_add_index_if_missing('erp_integration_inbox','idx_erp_inbox_trace_id','CREATE INDEX `idx_erp_inbox_trace_id` ON `erp_integration_inbox` (`trace_id`)');
CALL hx_add_index_if_missing('erp_integration_task','idx_erp_task_event_id','CREATE INDEX `idx_erp_task_event_id` ON `erp_integration_task` (`event_id`)');
CALL hx_add_index_if_missing('erp_integration_task','idx_erp_task_trace_id','CREATE INDEX `idx_erp_task_trace_id` ON `erp_integration_task` (`trace_id`)');

CALL hx_add_index_if_missing('bom_integration_inbox','uk_bom_inbox_event_id','CREATE UNIQUE INDEX `uk_bom_inbox_event_id` ON `bom_integration_inbox` (`event_id`)');
CALL hx_add_index_if_missing('bom_integration_inbox','idx_bom_inbox_trace_id','CREATE INDEX `idx_bom_inbox_trace_id` ON `bom_integration_inbox` (`trace_id`)');

CALL hx_add_index_if_missing('wms_integration_outbox','uk_wms_outbox_event_id','CREATE UNIQUE INDEX `uk_wms_outbox_event_id` ON `wms_integration_outbox` (`event_id`)');
CALL hx_add_index_if_missing('wms_integration_outbox','idx_wms_outbox_trace_id','CREATE INDEX `idx_wms_outbox_trace_id` ON `wms_integration_outbox` (`trace_id`)');
CALL hx_add_index_if_missing('wms_integration_task','idx_wms_task_event_id','CREATE INDEX `idx_wms_task_event_id` ON `wms_integration_task` (`event_id`)');
CALL hx_add_index_if_missing('wms_integration_task','idx_wms_task_trace_id','CREATE INDEX `idx_wms_task_trace_id` ON `wms_integration_task` (`trace_id`)');

CALL hx_add_index_if_missing('qms_integration_outbox','uk_qms_outbox_event_id','CREATE UNIQUE INDEX `uk_qms_outbox_event_id` ON `qms_integration_outbox` (`event_id`)');
CALL hx_add_index_if_missing('qms_integration_outbox','idx_qms_outbox_trace_id','CREATE INDEX `idx_qms_outbox_trace_id` ON `qms_integration_outbox` (`trace_id`)');

DROP PROCEDURE IF EXISTS `hx_add_column_if_missing`;
DROP PROCEDURE IF EXISTS `hx_add_index_if_missing`;
