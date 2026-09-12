-- =============================================================
-- WMS 模块测试数据种子脚本（幂等，可重复执行）
-- 说明：容器 JVM 与 MySQL 会话均为 UTC，JDBC serverTimezone=Asia/Shanghai，
--       JPA 写入时会 +8h 存储。为与 JPA 写入数据保持一致，
--       本脚本所有时间统一使用 DATE_ADD(NOW(), INTERVAL 8 HOUR) 作为"当前时间"基准。
-- =============================================================

-- ---------- 1. 仓库 ----------
INSERT INTO wms_warehouse (warehouse_code, warehouse_name, address, manager, contact, status, created_time, updated_time)
SELECT 'WH001', '一号仓', '上海市浦东新区金桥路100号', '张仓库', '13800000001', 1, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_warehouse WHERE warehouse_code = 'WH001');

INSERT INTO wms_warehouse (warehouse_code, warehouse_name, address, manager, contact, status, created_time, updated_time)
SELECT 'WH002', '二号仓', '上海市嘉定区安亭镇园区路200号', '李仓管', '13800000002', 1, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_warehouse WHERE warehouse_code = 'WH002');

INSERT INTO wms_warehouse (warehouse_code, warehouse_name, address, manager, contact, status, created_time, updated_time)
SELECT 'WH-SPARE', '备件仓', '上海市浦东新区金桥路100号辅楼', '王备件', '13800000003', 1, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_warehouse WHERE warehouse_code = 'WH-SPARE');

-- ---------- 2. 库区 ----------
INSERT INTO wms_zone (warehouse_code, zone_code, zone_name, zone_type, description, status, created_time, updated_time)
SELECT 'WH001', 'Z001', '存储区', 'storage', '一号仓常规存储区', '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_zone WHERE zone_code = 'Z001');

INSERT INTO wms_zone (warehouse_code, zone_code, zone_name, zone_type, description, status, created_time, updated_time)
SELECT 'WH001', 'Z002', '拣选区', 'picking', '一号仓拣选作业区', '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_zone WHERE zone_code = 'Z002');

INSERT INTO wms_zone (warehouse_code, zone_code, zone_name, zone_type, description, status, created_time, updated_time)
SELECT 'WH002', 'Z003', '存储区', 'storage', '二号仓常规存储区', '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_zone WHERE zone_code = 'Z003');

-- ---------- 3. 库位类型 ----------
INSERT INTO wms_location_type (type_code, type_name, type_desc, max_weight, mix_flag, status, created_time, updated_time)
SELECT 'LT01', '标准库位', '标准货架库位，承重1000kg', 1000.0, 1, '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location_type WHERE type_code = 'LT01');

INSERT INTO wms_location_type (type_code, type_name, type_desc, max_weight, mix_flag, status, created_time, updated_time)
SELECT 'LT02', '重型库位', '重型货架库位，承重5000kg', 5000.0, 0, '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location_type WHERE type_code = 'LT02');

INSERT INTO wms_location_type (type_code, type_name, type_desc, max_weight, mix_flag, status, created_time, updated_time)
SELECT 'LT03', '拣选库位', '拣选作业专用库位，承重500kg', 500.0, 1, '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location_type WHERE type_code = 'LT03');

-- ---------- 4. 物料类型 ----------
INSERT INTO wms_material_type (type_code, type_name, description, status, created_time, updated_time)
SELECT 'MT01', '原材料', '生产用原材料', '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_material_type WHERE type_code = 'MT01');

INSERT INTO wms_material_type (type_code, type_name, description, status, created_time, updated_time)
SELECT 'MT02', '半成品', '在制半成品', '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_material_type WHERE type_code = 'MT02');

INSERT INTO wms_material_type (type_code, type_name, description, status, created_time, updated_time)
SELECT 'MT03', '产成品', '完工产成品', '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_material_type WHERE type_code = 'MT03');

INSERT INTO wms_material_type (type_code, type_name, description, status, created_time, updated_time)
SELECT 'MT04', '备品备件', '设备备品备件', '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_material_type WHERE type_code = 'MT04');

-- ---------- 5. 条码规则 ----------
INSERT INTO wms_barcode_rule (rule_code, rule_name, rule_type, rule_format, description, status, created_time, updated_time)
SELECT 'BR01', '物料条码规则', 'material', 'MAT{yyyy}{MM}{dd}{####}', '物料编码生成规则：前缀MAT+日期+4位流水', '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_barcode_rule WHERE rule_code = 'BR01');

INSERT INTO wms_barcode_rule (rule_code, rule_name, rule_type, rule_format, description, status, created_time, updated_time)
SELECT 'BR02', '库位条码规则', 'location', '{zone}-{####}', '库位条码：库区编码+4位流水', '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_barcode_rule WHERE rule_code = 'BR02');

