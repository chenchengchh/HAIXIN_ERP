-- 创建独立 PLM 数据库，避免与共享库冲突
CREATE DATABASE IF NOT EXISTS plm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE plm_db;

-- 产品表
CREATE TABLE IF NOT EXISTS plm_product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_code VARCHAR(50) NOT NULL,
  product_name VARCHAR(100) NOT NULL,
  product_model VARCHAR(50),
  product_spec VARCHAR(200),
  product_type VARCHAR(50),
  product_category VARCHAR(50),
  unit VARCHAR(20),
  status VARCHAR(20) DEFAULT 'DRAFT',
  version VARCHAR(20),
  description VARCHAR(500),
  created_by VARCHAR(50),
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50),
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_product_code (product_code)
);

-- BOM表（BOM行，按parent_id构建树）
CREATE TABLE IF NOT EXISTS plm_bom (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  bom_code VARCHAR(50) NOT NULL,
  product_code VARCHAR(50),
  child_component_code VARCHAR(50),
  material_code VARCHAR(50) NOT NULL,
  material_name VARCHAR(100) NOT NULL,
  material_spec VARCHAR(200),
  unit VARCHAR(20),
  quantity DECIMAL(10,4) NOT NULL DEFAULT 1,
  level INT NOT NULL DEFAULT 1,
  parent_id BIGINT,
  sort_order INT,
  is_key_part TINYINT(1),
  substitute_material VARCHAR(50),
  remark VARCHAR(500),
  status VARCHAR(20) DEFAULT 'DRAFT',
  version VARCHAR(20),
  created_by VARCHAR(50),
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50),
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_bom_code (bom_code),
  INDEX idx_bom_product (product_id),
  INDEX idx_bom_parent (parent_id),
  INDEX idx_bom_material (material_code)
);

-- PLM文档表（元数据 + 文件路径）
CREATE TABLE IF NOT EXISTS plm_document (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doc_code VARCHAR(64),
  title VARCHAR(256) NOT NULL,
  doc_type VARCHAR(64),
  category VARCHAR(64),
  version VARCHAR(32),
  status VARCHAR(32),
  author VARCHAR(64),
  file_name VARCHAR(256),
  file_path VARCHAR(512),
  file_size VARCHAR(32),
  file_format VARCHAR(32),
  remark VARCHAR(500),
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_doc_code (doc_code),
  INDEX idx_doc_code (doc_code),
  INDEX idx_doc_type (doc_type)
);

-- 研发项目表
CREATE TABLE IF NOT EXISTS plm_project (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_code VARCHAR(64) NOT NULL,
  project_name VARCHAR(128) NOT NULL,
  manager VARCHAR(64),
  project_type VARCHAR(64),
  status VARCHAR(32) DEFAULT 'planning',
  progress INT DEFAULT 0,
  start_date DATE,
  end_date DATE,
  created_by VARCHAR(64),
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(64),
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_project_code (project_code)
);

-- 项目任务表
CREATE TABLE IF NOT EXISTS plm_task (
  id BIGINT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  task_name VARCHAR(128) NOT NULL,
  assignee VARCHAR(64),
  progress INT DEFAULT 0,
  start_date DATE,
  end_date DATE,
  duration INT,
  parent_id BIGINT,
  task_type VARCHAR(32),
  status VARCHAR(32) DEFAULT 'OPEN',
  created_by VARCHAR(64),
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(64),
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_task_project (project_id)
);

-- 甘特图依赖（任务连线）
CREATE TABLE IF NOT EXISTS plm_gantt_link (
  id BIGINT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  source_task_id BIGINT NOT NULL,
  target_task_id BIGINT NOT NULL,
  link_type VARCHAR(32) NOT NULL,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_gantt_link_project (project_id)
);

-- 资源负载（按天/周/月均可，前端按日期展示）
CREATE TABLE IF NOT EXISTS plm_resource_load (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT,
  resource_id VARCHAR(64) NOT NULL,
  resource_name VARCHAR(64) NOT NULL,
  load_value INT NOT NULL,
  load_date DATE NOT NULL,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_resource_load_project (project_id),
  INDEX idx_resource_load_resource (resource_id),
  INDEX idx_resource_load_date (load_date)
);

-- 工艺路线表
CREATE TABLE IF NOT EXISTS plm_process_route (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT,
  route_code VARCHAR(64),
  route_name VARCHAR(128) NOT NULL,
  product_code VARCHAR(64),
  product_name VARCHAR(128),
  route_type VARCHAR(32),
  version VARCHAR(32),
  status VARCHAR(32) DEFAULT 'draft',
  create_user VARCHAR(64),
  steps_json LONGTEXT,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_route_code (route_code),
  INDEX idx_route_product (product_id),
  INDEX idx_route_code (route_code)
);

