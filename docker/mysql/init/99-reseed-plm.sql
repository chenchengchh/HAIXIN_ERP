CREATE DATABASE IF NOT EXISTS plm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE plm_db;

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

INSERT INTO plm_product (id, product_code, product_name, product_model, product_spec, product_type, product_category, unit, status, version, description, created_by)
VALUES
  (1, 'PROD-001', '智能手表', 'SW-100', '42mm/蓝牙/WiFi', 'finished', 'wearable', '件', 'ACTIVE', 'V1.0', '智能穿戴产品', '系统'),
  (2, 'PROD-002', '锂电池电芯', 'BAT-18650', '3.7V/2600mAh', 'raw', 'battery', '件', 'ACTIVE', 'V1.0', '动力电池电芯', '系统')
ON DUPLICATE KEY UPDATE
  product_name = VALUES(product_name),
  product_model = VALUES(product_model),
  product_spec = VALUES(product_spec),
  product_type = VALUES(product_type),
  product_category = VALUES(product_category),
  unit = VALUES(unit),
  status = VALUES(status),
  version = VALUES(version),
  description = VALUES(description),
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_document (id, doc_code, title, doc_type, category, version, status, author, file_name, file_path, file_size, file_format, remark)
VALUES
  (1, 'DOC-2024-001', '智能手表产品规格书', 'spec', '产品文档', 'V1.0', 'approved', '系统', 'spec_PROD-001_V1.0.pdf', '/data/plm/files/spec_PROD-001_V1.0.pdf', '2.5MB', 'pdf', ''),
  (2, 'DOC-2024-002', '智能手表装配工艺指导书', 'guide', '工艺文档', 'V1.0', 'draft', '系统', 'guide_PROD-001_V1.0.pdf', '/data/plm/files/guide_PROD-001_V1.0.pdf', '1.8MB', 'pdf', '')
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  doc_type = VALUES(doc_type),
  category = VALUES(category),
  version = VALUES(version),
  status = VALUES(status),
  author = VALUES(author),
  file_name = VALUES(file_name),
  file_path = VALUES(file_path),
  file_size = VALUES(file_size),
  file_format = VALUES(file_format),
  remark = VALUES(remark),
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO plm_bom (id, product_id, bom_code, product_code, child_component_code, material_code, material_name, material_spec, unit, quantity, level, parent_id, sort_order, is_key_part, substitute_material, remark, status, version, created_by)
VALUES
  (1001, 1, 'BOM-PROD-001-0001', 'PROD-001', NULL, 'MAT-001', '主板', 'PCB主板', '件', 1.0000, 1, NULL, 1, 1, NULL, '', 'DRAFT', 'V1.0', '系统'),
  (1002, 1, 'BOM-PROD-001-0002', 'PROD-001', NULL, 'MAT-002', '屏幕模组', 'AMOLED', '件', 1.0000, 1, NULL, 2, 1, NULL, '', 'DRAFT', 'V1.0', '系统'),
  (1003, 1, 'BOM-PROD-001-0003', 'PROD-001', NULL, 'MAT-003', '电池', '锂电池 300mAh', '件', 1.0000, 1, NULL, 3, 0, NULL, '', 'DRAFT', 'V1.0', '系统'),
  (1004, 1, 'BOM-PROD-001-0004', 'PROD-001', NULL, 'MAT-004', '螺丝', 'M1.4', '个', 4.0000, 2, 1001, 1, 0, NULL, '', 'DRAFT', 'V1.0', '系统')
ON DUPLICATE KEY UPDATE
  quantity = VALUES(quantity),
  level = VALUES(level),
  parent_id = VALUES(parent_id),
  sort_order = VALUES(sort_order),
  is_key_part = VALUES(is_key_part),
  substitute_material = VALUES(substitute_material),
  remark = VALUES(remark),
  status = VALUES(status),
  version = VALUES(version),
  updated_time = CURRENT_TIMESTAMP;

-- Rebuild BOM seed data into the owner schema instead of the legacy shared schema.
CREATE DATABASE IF NOT EXISTS bom_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bom_db;