INSERT INTO wms_barcode_rule (rule_code, rule_name, rule_type, rule_format, description, status, created_time, updated_time)
SELECT 'BR03', '批次条码规则', 'batch', 'BATCH-{yyyy}{MM}-{###}', '批次条码：前缀BATCH+年月+3位流水', '1', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_barcode_rule WHERE rule_code = 'BR03');

-- ---------- 6. 库位 ----------
INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH001', 'Z001', 'Z001-0004', '库位0004', 'LT01', '1', '', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z001-0004');
INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH001', 'Z001', 'Z001-0005', '库位0005', 'LT01', '1', '', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z001-0005');
INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH001', 'Z001', 'Z001-0006', '库位0006', 'LT02', '1', '重型货架', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z001-0006');
INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH001', 'Z002', 'Z002-0001', '拣选位0001', 'LT03', '1', '', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z002-0001');
INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH001', 'Z002', 'Z002-0002', '拣选位0002', 'LT03', '1', '', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z002-0002');
INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH002', 'Z003', 'Z003-0001', '库位3001', 'LT01', '1', '', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z003-0001');
INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH002', 'Z003', 'Z003-0002', '库位3002', 'LT01', '1', '', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z003-0002');
INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH-SPARE', 'Z001', 'Z001-SP01', '备件位01', 'LT01', '1', '', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z001-SP01');

-- ---------- 7. 库存（含低库存/超储/冻结，用于预警测试） ----------
-- 阈值：低库存 <10，超储 >1000
INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH001', 'Z001-0001', 'MAT001', '示例物料', 150.00, 'PCS', 'BATCH-001', 'NORMAL', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH001' AND i.location_code='Z001-0001' AND i.material_code='MAT001' AND i.batch_no='BATCH-001');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH001', 'Z001-0002', 'RAW-AL-001', '铝合金型材', 500.00, 'KG', 'BATCH-202607', 'NORMAL', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH001' AND i.location_code='Z001-0002' AND i.material_code='RAW-AL-001' AND i.batch_no='BATCH-202607');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH001', 'Z001-0003', 'RAW-STL-002', '不锈钢板', 8.00, '张', 'BATCH-202607', 'NORMAL', '库存偏低需补货', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH001' AND i.location_code='Z001-0003' AND i.material_code='RAW-STL-002' AND i.batch_no='BATCH-202607');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH001', 'Z001-0004', 'SEMI-MOTOR-003', '电机半成品', 60.00, '台', 'BATCH-A01', 'NORMAL', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH001' AND i.location_code='Z001-0004' AND i.material_code='SEMI-MOTOR-003' AND i.batch_no='BATCH-A01');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH001', 'Z001-0005', 'P-GEAR-002', '齿轮组件', 1200.00, '件', 'BATCH-G05', 'NORMAL', '超储库存', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH001' AND i.location_code='Z001-0005' AND i.material_code='P-GEAR-002' AND i.batch_no='BATCH-G05');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH001', 'Z001-0006', 'MAT-PKG-001', '包装箱', 3000.00, '个', 'BATCH-P09', 'NORMAL', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH001' AND i.location_code='Z001-0006' AND i.material_code='MAT-PKG-001' AND i.batch_no='BATCH-P09');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH001', 'Z002-0001', 'P-PROD-002', '智能控制器', 5.00, '台', 'BATCH-C11', 'NORMAL', '低库存预警', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH001' AND i.location_code='Z002-0001' AND i.material_code='P-PROD-002' AND i.batch_no='BATCH-C11');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH001', 'Z002-0002', 'RAW-CU-004', '铜排', 220.00, 'KG', 'BATCH-K02', 'FROZEN', '质检待判定，临时冻结', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH001' AND i.location_code='Z002-0002' AND i.material_code='RAW-CU-004' AND i.batch_no='BATCH-K02');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH002', 'Z003-0001', 'P-PROD-001', '精密减速机', 85.00, '台', 'BATCH-E2E', 'NORMAL', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH002'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH002' AND i.location_code='Z003-0001' AND i.material_code='P-PROD-001' AND i.batch_no='BATCH-E2E');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH002', 'Z003-0002', 'RAW-PL-005', '工程塑料粒子', 760.00, 'KG', 'BATCH-Q07', 'NORMAL', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH002'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH002' AND i.location_code='Z003-0002' AND i.material_code='RAW-PL-005' AND i.batch_no='BATCH-Q07');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, status, remark, created_time, updated_time)
SELECT w.id, 'WH-SPARE', 'Z001-SP01', 'SP-SEAL-002', '密封圈', 430.00, '件', 'BATCH-S03', 'NORMAL', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code = 'WH-SPARE'
  AND NOT EXISTS (SELECT 1 FROM wms_inventory i WHERE i.warehouse_code='WH-SPARE' AND i.location_code='Z001-SP01' AND i.material_code='SP-SEAL-002' AND i.batch_no='BATCH-S03');

-- 更新历史备件库存的状态字段
UPDATE wms_inventory SET status = 'NORMAL' WHERE status IS NULL;

