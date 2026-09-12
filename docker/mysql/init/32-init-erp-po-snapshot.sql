USE erp_db;

CREATE TABLE IF NOT EXISTS erp_po_snapshot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  po_no VARCHAR(64) NOT NULL,
  last_event_type VARCHAR(64) NOT NULL,
  payload_json JSON NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_erp_po_snapshot_po_no (po_no),
  KEY idx_erp_po_snapshot_updated_time (updated_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP PO 事件快照（只读参考，不作为 PO 权威）';

