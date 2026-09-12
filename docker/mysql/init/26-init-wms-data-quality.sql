USE wms_db;

CREATE TABLE IF NOT EXISTS wms_data_clean_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(64) NOT NULL,
  conflict_type VARCHAR(64) NOT NULL,
  business_key VARCHAR(128) NOT NULL,
  detail_json JSON NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  resolution VARCHAR(512) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_wms_clean_task (entity_type, conflict_type, business_key),
  KEY idx_wms_clean_task_status (status),
  KEY idx_wms_clean_task_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 主数据清洗队列（冲突/缺失）';

CREATE TABLE IF NOT EXISTS wms_data_quality_metric (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  metric_date DATE NOT NULL,
  metric_key VARCHAR(128) NOT NULL,
  metric_value BIGINT NOT NULL,
  detail_json JSON NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_wms_metric_date_key (metric_date, metric_key),
  KEY idx_wms_metric_date (metric_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='WMS 数据质量指标（日维度）';