-- ---------- 8. ASN 入库通知单 ----------
INSERT INTO wms_asn (asn_no, delivery_note_no, supplier_code, supplier_name, warehouse_id, warehouse_code, warehouse_name, status, expected_arrival_date, actual_arrival_date, created_time, updated_time)
SELECT 'ASN-20260730-001', 'DN-001', 'SUP001', '上海精密五金有限公司', w.id, 'WH001', '一号仓', 'RECEIVED',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY),
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -3 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
FROM wms_warehouse w WHERE w.warehouse_code='WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_asn WHERE asn_no='ASN-20260730-001');

INSERT INTO wms_asn (asn_no, delivery_note_no, supplier_code, supplier_name, warehouse_id, warehouse_code, warehouse_name, status, expected_arrival_date, actual_arrival_date, created_time, updated_time)
SELECT 'ASN-20260731-002', 'DN-002', 'SUP002', '苏州电子元件厂', w.id, 'WH001', '一号仓', 'RECEIVING',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY), NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY)
FROM wms_warehouse w WHERE w.warehouse_code='WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_asn WHERE asn_no='ASN-20260731-002');

INSERT INTO wms_asn (asn_no, delivery_note_no, supplier_code, supplier_name, warehouse_id, warehouse_code, warehouse_name, status, expected_arrival_date, actual_arrival_date, created_time, updated_time)
SELECT 'ASN-20260801-003', 'DN-003', 'SUP003', '东莞塑胶制品公司', w.id, 'WH002', '二号仓', 'CREATED',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL 1 DAY), NULL,
       DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code='WH002'
  AND NOT EXISTS (SELECT 1 FROM wms_asn WHERE asn_no='ASN-20260801-003');

INSERT INTO wms_asn (asn_no, delivery_note_no, supplier_code, supplier_name, warehouse_id, warehouse_code, warehouse_name, status, expected_arrival_date, actual_arrival_date, created_time, updated_time)
SELECT 'ASN-20260801-004', 'DN-004', 'SUP001', '上海精密五金有限公司', w.id, 'WH001', '一号仓', 'PARTIAL_RECEIVED',
       DATE_ADD(NOW(), INTERVAL 8 HOUR), NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_warehouse w WHERE w.warehouse_code='WH001'
  AND NOT EXISTS (SELECT 1 FROM wms_asn WHERE asn_no='ASN-20260801-004');

INSERT INTO wms_asn (asn_no, delivery_note_no, supplier_code, supplier_name, warehouse_id, warehouse_code, warehouse_name, status, expected_arrival_date, actual_arrival_date, created_time, updated_time)
SELECT 'ASN-20260728-005', 'DN-005', 'SUP004', '宁波包装材料厂', w.id, 'WH002', '二号仓', 'CANCELLED',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY), NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -5 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY)
FROM wms_warehouse w WHERE w.warehouse_code='WH002'
  AND NOT EXISTS (SELECT 1 FROM wms_asn WHERE asn_no='ASN-20260728-005');

-- ---------- 9. ASN 明细 ----------
INSERT INTO wms_asn_item (asn_id, material_code, material_name, expected_quantity, received_quantity, unit, batch_no)
SELECT a.id, 'RAW-AL-001', '铝合金型材', 500.00, 500.00, 'KG', 'BATCH-202607' FROM wms_asn a WHERE a.asn_no='ASN-20260730-001'
  AND NOT EXISTS (SELECT 1 FROM wms_asn_item i WHERE i.asn_id=a.id AND i.material_code='RAW-AL-001');
INSERT INTO wms_asn_item (asn_id, material_code, material_name, expected_quantity, received_quantity, unit, batch_no)
SELECT a.id, 'RAW-STL-002', '不锈钢板', 100.00, 100.00, '张', 'BATCH-202607' FROM wms_asn a WHERE a.asn_no='ASN-20260730-001'
  AND NOT EXISTS (SELECT 1 FROM wms_asn_item i WHERE i.asn_id=a.id AND i.material_code='RAW-STL-002');
INSERT INTO wms_asn_item (asn_id, material_code, material_name, expected_quantity, received_quantity, unit, batch_no)
SELECT a.id, 'SEMI-MOTOR-003', '电机半成品', 60.00, 30.00, '台', 'BATCH-A01' FROM wms_asn a WHERE a.asn_no='ASN-20260731-002'
  AND NOT EXISTS (SELECT 1 FROM wms_asn_item i WHERE i.asn_id=a.id AND i.material_code='SEMI-MOTOR-003');
INSERT INTO wms_asn_item (asn_id, material_code, material_name, expected_quantity, received_quantity, unit, batch_no)
SELECT a.id, 'RAW-CU-004', '铜排', 220.00, 0.00, 'KG', 'BATCH-K02' FROM wms_asn a WHERE a.asn_no='ASN-20260731-002'
  AND NOT EXISTS (SELECT 1 FROM wms_asn_item i WHERE i.asn_id=a.id AND i.material_code='RAW-CU-004');