-- 工艺文件表
CREATE TABLE IF NOT EXISTS plm_process_file (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT,
  file_code VARCHAR(64),
  title VARCHAR(256) NOT NULL,
  file_type VARCHAR(64),
  category VARCHAR(64),
  file_name VARCHAR(256),
  file_path VARCHAR(512) NOT NULL,
  version VARCHAR(32),
  status VARCHAR(32) DEFAULT 'active',
  author VARCHAR(64),
  file_size VARCHAR(32),
  file_format VARCHAR(32),
  remark VARCHAR(500),
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_file_code (file_code),
  INDEX idx_file_product (product_id),
  INDEX idx_file_code (file_code)
);

-- 工艺变更表
CREATE TABLE IF NOT EXISTS plm_change_request (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT,
  change_code VARCHAR(64),
  title VARCHAR(256) NOT NULL,
  description TEXT,
  change_type VARCHAR(64),
  process_code VARCHAR(64),
  process_name VARCHAR(128),
  reason LONGTEXT,
  content LONGTEXT,
  status VARCHAR(32) DEFAULT 'DRAFT',
  created_by VARCHAR(64),
  apply_user VARCHAR(64),
  apply_time DATE,
  approve_user VARCHAR(64),
  approve_time DATE,
  implement_time DATE,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_change_code (change_code),
  INDEX idx_change_product (product_id),
  INDEX idx_change_code (change_code)
);

-- 试产计划表
CREATE TABLE IF NOT EXISTS plm_trial_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT,
  plan_code VARCHAR(64),
  plan_name VARCHAR(128) NOT NULL,
  product_code VARCHAR(64),
  product_name VARCHAR(128),
  version VARCHAR(32),
  trial_type VARCHAR(64),
  trial_qty INT,
  departments_json LONGTEXT,
  stages_json LONGTEXT,
  responsible_person VARCHAR(64),
  status VARCHAR(32) DEFAULT 'PLANNED',
  start_date DATE,
  end_date DATE,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_plan_code (plan_code),
  INDEX idx_trial_plan_product (product_id),
  INDEX idx_plan_code (plan_code)
);

-- 试产报告表
CREATE TABLE IF NOT EXISTS plm_trial_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plan_id BIGINT NOT NULL,
  report_code VARCHAR(64),
  report_title VARCHAR(256) NOT NULL,
  content TEXT,
  status VARCHAR(32) DEFAULT 'DRAFT',
  create_user VARCHAR(64),
  create_time DATE,
  approve_user VARCHAR(64),
  approve_time DATE,
  main_issues_json LONGTEXT,
  improvements_json LONGTEXT,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_report_code (report_code),
  INDEX idx_trial_report_plan (plan_id),
  INDEX idx_report_code (report_code)
);

-- 质量问题表
CREATE TABLE IF NOT EXISTS plm_quality_issue (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT,
  issue_code VARCHAR(64),
  issue_title VARCHAR(256) NOT NULL,
  severity VARCHAR(16) DEFAULT 'MEDIUM',
  status VARCHAR(32) DEFAULT 'OPEN',
  resolution LONGTEXT,
  create_user VARCHAR(64),
  create_time DATE,
  resolve_user VARCHAR(64),
  resolve_time DATE,
  created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_issue_code (issue_code),
  INDEX idx_quality_issue_product (product_id),
  INDEX idx_issue_code (issue_code)
);

-- 初始化演示数据（用于前端无模拟数据时可直接展示）
INSERT INTO plm_product (id, product_code, product_name, product_model, product_spec, product_type, product_category, unit, status, version, description, created_by)
VALUES
  (1, 'PROD-001', '智能手表', 'SW-100', '42mm/蓝牙/WiFi', 'finished', 'wearable', '件', 'ACTIVE', 'V1.0', '智能穿戴产品', '系统'),
  (2, 'PROD-002', '锂电池电芯', 'BAT-18650', '3.7V/2600mAh', 'raw', 'battery', '件', 'ACTIVE', 'V1.0', '动力电池电芯', '系统')
ON DUPLICATE KEY UPDATE
  product_name = VALUES(product_name),
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_bom (id, product_id, bom_code, product_code, child_component_code, material_code, material_name, material_spec, unit, quantity, level, parent_id, sort_order, is_key_part, substitute_material, remark, status, version, created_by)
VALUES
  (1001, 1, 'BOM-PROD-001-0001', 'PROD-001', NULL, 'MAT-001', '主板', 'PCB主板', '件', 1.0000, 1, NULL, 1, 1, NULL, '', 'DRAFT', 'V1.0', '系统'),
  (1002, 1, 'BOM-PROD-001-0002', 'PROD-001', NULL, 'MAT-002', '屏幕模组', 'AMOLED', '件', 1.0000, 1, NULL, 2, 1, NULL, '', 'DRAFT', 'V1.0', '系统'),
  (1003, 1, 'BOM-PROD-001-0003', 'PROD-001', NULL, 'MAT-003', '电池', '锂电池 300mAh', '件', 1.0000, 1, NULL, 3, 0, NULL, '', 'DRAFT', 'V1.0', '系统'),
  (1004, 1, 'BOM-PROD-001-0004', 'PROD-001', NULL, 'MAT-004', '螺丝', 'M1.4', '个', 4.0000, 2, 1001, 1, 0, NULL, '', 'DRAFT', 'V1.0', '系统')
