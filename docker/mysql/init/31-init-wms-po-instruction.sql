USE wms_db;

CREATE TABLE IF NOT EXISTS wms_integration_inbox (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_key VARCHAR(200) NOT NULL,
  event_id VARCHAR(64) NULL,
  trace_id VARCHAR(64) NULL,
  producer VARCHAR(64) NULL,
  event_version INT NULL,
  partition_key VARCHAR(128) NULL,
  idempotency_key VARCHAR(200) NULL,
  event_type VARCHAR(64) NOT NULL,
  received_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_wms_inbox_event_key (event_key),
  UNIQUE KEY uk_wms_inbox_event_id (event_id),
  KEY idx_wms_inbox_received_time (received_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 集成 Inbox（幂等去重）';

CREATE TABLE IF NOT EXISTS wms_po_instruction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  po_no VARCHAR(64) NOT NULL,
  last_event_type VARCHAR(64) NOT NULL,
  payload_json JSON NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_wms_po_instruction_po_no (po_no),
  KEY idx_wms_po_instruction_updated_time (updated_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS PO 下发指令（仅用于执行侧参考/生成 ASN 等）';