INSERT INTO wms_asn_item (asn_id, material_code, material_name, expected_quantity, received_quantity, unit, batch_no)
SELECT a.id, 'RAW-PL-005', '工程塑料粒子', 800.00, 0.00, 'KG', 'BATCH-Q07' FROM wms_asn a WHERE a.asn_no='ASN-20260801-003'
  AND NOT EXISTS (SELECT 1 FROM wms_asn_item i WHERE i.asn_id=a.id AND i.material_code='RAW-PL-005');
INSERT INTO wms_asn_item (asn_id, material_code, material_name, expected_quantity, received_quantity, unit, batch_no)
SELECT a.id, 'P-GEAR-002', '齿轮组件', 200.00, 120.00, '件', 'BATCH-G05' FROM wms_asn a WHERE a.asn_no='ASN-20260801-004'
  AND NOT EXISTS (SELECT 1 FROM wms_asn_item i WHERE i.asn_id=a.id AND i.material_code='P-GEAR-002');
INSERT INTO wms_asn_item (asn_id, material_code, material_name, expected_quantity, received_quantity, unit, batch_no)
SELECT a.id, 'MAT-PKG-001', '包装箱', 3000.00, 0.00, '个', 'BATCH-P09' FROM wms_asn a WHERE a.asn_no='ASN-20260728-005'
  AND NOT EXISTS (SELECT 1 FROM wms_asn_item i WHERE i.asn_id=a.id AND i.material_code='MAT-PKG-001');

-- ---------- 10. 出库单 ----------
INSERT INTO wms_outbound_order (order_no, customer_name, address, type, status, source_no, wave_id, created_time, updated_time)
SELECT 'SO-20260729-001', '上海机电设备有限公司', '上海市闵行区虹梅路500号', 'SALES', 'SHIPPED', 'ERP-SO-1001', NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -3 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_outbound_order WHERE order_no='SO-20260729-001');
INSERT INTO wms_outbound_order (order_no, customer_name, address, type, status, source_no, wave_id, created_time, updated_time)
SELECT 'SO-20260730-002', '北京自动化科技公司', '北京市海淀区中关村大街1号', 'SALES', 'SHIPPED', 'ERP-SO-1002', NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_outbound_order WHERE order_no='SO-20260730-002');
INSERT INTO wms_outbound_order (order_no, customer_name, address, type, status, source_no, wave_id, created_time, updated_time)
SELECT 'SO-20260731-003', '深圳智能制造公司', '深圳市南山区科技园南路10号', 'SALES', 'APPROVED', 'ERP-SO-1003', NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_outbound_order WHERE order_no='SO-20260731-003');
INSERT INTO wms_outbound_order (order_no, customer_name, address, type, status, source_no, wave_id, created_time, updated_time)
SELECT 'SO-20260801-004', '杭州精密仪器公司', '杭州市滨江区江陵路88号', 'SALES', 'CREATED', 'ERP-SO-1004', NULL,
       DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_outbound_order WHERE order_no='SO-20260801-004');
INSERT INTO wms_outbound_order (order_no, customer_name, address, type, status, source_no, wave_id, created_time, updated_time)
SELECT 'SO-20260728-005', '广州贸易公司', '广州市天河区体育西路20号', 'SALES', 'CANCELLED', 'ERP-SO-1005', NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -3 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_outbound_order WHERE order_no='SO-20260728-005');
INSERT INTO wms_outbound_order (order_no, customer_name, address, type, status, source_no, wave_id, created_time, updated_time)
SELECT 'MAT-20260731-006', '生产车间一', '一号厂房', 'MATERIAL', 'APPROVED', 'MES-WO-201', NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_outbound_order WHERE order_no='MAT-20260731-006');

-- ---------- 11. 出库单明细 ----------
INSERT INTO wms_outbound_order_item (outbound_order_id, material_code, material_name, quantity, unit, batch_no, location_code)
SELECT o.id, 'P-PROD-001', '精密减速机', 10.00, '台', 'BATCH-E2E', 'Z003-0001' FROM wms_outbound_order o WHERE o.order_no='SO-20260729-001'
  AND NOT EXISTS (SELECT 1 FROM wms_outbound_order_item i WHERE i.outbound_order_id=o.id AND i.material_code='P-PROD-001');
INSERT INTO wms_outbound_order_item (outbound_order_id, material_code, material_name, quantity, unit, batch_no, location_code)
SELECT o.id, 'P-GEAR-002', '齿轮组件', 50.00, '件', 'BATCH-G05', 'Z001-0005' FROM wms_outbound_order o WHERE o.order_no='SO-20260729-001'
  AND NOT EXISTS (SELECT 1 FROM wms_outbound_order_item i WHERE i.outbound_order_id=o.id AND i.material_code='P-GEAR-002');
