USE scm_db;

CREATE TABLE IF NOT EXISTS `scm_po_status_history` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL,
  `from_status` INT NULL,
  `to_status` INT NULL,
  `event_type` VARCHAR(32) NOT NULL,
  `operator` VARCHAR(64) NULL,
  `detail_json` JSON NULL,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_scm_po_status_history_order_no` (`order_no`),
  KEY `idx_scm_po_status_history_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SCM PO 状态流转审计（状态机历史）';

CREATE TABLE IF NOT EXISTS `scm_po_supplier_commit` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL,
  `supplier_code` VARCHAR(64) NOT NULL,
  `commit_delivery_date` DATETIME NULL,
  `confirm_status` VARCHAR(16) NOT NULL DEFAULT 'CONFIRMED',
  `operator` VARCHAR(64) NULL,
  `payload_json` JSON NULL,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_scm_po_supplier_commit_order_no` (`order_no`),
  KEY `idx_scm_po_supplier_commit_supplier_code` (`supplier_code`),
  KEY `idx_scm_po_supplier_commit_updated_time` (`updated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SCM PO 供应商协同承诺（确认/承诺交期）';

