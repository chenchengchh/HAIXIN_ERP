-- ========================================
-- BOM 模块基础数据脚本
-- 说明：用于分域改造后的最小可用种子数据
-- ========================================
USE bom_db;

INSERT INTO bom_category (
  id, name, code, parent_id, description, status, remark, created_by, created_time
) VALUES
  (1, 'Electronics', 'ELEC', NULL, 'Electronic materials', 1, '', 'system', NOW()),
  (2, 'Fastener', 'FAST', NULL, 'Fastener materials', 1, '', 'system', NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  status = VALUES(status),
  updated_time = NOW();

INSERT INTO bom_material (
  id, material_code, material_name, material_spec, material_type, unit,
  category_id, category_name, unit_price, attr_json, description, status,
  created_by, created_time, updated_by, updated_time, remark
) VALUES
  (1, 'PROD-001', 'Smart Watch', '42mm', 'FIN', 'pcs', 1, 'Electronics', 499.000000, '{}', 'Finished product', 'ACTIVE', 'system', NOW(), 'system', NOW(), ''),
  (2, 'MAT-001', 'Main Board', 'PCB', 'RAW', 'pcs', 1, 'Electronics', 120.000000, '{}', 'Board assembly', 'ACTIVE', 'system', NOW(), 'system', NOW(), ''),
  (3, 'MAT-002', 'Screen Module', 'AMOLED', 'RAW', 'pcs', 1, 'Electronics', 80.000000, '{}', 'Display module', 'ACTIVE', 'system', NOW(), 'system', NOW(), ''),
  (4, 'MAT-003', 'Battery', '300mAh', 'RAW', 'pcs', 1, 'Electronics', 15.000000, '{}', 'Battery pack', 'ACTIVE', 'system', NOW(), 'system', NOW(), ''),
  (5, 'MAT-004', 'Screw', 'M1.4', 'RAW', 'ea', 2, 'Fastener', 0.050000, '{}', 'Assembly screw', 'ACTIVE', 'system', NOW(), 'system', NOW(), '')
ON DUPLICATE KEY UPDATE
  material_name = VALUES(material_name),
  material_spec = VALUES(material_spec),
  material_type = VALUES(material_type),
  unit = VALUES(unit),
  category_id = VALUES(category_id),
  category_name = VALUES(category_name),
  unit_price = VALUES(unit_price),
  attr_json = VALUES(attr_json),
  description = VALUES(description),
  status = VALUES(status),
  updated_by = VALUES(updated_by),
  updated_time = NOW(),
  remark = VALUES(remark);

INSERT INTO bom_header (
  id, material_id, material_code, material_name, bom_code, version,
  type, status, is_default, effective_date, expire_date, remark,
  created_by, created_time, updated_by, updated_time
) VALUES
  (1, 1, 'PROD-001', 'Smart Watch', 'EBOM-PROD-001-V1.0', 'V1.0',
   1, 1, 1, NOW(), NULL, '', 'system', NOW(), 'system', NOW())
ON DUPLICATE KEY UPDATE
  material_id = VALUES(material_id),
  material_code = VALUES(material_code),
  material_name = VALUES(material_name),
  version = VALUES(version),
  type = VALUES(type),
  status = VALUES(status),
  is_default = VALUES(is_default),
  effective_date = VALUES(effective_date),
  expire_date = VALUES(expire_date),
  updated_by = VALUES(updated_by),
  updated_time = NOW();

INSERT INTO bom_line (
  id, header_id, parent_material_id, child_material_id, child_material_code, child_material_name,
  quantity, unit, scrap_rate, level, effective_date, expire_date, sequence, usage_type, remark
) VALUES
  (1, 1, 1, 2, 'MAT-001', 'Main Board', 1.000000, 'pcs', 0.000000, 1, NOW(), NULL, 1, 'assembly', ''),
  (2, 1, 1, 3, 'MAT-002', 'Screen Module', 1.000000, 'pcs', 0.000000, 1, NOW(), NULL, 2, 'assembly', ''),
  (3, 1, 1, 4, 'MAT-003', 'Battery', 1.000000, 'pcs', 0.000000, 1, NOW(), NULL, 3, 'assembly', ''),
  (4, 1, 1, 5, 'MAT-004', 'Screw', 4.000000, 'ea', 0.000000, 1, NOW(), NULL, 4, 'assembly', '')
ON DUPLICATE KEY UPDATE
  parent_material_id = VALUES(parent_material_id),
  child_material_code = VALUES(child_material_code),
  child_material_name = VALUES(child_material_name),
  quantity = VALUES(quantity),
  unit = VALUES(unit),
  scrap_rate = VALUES(scrap_rate),
  level = VALUES(level),
  effective_date = VALUES(effective_date),
  expire_date = VALUES(expire_date),
  sequence = VALUES(sequence),
  usage_type = VALUES(usage_type),
  remark = VALUES(remark);
