-- ========================================
-- LES 最小可用种子数据
-- 说明：用于分域改造验收所需的最小业务数据
-- ========================================
USE les_db;

INSERT INTO les_vehicle (id, license_plate, vehicle_type, load_capacity, status, create_time)
VALUES
  (1, '粤A12345', 'van', 5.00, 'available', NOW()),
  (2, '粤A67890', 'cold-chain', 3.00, 'active', NOW())
ON DUPLICATE KEY UPDATE
  vehicle_type = VALUES(vehicle_type),
  load_capacity = VALUES(load_capacity),
  status = VALUES(status),
  create_time = VALUES(create_time);

INSERT INTO les_driver (id, name, phone, license_no, status, create_time)
VALUES
  (1, 'driver1', '13800138001', 'A123456789', 'available', NOW()),
  (2, 'driver2', '13900139001', 'A987654321', 'active', NOW())
ON DUPLICATE KEY UPDATE
  phone = VALUES(phone),
  license_no = VALUES(license_no),
  status = VALUES(status),
  create_time = VALUES(create_time);

INSERT INTO les_route (id, route_name, start_location, end_location, distance, estimated_time, create_time)
VALUES
  (1, 'GZ-SZ', 'Guangzhou', 'Shenzhen', 130.00, 120, NOW()),
  (2, 'GZ-ZH', 'Guangzhou', 'Zhuhai', 150.00, 150, NOW())
ON DUPLICATE KEY UPDATE
  start_location = VALUES(start_location),
  end_location = VALUES(end_location),
  distance = VALUES(distance),
  estimated_time = VALUES(estimated_time),
  create_time = VALUES(create_time);

INSERT INTO les_transport_plan (id, plan_no, sales_order_no, source_type, source_no, vehicle_id, driver_id, route_id, status, cost_estimated, create_time, update_time)
VALUES
  (1, 'PLAN-001', 'SO-001', 'LOCAL', 'SO-001', 1, 1, 1, 2, 500.00, NOW(), NOW()),
  (2, 'PLAN-002', 'SO-002', 'LOCAL', 'SO-002', 2, 2, 2, 1, 800.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  sales_order_no = VALUES(sales_order_no),
  source_type = VALUES(source_type),
  source_no = VALUES(source_no),
  vehicle_id = VALUES(vehicle_id),
  driver_id = VALUES(driver_id),
  route_id = VALUES(route_id),
  status = VALUES(status),
  cost_estimated = VALUES(cost_estimated),
  update_time = VALUES(update_time);

INSERT INTO les_monitor_log (id, plan_id, vehicle_id, longitude, latitude, current_status, is_anomaly, record_time)
VALUES
  (1, 1, 1, 113.32452000, 23.10229000, 'running', 0, NOW() - INTERVAL 30 MINUTE),
  (2, 1, 1, 113.34452000, 23.12229000, 'unloading', 0, NOW())
ON DUPLICATE KEY UPDATE
  current_status = VALUES(current_status),
  is_anomaly = VALUES(is_anomaly),
  record_time = VALUES(record_time);
