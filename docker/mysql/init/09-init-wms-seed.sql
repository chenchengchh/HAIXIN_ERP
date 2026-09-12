-- ========================================
-- WMS 模块（hxcoe003）基础数据脚本
-- 说明：用于 Docker MySQL 初始化阶段注入 WMS 最小可用数据
-- ========================================
USE wms_db;

INSERT INTO wms_warehouse (id, warehouse_code, warehouse_name, address, manager, contact, status, created_time, updated_time)
VALUES
  (1, 'MAIN_WH', '主仓库', '园区A-1号库', 'admin', '0000', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  warehouse_name = VALUES(warehouse_name),
  address = VALUES(address),
  manager = VALUES(manager),
  contact = VALUES(contact),
  status = VALUES(status),
  updated_time = NOW();

INSERT INTO wms_zone (id, warehouse_code, zone_code, zone_name, zone_type, description, status, created_time, updated_time)
VALUES
  (1, 'MAIN_WH', 'ZONE_A', 'A区', '1', '默认分区 A', '1', NOW(), NOW()),
  (2, 'MAIN_WH', 'ZONE_B', 'B区', '1', '默认分区 B', '1', NOW(), NOW()),
  (3, 'MAIN_WH', 'ZONE_C', 'C区', '1', '默认分区 C', '1', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  warehouse_code = VALUES(warehouse_code),
  zone_name = VALUES(zone_name),
  zone_type = VALUES(zone_type),
  description = VALUES(description),
  status = VALUES(status),
  updated_time = NOW();

INSERT INTO wms_location_type (id, type_code, type_name, type_desc, max_weight, mix_flag, status, created_time, updated_time)
VALUES
  (1, 'NORMAL', '普通库位', '默认库位类型', 0.0000, b'1', '1', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  type_name = VALUES(type_name),
  type_desc = VALUES(type_desc),
  max_weight = VALUES(max_weight),
  mix_flag = VALUES(mix_flag),
  status = VALUES(status),
  updated_time = NOW();

INSERT INTO wms_material_type (id, type_code, type_name, description, status, created_time, updated_time)
VALUES
  (1, 'RAW', '原材料', '默认物料类型：原材料', '1', NOW(), NOW()),
  (2, 'FG', '成品', '默认物料类型：成品', '1', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  type_name = VALUES(type_name),
  description = VALUES(description),
  status = VALUES(status),
  updated_time = NOW();

INSERT INTO wms_barcode_rule (id, rule_code, rule_name, rule_type, rule_format, description, status, created_time, updated_time)
VALUES
  (1, 'MAT_DEFAULT', '物料条码默认规则', 'material', '{materialCode}', '默认条码格式', '1', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  rule_name = VALUES(rule_name),
  rule_type = VALUES(rule_type),
  rule_format = VALUES(rule_format),
  description = VALUES(description),
  status = VALUES(status),
  updated_time = NOW();

INSERT INTO wms_location (id, warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
VALUES
  (1, 'MAIN_WH', 'ZONE_A', 'A-01-01', 'A区-01-01', 'NORMAL', '1', '', NOW(), NOW()),
  (2, 'MAIN_WH', 'ZONE_A', 'A-01-02', 'A区-01-02', 'NORMAL', '1', '', NOW(), NOW()),
  (3, 'MAIN_WH', 'ZONE_B', 'B-01-01', 'B区-01-01', 'NORMAL', '1', '', NOW(), NOW()),
  (4, 'MAIN_WH', 'ZONE_C', 'C-01-01', 'C区-01-01', 'NORMAL', '1', '', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  warehouse_code = VALUES(warehouse_code),
  zone_code = VALUES(zone_code),
  location_name = VALUES(location_name),
  location_type_code = VALUES(location_type_code),
  status = VALUES(status),
  remark = VALUES(remark),
  updated_time = NOW();

INSERT INTO wms_inventory (id, warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, created_time, updated_time)
VALUES
  (1, 1, 'MAIN_WH', 'A-01-01', 'MAT-001', '示例物料-1', 100.0000, 'PCS', 'BATCH-001', NOW(), NOW()),
  (2, 1, 'MAIN_WH', 'B-01-01', 'MAT-002', '示例物料-2', 50.0000, 'PCS', 'BATCH-002', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  warehouse_id = VALUES(warehouse_id),
  warehouse_code = VALUES(warehouse_code),
  location_code = VALUES(location_code),
  material_name = VALUES(material_name),
  quantity = VALUES(quantity),
  unit = VALUES(unit),
  batch_no = VALUES(batch_no),
  updated_time = NOW();