CREATE TABLE IF NOT EXISTS bom_material (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  material_code VARCHAR(50) NOT NULL,
  material_name VARCHAR(100) NOT NULL,
  material_spec VARCHAR(200),
  material_type VARCHAR(50),
  unit VARCHAR(20),
  category_id BIGINT,
  category_name VARCHAR(100),
  unit_price DECIMAL(18,6),
  attr_json LONGTEXT,
  description VARCHAR(500),
  status VARCHAR(20),
  created_by VARCHAR(50),
  created_time DATETIME,
  updated_by VARCHAR(50),
  updated_time DATETIME,
  remark VARCHAR(500),
  UNIQUE KEY uk_material_code (material_code),
  INDEX idx_material_name (material_name),
  INDEX idx_material_type (material_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS bom_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(50) NOT NULL,
  parent_id BIGINT,
  description VARCHAR(500),
  status INT,
  remark VARCHAR(500),
  created_by VARCHAR(50),
  created_time DATETIME,
  updated_by VARCHAR(50),
  updated_time DATETIME,
  UNIQUE KEY uk_category_code (code),
  INDEX idx_category_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS bom_header (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  material_id BIGINT NOT NULL,
  material_code VARCHAR(50),
  material_name VARCHAR(100),
  bom_code VARCHAR(50) NOT NULL,
  version VARCHAR(20) NOT NULL,
  type INT,
  status INT,
  is_default TINYINT(1),
  effective_date DATETIME,
  expire_date DATETIME,
  remark VARCHAR(500),
  created_by VARCHAR(50),
  created_time DATETIME,
  updated_by VARCHAR(50),
  updated_time DATETIME,
  UNIQUE KEY uk_bom_code (bom_code),
  INDEX idx_header_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS bom_line (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  header_id BIGINT NOT NULL,
  parent_material_id BIGINT,
  child_material_id BIGINT NOT NULL,
  child_material_code VARCHAR(50),
  child_material_name VARCHAR(100),
  quantity DOUBLE NOT NULL,
  unit VARCHAR(20),
  scrap_rate DOUBLE,
  level INT NOT NULL,
  effective_date DATETIME,
  expire_date DATETIME,
  sequence INT,
  usage_type VARCHAR(50),
  remark VARCHAR(500),
  UNIQUE KEY uk_header_child (header_id, child_material_id),
  INDEX idx_line_header (header_id),
  INDEX idx_line_child (child_material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE DATABASE IF NOT EXISTS `aps_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `hr_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `oa_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `crm_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `erp_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `bom_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `wms_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `scada_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ems_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `agv_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `mes_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `les_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `qms_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `scrm_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `srm_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `scm_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `opportunity_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `eam_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mysql;

CREATE USER IF NOT EXISTS 'aps_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'hr_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'oa_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'crm_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'erp_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'bom_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'wms_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'scada_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'ems_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'agv_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'mes_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'les_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'qms_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'scrm_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'srm_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'scm_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'plm_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'opportunity_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'eam_app'@'%' IDENTIFIED BY 'root';

GRANT ALL PRIVILEGES ON `aps_db`.* TO 'aps_app'@'%';
GRANT ALL PRIVILEGES ON `hr_db`.* TO 'hr_app'@'%';
GRANT ALL PRIVILEGES ON `oa_db`.* TO 'oa_app'@'%';
GRANT ALL PRIVILEGES ON `crm_db`.* TO 'crm_app'@'%';
GRANT ALL PRIVILEGES ON `erp_db`.* TO 'erp_app'@'%';
GRANT ALL PRIVILEGES ON `bom_db`.* TO 'bom_app'@'%';
GRANT ALL PRIVILEGES ON `wms_db`.* TO 'wms_app'@'%';
GRANT ALL PRIVILEGES ON `scada_db`.* TO 'scada_app'@'%';
GRANT ALL PRIVILEGES ON `ems_db`.* TO 'ems_app'@'%';
GRANT ALL PRIVILEGES ON `agv_db`.* TO 'agv_app'@'%';
GRANT ALL PRIVILEGES ON `mes_db`.* TO 'mes_app'@'%';
GRANT ALL PRIVILEGES ON `les_db`.* TO 'les_app'@'%';
GRANT ALL PRIVILEGES ON `qms_db`.* TO 'qms_app'@'%';
GRANT ALL PRIVILEGES ON `scrm_db`.* TO 'scrm_app'@'%';
GRANT ALL PRIVILEGES ON `srm_db`.* TO 'srm_app'@'%';
GRANT ALL PRIVILEGES ON `scm_db`.* TO 'scm_app'@'%';
GRANT ALL PRIVILEGES ON `plm_db`.* TO 'plm_app'@'%';
GRANT ALL PRIVILEGES ON `opportunity_db`.* TO 'opportunity_app'@'%';
GRANT ALL PRIVILEGES ON `eam_db`.* TO 'eam_app'@'%';

FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS bom_substitute (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  main_material_id BIGINT NOT NULL,
  sub_material_id BIGINT NOT NULL,
  bom_line_id BIGINT,
  ratio DOUBLE,
  priority INT,
  status INT,
  remark VARCHAR(500),
  UNIQUE KEY uk_substitute (main_material_id, sub_material_id, bom_line_id),
  INDEX idx_sub_main (main_material_id),
  INDEX idx_sub_line (bom_line_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_material' AND COLUMN_NAME='category_id'), 'SELECT 1', 'ALTER TABLE bom_material ADD COLUMN category_id BIGINT')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_material' AND COLUMN_NAME='category_name'), 'SELECT 1', 'ALTER TABLE bom_material ADD COLUMN category_name VARCHAR(100)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_material' AND COLUMN_NAME='unit_price'), 'SELECT 1', 'ALTER TABLE bom_material ADD COLUMN unit_price DECIMAL(18,6)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_material' AND COLUMN_NAME='attr_json'), 'SELECT 1', 'ALTER TABLE bom_material ADD COLUMN attr_json LONGTEXT')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_material' AND COLUMN_NAME='description'), 'SELECT 1', 'ALTER TABLE bom_material ADD COLUMN description VARCHAR(500)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO bom_category (name, code, parent_id, description, status, remark, created_by, created_time)
VALUES
  ('电子件', 'ELEC', NULL, '电子类物料', 1, '', '系统', CURRENT_TIMESTAMP),
  ('结构件', 'MECH', NULL, '结构类物料', 1, '', '系统', CURRENT_TIMESTAMP),
  ('紧固件', 'FAST', NULL, '紧固类物料', 1, '', '系统', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  parent_id = VALUES(parent_id),
  description = VALUES(description),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO bom_category (name, code, parent_id, description, status, remark, created_by, created_time)
SELECT
  '螺丝',
  'SCREW',
  c.id,
  '螺丝分类',
  1,
  '',
  '系统',
  CURRENT_TIMESTAMP
FROM bom_category c
WHERE c.code = 'FAST'
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  parent_id = VALUES(parent_id),
  description = VALUES(description),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_time = CURRENT_TIMESTAMP;

SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_header' AND COLUMN_NAME='material_code'), 'SELECT 1', 'ALTER TABLE bom_header ADD COLUMN material_code VARCHAR(50)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_header' AND COLUMN_NAME='material_name'), 'SELECT 1', 'ALTER TABLE bom_header ADD COLUMN material_name VARCHAR(100)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_header' AND COLUMN_NAME='type'), 'SELECT 1', 'ALTER TABLE bom_header ADD COLUMN type INT')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_header' AND COLUMN_NAME='status'), 'SELECT 1', 'ALTER TABLE bom_header ADD COLUMN status INT')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_header' AND COLUMN_NAME='is_default'), 'SELECT 1', 'ALTER TABLE bom_header ADD COLUMN is_default TINYINT(1)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_header' AND COLUMN_NAME='effective_date'), 'SELECT 1', 'ALTER TABLE bom_header ADD COLUMN effective_date DATETIME')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_header' AND COLUMN_NAME='expire_date'), 'SELECT 1', 'ALTER TABLE bom_header ADD COLUMN expire_date DATETIME')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_line' AND COLUMN_NAME='parent_material_id'), 'SELECT 1', 'ALTER TABLE bom_line ADD COLUMN parent_material_id BIGINT')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_line' AND COLUMN_NAME='child_material_code'), 'SELECT 1', 'ALTER TABLE bom_line ADD COLUMN child_material_code VARCHAR(50)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_line' AND COLUMN_NAME='child_material_name'), 'SELECT 1', 'ALTER TABLE bom_line ADD COLUMN child_material_name VARCHAR(100)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_line' AND COLUMN_NAME='scrap_rate'), 'SELECT 1', 'ALTER TABLE bom_line ADD COLUMN scrap_rate DOUBLE')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_line' AND COLUMN_NAME='effective_date'), 'SELECT 1', 'ALTER TABLE bom_line ADD COLUMN effective_date DATETIME')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_line' AND COLUMN_NAME='expire_date'), 'SELECT 1', 'ALTER TABLE bom_line ADD COLUMN expire_date DATETIME')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_line' AND COLUMN_NAME='sequence'), 'SELECT 1', 'ALTER TABLE bom_line ADD COLUMN sequence INT')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_line' AND COLUMN_NAME='usage_type'), 'SELECT 1', 'ALTER TABLE bom_line ADD COLUMN usage_type VARCHAR(50)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_line' AND COLUMN_NAME='remark'), 'SELECT 1', 'ALTER TABLE bom_line ADD COLUMN remark VARCHAR(500)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_substitute' AND COLUMN_NAME='bom_line_id'), 'SELECT 1', 'ALTER TABLE bom_substitute ADD COLUMN bom_line_id BIGINT')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_substitute' AND COLUMN_NAME='ratio'), 'SELECT 1', 'ALTER TABLE bom_substitute ADD COLUMN ratio DOUBLE')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_substitute' AND COLUMN_NAME='priority'), 'SELECT 1', 'ALTER TABLE bom_substitute ADD COLUMN priority INT')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_substitute' AND COLUMN_NAME='status'), 'SELECT 1', 'ALTER TABLE bom_substitute ADD COLUMN status INT')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl := (SELECT IF(EXISTS(SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bom_db' AND TABLE_NAME='bom_substitute' AND COLUMN_NAME='remark'), 'SELECT 1', 'ALTER TABLE bom_substitute ADD COLUMN remark VARCHAR(500)')); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO bom_material (material_code, material_name, material_spec, material_type, unit, status, unit_price, remark, created_by, created_time)
VALUES
  ('PROD-001', '智能手表', '42mm/蓝牙/WiFi', 'FIN', '件', 'ACTIVE', 499.000000, '', '系统', CURRENT_TIMESTAMP),
  ('MAT-001', '主板', 'PCB主板', 'RAW', '件', 'ACTIVE', 120.000000, '', '系统', CURRENT_TIMESTAMP),
  ('MAT-002', '屏幕模组', 'AMOLED', 'RAW', '件', 'ACTIVE', 80.000000, '', '系统', CURRENT_TIMESTAMP),
  ('MAT-003', '电池', '锂电池 300mAh', 'RAW', '件', 'ACTIVE', 15.000000, '', '系统', CURRENT_TIMESTAMP),
  ('MAT-004', '螺丝', 'M1.4', 'RAW', '个', 'ACTIVE', 0.050000, '', '系统', CURRENT_TIMESTAMP),
  ('MAT-006', '电池(替代)', '锂电池 320mAh', 'RAW', '件', 'ACTIVE', 16.000000, '', '系统', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE
  material_name = VALUES(material_name),
  material_spec = VALUES(material_spec),
  material_type = VALUES(material_type),
  unit = VALUES(unit),
  status = VALUES(status),
  unit_price = VALUES(unit_price),
  remark = VALUES(remark),
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO bom_header (material_id, material_code, material_name, bom_code, version, type, status, is_default, created_by, created_time, remark)
SELECT
  m.id,
  m.material_code,
  m.material_name,
  'EBOM-PROD-001-V1.0',
  'V1.0',
  1,
  1,
  1,
  '系统',
  CURRENT_TIMESTAMP,
  '初始化EBOM示例'
FROM bom_material m
WHERE m.material_code = 'PROD-001'
ON DUPLICATE KEY UPDATE
  status = VALUES(status),
  is_default = VALUES(is_default),
  remark = VALUES(remark),
  updated_time = CURRENT_TIMESTAMP;

INSERT INTO bom_line (header_id, parent_material_id, child_material_id, child_material_code, child_material_name, quantity, unit, scrap_rate, level, sequence, usage_type, remark)
SELECT
  h.id,
  NULL,
  c.id,
  c.material_code,
  c.material_name,
  v.qty,
  v.unit,
  0,
  1,
  v.seq,
  'normal',
  ''
FROM bom_header h
JOIN (
  SELECT 'MAT-001' AS code, 1.0 AS qty, '件' AS unit, 1 AS seq UNION ALL
  SELECT 'MAT-002' AS code, 1.0 AS qty, '件' AS unit, 2 AS seq UNION ALL
  SELECT 'MAT-003' AS code, 1.0 AS qty, '件' AS unit, 3 AS seq
) v ON 1=1
JOIN bom_material c ON c.material_code = v.code
WHERE h.bom_code = 'EBOM-PROD-001-V1.0'
ON DUPLICATE KEY UPDATE
  quantity = VALUES(quantity),
  unit = VALUES(unit),
  sequence = VALUES(sequence),
  remark = VALUES(remark);

INSERT INTO bom_line (header_id, parent_material_id, child_material_id, child_material_code, child_material_name, quantity, unit, scrap_rate, level, sequence, usage_type, remark)
SELECT
  h.id,
  parent.id,
  screw.id,
  screw.material_code,
  screw.material_name,
  4.0,
  '个',
  0,
  2,
  1,
  'normal',
  ''
FROM bom_header h
JOIN bom_material parent ON parent.material_code = 'MAT-001'
JOIN bom_material screw ON screw.material_code = 'MAT-004'
WHERE h.bom_code = 'EBOM-PROD-001-V1.0'
ON DUPLICATE KEY UPDATE
  quantity = VALUES(quantity),
  unit = VALUES(unit),
  remark = VALUES(remark);

INSERT INTO bom_substitute (main_material_id, sub_material_id, bom_line_id, ratio, priority, status, remark)
SELECT
  main.id,
  sub.id,
  NULL,
  1.0,
  1,
  1,
  '初始化替代料示例'
FROM bom_material main
JOIN bom_material sub ON sub.material_code = 'MAT-006'
WHERE main.material_code = 'MAT-003'
ON DUPLICATE KEY UPDATE
  ratio = VALUES(ratio),
  priority = VALUES(priority),
  status = VALUES(status),
  remark = VALUES(remark);