INSERT INTO wms_outbound_order_item (outbound_order_id, material_code, material_name, quantity, unit, batch_no, location_code)
SELECT o.id, 'P-PROD-002', '智能控制器', 2.00, '台', 'BATCH-C11', 'Z002-0001' FROM wms_outbound_order o WHERE o.order_no='SO-20260730-002'
  AND NOT EXISTS (SELECT 1 FROM wms_outbound_order_item i WHERE i.outbound_order_id=o.id AND i.material_code='P-PROD-002');
INSERT INTO wms_outbound_order_item (outbound_order_id, material_code, material_name, quantity, unit, batch_no, location_code)
SELECT o.id, 'MAT001', '示例物料', 20.00, 'PCS', 'BATCH-001', 'Z001-0001' FROM wms_outbound_order o WHERE o.order_no='SO-20260731-003'
  AND NOT EXISTS (SELECT 1 FROM wms_outbound_order_item i WHERE i.outbound_order_id=o.id AND i.material_code='MAT001');
INSERT INTO wms_outbound_order_item (outbound_order_id, material_code, material_name, quantity, unit, batch_no, location_code)
SELECT o.id, 'P-PROD-001', '精密减速机', 5.00, '台', 'BATCH-E2E', 'Z003-0001' FROM wms_outbound_order o WHERE o.order_no='SO-20260801-004'
  AND NOT EXISTS (SELECT 1 FROM wms_outbound_order_item i WHERE i.outbound_order_id=o.id AND i.material_code='P-PROD-001');
INSERT INTO wms_outbound_order_item (outbound_order_id, material_code, material_name, quantity, unit, batch_no, location_code)
SELECT o.id, 'RAW-AL-001', '铝合金型材', 100.00, 'KG', 'BATCH-202607', 'Z001-0002' FROM wms_outbound_order o WHERE o.order_no='MAT-20260731-006'
  AND NOT EXISTS (SELECT 1 FROM wms_outbound_order_item i WHERE i.outbound_order_id=o.id AND i.material_code='RAW-AL-001');
INSERT INTO wms_outbound_order_item (outbound_order_id, material_code, material_name, quantity, unit, batch_no, location_code)
SELECT o.id, 'RAW-STL-002', '不锈钢板', 10.00, '张', 'BATCH-202607', 'Z001-0003' FROM wms_outbound_order o WHERE o.order_no='MAT-20260731-006'
  AND NOT EXISTS (SELECT 1 FROM wms_outbound_order_item i WHERE i.outbound_order_id=o.id AND i.material_code='RAW-STL-002');

-- ---------- 12. 波次 ----------
INSERT INTO wms_wave (wave_no, order_type, status, order_count, total_quantity, assigned_to, remark, created_by, created_time, released_time, assigned_time, completed_time, updated_time)
SELECT 'WAVE-20260730-001', 'SALES', 'COMPLETED', 2, 62.0000, '张三', '上午波次', 'admin',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY),
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY),
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_wave WHERE wave_no='WAVE-20260730-001');
INSERT INTO wms_wave (wave_no, order_type, status, order_count, total_quantity, assigned_to, remark, created_by, created_time, released_time, assigned_time, completed_time, updated_time)
SELECT 'WAVE-20260731-002', 'SALES', 'RELEASED', 1, 20.0000, NULL, '下午波次', 'admin',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY),
       NULL, NULL, DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_wave WHERE wave_no='WAVE-20260731-002');
INSERT INTO wms_wave (wave_no, order_type, status, order_count, total_quantity, assigned_to, remark, created_by, created_time, released_time, assigned_time, completed_time, updated_time)
SELECT 'WAVE-20260801-003', 'MATERIAL', 'CREATED', 1, 110.0000, NULL, '生产领料波次', 'admin',
       DATE_ADD(NOW(), INTERVAL 8 HOUR), NULL, NULL, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_wave WHERE wave_no='WAVE-20260801-003');

-- ---------- 13. 拣货任务 ----------
INSERT INTO wms_picking_task (task_no, wave_id, wave_no, outbound_order_id, order_no, status, operator_id, operator_name, created_time, assigned_time, start_time, end_time, updated_time)
SELECT 'PICK-20260730-001', w.id, 'WAVE-20260730-001', o.id, 'SO-20260729-001', 'done', 'OP01', '张三',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY),
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY),
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
FROM wms_wave w, wms_outbound_order o WHERE w.wave_no='WAVE-20260730-001' AND o.order_no='SO-20260729-001'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task WHERE task_no='PICK-20260730-001');

INSERT INTO wms_picking_task (task_no, wave_id, wave_no, outbound_order_id, order_no, status, operator_id, operator_name, created_time, assigned_time, start_time, end_time, updated_time)
SELECT 'PICK-20260730-002', w.id, 'WAVE-20260730-001', o.id, 'SO-20260730-002', 'done', 'OP02', '李四',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY),
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY),
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
FROM wms_wave w, wms_outbound_order o WHERE w.wave_no='WAVE-20260730-001' AND o.order_no='SO-20260730-002'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task WHERE task_no='PICK-20260730-002');

