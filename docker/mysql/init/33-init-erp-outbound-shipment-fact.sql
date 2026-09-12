USE erp_db;

CREATE TABLE IF NOT EXISTS erp_outbound_shipment_fact (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64) NOT NULL,
  outbound_order_id BIGINT NULL,
  order_type VARCHAR(64) NULL,
  source_no VARCHAR(64) NULL,
  customer_name VARCHAR(255) NULL,
  address VARCHAR(255) NULL,
  status VARCHAR(32) NOT NULL,
  shipped_time DATETIME NULL,
  last_event_type VARCHAR(64) NOT NULL,
  payload_json JSON NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_erp_outbound_shipment_fact_order_no (order_no),
  KEY idx_erp_outbound_shipment_fact_updated_time (updated_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 出库发货事实表（Owner=WMS 发货完成回写）';
