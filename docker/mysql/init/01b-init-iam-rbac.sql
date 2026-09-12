USE oa_db;

CREATE TABLE IF NOT EXISTS oa_user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(255) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  employee_id BIGINT NULL,
  last_login_at DATETIME NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_oa_user_account_username (username),
  KEY idx_oa_user_account_employee_id (employee_id),
  KEY idx_oa_user_account_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='OA 账号表（账号域）';

CREATE TABLE IF NOT EXISTS oa_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(128) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_oa_role_code (role_code),
  KEY idx_oa_role_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='OA 角色表（RBAC）';

CREATE TABLE IF NOT EXISTS oa_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  perm_code VARCHAR(128) NOT NULL,
  perm_name VARCHAR(256) NOT NULL,
  resource VARCHAR(256) NULL,
  action VARCHAR(64) NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_oa_permission_code (perm_code),
  KEY idx_oa_permission_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='OA 权限表（RBAC）';

CREATE TABLE IF NOT EXISTS oa_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_oa_user_role (user_id, role_id),
  KEY idx_oa_user_role_user_id (user_id),
  KEY idx_oa_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='OA 用户-角色关联表';

CREATE TABLE IF NOT EXISTS oa_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  perm_id BIGINT NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_oa_role_permission (role_id, perm_id),
  KEY idx_oa_role_permission_role_id (role_id),
  KEY idx_oa_role_permission_perm_id (perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='OA 角色-权限关联表';

CREATE TABLE IF NOT EXISTS oa_audit_login (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NULL,
  username VARCHAR(64) NULL,
  ip VARCHAR(64) NULL,
  ua VARCHAR(512) NULL,
  result VARCHAR(16) NOT NULL,
  trace_id VARCHAR(64) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_oa_audit_login_user_id (user_id),
  KEY idx_oa_audit_login_username (username),
  KEY idx_oa_audit_login_result (result),
  KEY idx_oa_audit_login_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='OA 登录审计（可选）';