INSERT INTO wms_picking_task (task_no, wave_id, wave_no, outbound_order_id, order_no, status, operator_id, operator_name, created_time, assigned_time, start_time, end_time, updated_time)
SELECT 'PICK-20260731-003', w.id, 'WAVE-20260731-002', o.id, 'SO-20260731-003', 'working', 'OP01', '张三',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY),
       DATE_ADD(NOW(), INTERVAL 8 HOUR), NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_wave w, wms_outbound_order o WHERE w.wave_no='WAVE-20260731-002' AND o.order_no='SO-20260731-003'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task WHERE task_no='PICK-20260731-003');

INSERT INTO wms_picking_task (task_no, wave_id, wave_no, outbound_order_id, order_no, status, operator_id, operator_name, created_time, assigned_time, start_time, end_time, updated_time)
SELECT 'PICK-20260801-004', w.id, 'WAVE-20260801-003', o.id, 'MAT-20260731-006', 'pending', NULL, NULL,
       DATE_ADD(NOW(), INTERVAL 8 HOUR), NULL, NULL, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_wave w, wms_outbound_order o WHERE w.wave_no='WAVE-20260801-003' AND o.order_no='MAT-20260731-006'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task WHERE task_no='PICK-20260801-004');

INSERT INTO wms_picking_task (task_no, wave_id, wave_no, outbound_order_id, order_no, status, operator_id, operator_name, created_time, assigned_time, start_time, end_time, updated_time)
SELECT 'PICK-20260801-005', NULL, NULL, o.id, 'SO-20260801-004', 'assigned', 'OP03', '王五',
       DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR), NULL, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_outbound_order o WHERE o.order_no='SO-20260801-004'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task WHERE task_no='PICK-20260801-005');

-- ---------- 14. 拣货明细 ----------
INSERT INTO wms_picking_task_item (task_id, outbound_order_item_id, material_code, material_name, location_code, batch_no, quantity, unit, status, pick_time, created_time, updated_time)
SELECT t.id, i.id, 'P-PROD-001', '精密减速机', 'Z003-0001', 'BATCH-E2E', 10.00, '台', 'done',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
FROM wms_picking_task t, wms_outbound_order_item i, wms_outbound_order o
WHERE t.task_no='PICK-20260730-001' AND i.outbound_order_id=o.id AND o.order_no='SO-20260729-001' AND i.material_code='P-PROD-001'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task_item x WHERE x.task_id=t.id AND x.material_code='P-PROD-001');
INSERT INTO wms_picking_task_item (task_id, outbound_order_item_id, material_code, material_name, location_code, batch_no, quantity, unit, status, pick_time, created_time, updated_time)
SELECT t.id, i.id, 'P-GEAR-002', '齿轮组件', 'Z001-0005', 'BATCH-G05', 50.00, '件', 'done',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
FROM wms_picking_task t, wms_outbound_order_item i, wms_outbound_order o
WHERE t.task_no='PICK-20260730-001' AND i.outbound_order_id=o.id AND o.order_no='SO-20260729-001' AND i.material_code='P-GEAR-002'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task_item x WHERE x.task_id=t.id AND x.material_code='P-GEAR-002');
INSERT INTO wms_picking_task_item (task_id, outbound_order_item_id, material_code, material_name, location_code, batch_no, quantity, unit, status, pick_time, created_time, updated_time)
SELECT t.id, i.id, 'P-PROD-002', '智能控制器', 'Z002-0001', 'BATCH-C11', 2.00, '台', 'done',
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
FROM wms_picking_task t, wms_outbound_order_item i, wms_outbound_order o
WHERE t.task_no='PICK-20260730-002' AND i.outbound_order_id=o.id AND o.order_no='SO-20260730-002' AND i.material_code='P-PROD-002'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task_item x WHERE x.task_id=t.id AND x.material_code='P-PROD-002');
INSERT INTO wms_picking_task_item (task_id, outbound_order_item_id, material_code, material_name, location_code, batch_no, quantity, unit, status, pick_time, created_time, updated_time)
SELECT t.id, i.id, 'MAT001', '示例物料', 'Z001-0001', 'BATCH-001', 20.00, 'PCS', 'pending', NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY)
FROM wms_picking_task t, wms_outbound_order_item i, wms_outbound_order o
WHERE t.task_no='PICK-20260731-003' AND i.outbound_order_id=o.id AND o.order_no='SO-20260731-003' AND i.material_code='MAT001'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task_item x WHERE x.task_id=t.id AND x.material_code='MAT001');
INSERT INTO wms_picking_task_item (task_id, outbound_order_item_id, material_code, material_name, location_code, batch_no, quantity, unit, status, pick_time, created_time, updated_time)
SELECT t.id, i.id, 'RAW-AL-001', '铝合金型材', 'Z001-0002', 'BATCH-202607', 100.00, 'KG', 'pending', NULL,
       DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_picking_task t, wms_outbound_order_item i, wms_outbound_order o
