USE oa_db;

CREATE TABLE IF NOT EXISTS oa_audit_action (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  employee_id BIGINT NULL,
  username VARCHAR(64) NULL,
  trace_id VARCHAR(64) NULL,
  http_method VARCHAR(16) NULL,
  path VARCHAR(512) NULL,
  status_code INT NULL,
  duration_ms BIGINT NULL,
  result VARCHAR(16) NULL,
  error_message VARCHAR(512) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_oa_audit_action_user_id (user_id),
  KEY idx_oa_audit_action_employee_id (employee_id),
  KEY idx_oa_audit_action_username (username),
  KEY idx_oa_audit_action_trace_id (trace_id),
  KEY idx_oa_audit_action_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='OA 操作审计（非登录）';