ON DUPLICATE KEY UPDATE
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_document (id, doc_code, title, doc_type, category, version, status, author, file_name, file_path, file_size, file_format, remark)
VALUES
  (1, 'DOC-2024-001', '智能手表产品规格书', 'spec', '产品文档', 'V1.0', 'approved', '系统', 'spec_PROD-001_V1.0.pdf', '/data/plm/files/spec_PROD-001_V1.0.pdf', '2.5MB', 'pdf', ''),
  (2, 'DOC-2024-002', '智能手表装配工艺指导书', 'guide', '工艺文档', 'V1.0', 'draft', '系统', 'guide_PROD-001_V1.0.pdf', '/data/plm/files/guide_PROD-001_V1.0.pdf', '1.8MB', 'pdf', '')
ON DUPLICATE KEY UPDATE
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_project (id, project_code, project_name, manager, project_type, status, progress, start_date, end_date, created_by)
VALUES
  (1, 'PRJ-001', '智能手表研发项目', '张三', '研发', 'planning', 10, '2026-01-01', '2026-03-31', '系统')
ON DUPLICATE KEY UPDATE
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_task (id, project_id, task_name, assignee, progress, start_date, end_date, duration, parent_id, task_type, status, created_by)
VALUES
  (101, 1, '需求评审', '李四', 100, '2026-01-01', '2026-01-05', 5, NULL, 'task', 'COMPLETED', '系统'),
  (102, 1, '硬件设计', '王五', 30, '2026-01-06', '2026-01-31', 26, NULL, 'task', 'OPEN', '系统'),
  (103, 1, '软件开发', '赵六', 20, '2026-01-10', '2026-02-28', 50, NULL, 'task', 'OPEN', '系统')
ON DUPLICATE KEY UPDATE
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_gantt_link (id, project_id, source_task_id, target_task_id, link_type)
VALUES
  (201, 1, 101, 102, '0'),
  (202, 1, 102, 103, '0')
ON DUPLICATE KEY UPDATE
  link_type = VALUES(link_type);

INSERT INTO plm_resource_load (project_id, resource_id, resource_name, load_value, load_date)
VALUES
  (1, 'R-001', '硬件工程师', 60, '2026-01-15'),
  (1, 'R-002', '软件工程师', 40, '2026-01-15')
ON DUPLICATE KEY UPDATE
  load_value = VALUES(load_value);

INSERT INTO plm_process_route (id, product_id, route_code, route_name, product_code, product_name, route_type, version, status, create_user, steps_json)
VALUES
  (1, 1, 'PROC-001', '智能手表组装工艺', 'PROD-001', '智能手表', 'assembly', 'V1.0', 'active', '系统', '[{\"id\":\"1\",\"name\":\"PCB板贴装\",\"equipment\":\"贴片机\",\"operator\":\"李四\",\"time\":30,\"standard\":\"IPC-A-610E\"}]')
ON DUPLICATE KEY UPDATE
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_process_file (id, product_id, file_code, title, file_type, category, file_name, file_path, version, status, author, file_size, file_format, remark)
VALUES
  (1, 1, 'PF-001', '装配工艺指导书', 'guide', '工艺文件', 'process_guide.pdf', '/data/plm/files/process_guide.pdf', 'V1.0', 'active', '系统', '1.8MB', 'pdf', '')
ON DUPLICATE KEY UPDATE
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_change_request (id, product_id, change_code, title, description, change_type, process_code, process_name, reason, content, status, created_by, apply_user, apply_time)
VALUES
  (1, 1, 'CR-001', '工艺路线变更-增加外观检测', '', '工艺路线变更', 'PROC-001', '智能手表组装工艺', '提升良率', '增加外观检测工序', 'in-progress', '系统', '系统', '2026-01-20')
ON DUPLICATE KEY UPDATE
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_trial_plan (id, product_id, plan_code, plan_name, product_code, product_name, version, trial_type, trial_qty, departments_json, stages_json, responsible_person, status, start_date, end_date)
VALUES
  (1, 1, 'TP-001', '智能手表试产计划', 'PROD-001', '智能手表', 'V1.0', '小批量', 1000, '[\"研发\",\"制造\",\"质量\"]', '[{\"id\":\"1\",\"name\":\"试产准备\",\"status\":\"pending\"}]', '张三', 'pending', '2026-02-01', '2026-02-15')
ON DUPLICATE KEY UPDATE
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_trial_report (id, plan_id, report_code, report_title, content, status, create_user, create_time, main_issues_json, improvements_json)
VALUES
  (1, 1, 'TR-001', '智能手表试产报告', '试产总结内容', 'draft', '系统', '2026-02-16', '[\"外观刮伤\"]', '[\"增加防护膜\"]')
ON DUPLICATE KEY UPDATE
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_quality_issue (id, product_id, issue_code, issue_title, severity, status, resolution, create_user, create_time)
VALUES
  (1, 1, 'QI-001', '外观刮伤比例偏高', 'high', 'in-progress', '', '系统', '2026-02-10')
ON DUPLICATE KEY UPDATE
  updated_time = CURRENT_TIMESTAMP;