WHERE t.task_no='PICK-20260801-004' AND i.outbound_order_id=o.id AND o.order_no='MAT-20260731-006' AND i.material_code='RAW-AL-001'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task_item x WHERE x.task_id=t.id AND x.material_code='RAW-AL-001');
INSERT INTO wms_picking_task_item (task_id, outbound_order_item_id, material_code, material_name, location_code, batch_no, quantity, unit, status, pick_time, created_time, updated_time)
SELECT t.id, i.id, 'RAW-STL-002', '不锈钢板', 'Z001-0003', 'BATCH-202607', 10.00, '张', 'pending', NULL,
       DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_picking_task t, wms_outbound_order_item i, wms_outbound_order o
WHERE t.task_no='PICK-20260801-004' AND i.outbound_order_id=o.id AND o.order_no='MAT-20260731-006' AND i.material_code='RAW-STL-002'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task_item x WHERE x.task_id=t.id AND x.material_code='RAW-STL-002');
INSERT INTO wms_picking_task_item (task_id, outbound_order_item_id, material_code, material_name, location_code, batch_no, quantity, unit, status, pick_time, created_time, updated_time)
SELECT t.id, i.id, 'P-PROD-001', '精密减速机', 'Z003-0001', 'BATCH-E2E', 5.00, '台', 'pending', NULL,
       DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)
FROM wms_picking_task t, wms_outbound_order_item i, wms_outbound_order o
WHERE t.task_no='PICK-20260801-005' AND i.outbound_order_id=o.id AND o.order_no='SO-20260801-004' AND i.material_code='P-PROD-001'
  AND NOT EXISTS (SELECT 1 FROM wms_picking_task_item x WHERE x.task_id=t.id AND x.material_code='P-PROD-001');

-- ---------- 15. 盘点任务 ----------
INSERT INTO wms_stock_count_job (count_no, warehouse_code, count_type, status, create_user, total_item_count, finished_item_count, diff_count, create_time, start_time, end_time, updated_time)
SELECT 'COUNT-20260728-001', 'WH001', '1', '2', 'admin', 6, 6, 1,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY),
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_stock_count_job WHERE count_no='COUNT-20260728-001');
INSERT INTO wms_stock_count_job (count_no, warehouse_code, count_type, status, create_user, total_item_count, finished_item_count, diff_count, create_time, start_time, end_time, updated_time)
SELECT 'COUNT-20260731-002', 'WH001', '2', '1', 'admin', 3, 1, 0,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY),
       NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_stock_count_job WHERE count_no='COUNT-20260731-002');
INSERT INTO wms_stock_count_job (count_no, warehouse_code, count_type, status, create_user, total_item_count, finished_item_count, diff_count, create_time, start_time, end_time, updated_time)
SELECT 'COUNT-20260801-003', 'WH002', '1', '0', 'admin', 0, 0, 0,
       DATE_ADD(NOW(), INTERVAL 8 HOUR), NULL, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR)
WHERE NOT EXISTS (SELECT 1 FROM wms_stock_count_job WHERE count_no='COUNT-20260801-003');

-- ---------- 16. 盘点明细 ----------
INSERT INTO wms_stock_count_item (job_id, location_code, material_code, material_name, batch_no, unit, sys_qty, count_qty, diff_qty, scan_time, created_time, updated_time)
SELECT j.id, 'Z001-0001', 'MAT001', '示例物料', 'BATCH-001', 'PCS', 150.00, 150.00, 0.00,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY)
FROM wms_stock_count_job j WHERE j.count_no='COUNT-20260728-001'
  AND NOT EXISTS (SELECT 1 FROM wms_stock_count_item x WHERE x.job_id=j.id AND x.material_code='MAT001' AND x.location_code='Z001-0001');
INSERT INTO wms_stock_count_item (job_id, location_code, material_code, material_name, batch_no, unit, sys_qty, count_qty, diff_qty, scan_time, created_time, updated_time)
SELECT j.id, 'Z001-0002', 'RAW-AL-001', '铝合金型材', 'BATCH-202607', 'KG', 500.00, 498.00, -2.00,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY)
FROM wms_stock_count_job j WHERE j.count_no='COUNT-20260728-001'
  AND NOT EXISTS (SELECT 1 FROM wms_stock_count_item x WHERE x.job_id=j.id AND x.material_code='RAW-AL-001' AND x.location_code='Z001-0002');
INSERT INTO wms_stock_count_item (job_id, location_code, material_code, material_name, batch_no, unit, sys_qty, count_qty, diff_qty, scan_time, created_time, updated_time)
SELECT j.id, 'Z001-0003', 'RAW-STL-002', '不锈钢板', 'BATCH-202607', '张', 8.00, 8.00, 0.00,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY)
FROM wms_stock_count_job j WHERE j.count_no='COUNT-20260728-001'
  AND NOT EXISTS (SELECT 1 FROM wms_stock_count_item x WHERE x.job_id=j.id AND x.material_code='RAW-STL-002' AND x.location_code='Z001-0003');
