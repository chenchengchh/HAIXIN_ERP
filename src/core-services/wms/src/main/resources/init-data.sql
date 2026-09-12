INSERT INTO wms_warehouse (warehouse_code, warehouse_name, address, manager, contact, status, created_time, updated_time)
SELECT 'WH001', '一号仓', '默认地址', 'admin', '00000000000', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_warehouse WHERE warehouse_code = 'WH001');

INSERT INTO wms_zone (warehouse_code, zone_code, zone_name, zone_type, status, created_time, updated_time)
SELECT 'WH001', 'Z001', '默认库区', 'storage', '1', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_zone WHERE zone_code = 'Z001');

INSERT INTO wms_location_type (type_code, type_name, type_desc, max_weight, mix_flag, status, created_time, updated_time)
SELECT 'LT01', '标准库位', '默认库位类型', 1000.0, 1, '1', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_location_type WHERE type_code = 'LT01');

INSERT INTO wms_material_type (type_code, type_name, description, status, created_time, updated_time)
SELECT 'MT01', '默认物料类型', '默认物料类型', '1', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_material_type WHERE type_code = 'MT01');

INSERT INTO wms_barcode_rule (rule_code, rule_name, rule_type, rule_format, description, status, created_time, updated_time)
SELECT 'BR01', '默认条码规则', 'material', 'MAT{yyyy}{MM}{dd}{####}', '默认条码规则', '1', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_barcode_rule WHERE rule_code = 'BR01');

INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH001', 'Z001', 'Z001-0001', '库位0001', 'LT01', '1', '', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z001-0001');

INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH001', 'Z001', 'Z001-0002', '库位0002', 'LT01', '1', '', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z001-0002');

INSERT INTO wms_location (warehouse_code, zone_code, location_code, location_name, location_type_code, status, remark, created_time, updated_time)
SELECT 'WH001', 'Z001', 'Z001-0003', '库位0003', 'LT01', '1', '', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_location WHERE location_code = 'Z001-0003');

INSERT INTO wms_inventory (warehouse_id, warehouse_code, location_code, material_code, material_name, quantity, unit, batch_no, created_time, updated_time)
SELECT w.id, 'WH001', 'Z001-0001', 'MAT001', '示例物料', 10.0, 'PCS', 'BATCH-001', NOW(), NOW()
FROM wms_warehouse w
WHERE w.warehouse_code = 'WH001'
  AND NOT EXISTS (
    SELECT 1 FROM wms_inventory i
    WHERE i.warehouse_code = 'WH001' AND i.location_code = 'Z001-0001' AND i.material_code = 'MAT001' AND i.batch_no = 'BATCH-001'
  );

INSERT INTO wms_outbound_order (order_no, customer_name, address, type, status, source_no, created_time, updated_time, wave_id)
SELECT 'SO-DEMO-001', '示例客户', '默认地址', 'SALES', 'APPROVED', 'SRC-DEMO-001', NOW(), NOW(), NULL
WHERE NOT EXISTS (SELECT 1 FROM wms_outbound_order WHERE order_no = 'SO-DEMO-001');

INSERT INTO wms_outbound_order_item (outbound_order_id, material_code, material_name, quantity, unit, batch_no, location_code)
SELECT o.id, 'MAT001', '示例物料', 2.0, 'PCS', 'BATCH-001', 'Z001-0001'
FROM wms_outbound_order o
WHERE o.order_no = 'SO-DEMO-001'
  AND NOT EXISTS (
    SELECT 1 FROM wms_outbound_order_item i
    WHERE i.outbound_order_id = o.id AND i.material_code = 'MAT001' AND i.location_code = 'Z001-0001' AND i.batch_no = 'BATCH-001'
  );