INSERT INTO wms_stock_count_item (job_id, location_code, material_code, material_name, batch_no, unit, sys_qty, count_qty, diff_qty, scan_time, created_time, updated_time)
SELECT j.id, 'Z001-0004', 'SEMI-MOTOR-003', '电机半成品', 'BATCH-A01', '台', 60.00, 60.00, 0.00, NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY)
FROM wms_stock_count_job j WHERE j.count_no='COUNT-20260731-002'
  AND NOT EXISTS (SELECT 1 FROM wms_stock_count_item x WHERE x.job_id=j.id AND x.material_code='SEMI-MOTOR-003' AND x.location_code='Z001-0004');
INSERT INTO wms_stock_count_item (job_id, location_code, material_code, material_name, batch_no, unit, sys_qty, count_qty, diff_qty, scan_time, created_time, updated_time)
SELECT j.id, 'Z001-0005', 'P-GEAR-002', '齿轮组件', 'BATCH-G05', '件', 1200.00, 0.00, -1200.00, NULL,
       DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY)
FROM wms_stock_count_job j WHERE j.count_no='COUNT-20260731-002'
  AND NOT EXISTS (SELECT 1 FROM wms_stock_count_item x WHERE x.job_id=j.id AND x.material_code='P-GEAR-002' AND x.location_code='Z001-0005');

-- ---------- 17. 库存流水 ----------
INSERT INTO wms_inventory_transaction (warehouse_code, location_code, material_code, material_name, batch_no, type, quantity, unit, source_no, operator, transaction_time)
SELECT 'WH001', 'Z001-0002', 'RAW-AL-001', '铝合金型材', 'BATCH-202607', 'IN', 500.00, 'KG', 'ASN-20260730-001', 'admin', DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_inventory_transaction WHERE source_no='ASN-20260730-001' AND material_code='RAW-AL-001' AND type='IN');
INSERT INTO wms_inventory_transaction (warehouse_code, location_code, material_code, material_name, batch_no, type, quantity, unit, source_no, operator, transaction_time)
SELECT 'WH001', 'Z001-0003', 'RAW-STL-002', '不锈钢板', 'BATCH-202607', 'IN', 100.00, '张', 'ASN-20260730-001', 'admin', DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_inventory_transaction WHERE source_no='ASN-20260730-001' AND material_code='RAW-STL-002' AND type='IN');
INSERT INTO wms_inventory_transaction (warehouse_code, location_code, material_code, material_name, batch_no, type, quantity, unit, source_no, operator, transaction_time)
SELECT 'WH002', 'Z003-0001', 'P-PROD-001', '精密减速机', 'BATCH-E2E', 'OUT', 10.00, '台', 'SO-20260729-001', '张三', DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_inventory_transaction WHERE source_no='SO-20260729-001' AND material_code='P-PROD-001' AND type='OUT');
INSERT INTO wms_inventory_transaction (warehouse_code, location_code, material_code, material_name, batch_no, type, quantity, unit, source_no, operator, transaction_time)
SELECT 'WH001', 'Z001-0005', 'P-GEAR-002', '齿轮组件', 'BATCH-G05', 'OUT', 50.00, '件', 'SO-20260729-001', '张三', DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -2 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_inventory_transaction WHERE source_no='SO-20260729-001' AND material_code='P-GEAR-002' AND type='OUT');
INSERT INTO wms_inventory_transaction (warehouse_code, location_code, material_code, material_name, batch_no, type, quantity, unit, source_no, operator, transaction_time)
SELECT 'WH001', 'Z002-0001', 'P-PROD-002', '智能控制器', 'BATCH-C11', 'OUT', 2.00, '台', 'SO-20260730-002', '李四', DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_inventory_transaction WHERE source_no='SO-20260730-002' AND material_code='P-PROD-002' AND type='OUT');
INSERT INTO wms_inventory_transaction (warehouse_code, location_code, material_code, material_name, batch_no, type, quantity, unit, source_no, operator, transaction_time)
SELECT 'WH001', 'Z001-0002', 'RAW-AL-001', '铝合金型材', 'BATCH-202607', 'COUNT', -2.00, 'KG', 'COUNT-20260728-001', 'admin', DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -4 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_inventory_transaction WHERE source_no='COUNT-20260728-001' AND material_code='RAW-AL-001' AND type='COUNT');
INSERT INTO wms_inventory_transaction (warehouse_code, location_code, material_code, material_name, batch_no, type, quantity, unit, source_no, operator, transaction_time)
SELECT 'WH001', 'Z001-0004', 'SEMI-MOTOR-003', '电机半成品', 'BATCH-A01', 'ADJUST', 5.00, '台', 'ADJ-20260731-001', 'admin', DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL -1 DAY)
WHERE NOT EXISTS (SELECT 1 FROM wms_inventory_transaction WHERE source_no='ADJ-20260731-001' AND material_code='SEMI-MOTOR-003' AND type='ADJUST');
