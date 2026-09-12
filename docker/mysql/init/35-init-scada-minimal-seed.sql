-- ========================================
-- SCADA 最小可用种子数据
-- 说明：用于分域改造验收所需的最小业务数据
-- ========================================
USE scada_db;

INSERT INTO scada_device (
  id, device_code, device_name, device_type, device_category, device_status,
  location, ip_address, port, protocol, collect_interval, last_online_time,
  remark, created_by, created_time, updated_by, updated_time
) VALUES
  (1, 'DEV-PLC-001', 'PLC-001', 'PLC', 'production', 'online', 'workshop-1', '192.168.1.101', '502', 'modbus', '5s', NOW(), '', 'system', NOW(), 'system', NOW()),
  (2, 'DEV-SENSOR-001', 'SENSOR-001', 'SENSOR', 'warehouse', 'online', 'warehouse', '192.168.1.103', '1883', 'mqtt', '10s', NOW(), '', 'system', NOW(), 'system', NOW())
ON DUPLICATE KEY UPDATE
  device_name = VALUES(device_name),
  device_type = VALUES(device_type),
  device_category = VALUES(device_category),
  device_status = VALUES(device_status),
  location = VALUES(location),
  ip_address = VALUES(ip_address),
  port = VALUES(port),
  protocol = VALUES(protocol),
  collect_interval = VALUES(collect_interval),
  last_online_time = VALUES(last_online_time),
  updated_by = VALUES(updated_by),
  updated_time = VALUES(updated_time);

INSERT INTO scada_collect_point (
  id, tag_code, device_name, protocol, address, data_type, unit,
  latest_value, status, enabled, remark, created_time, updated_time
) VALUES
  (1, 'TEMP_001', 'PLC-001', 'modbus', '40001', 'float', 'C', 22.5, 'normal', 1, '', NOW(), NOW()),
  (2, 'PRESS_001', 'PLC-001', 'modbus', '40011', 'float', 'bar', 1.6, 'normal', 1, '', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  device_name = VALUES(device_name),
  protocol = VALUES(protocol),
  address = VALUES(address),
  data_type = VALUES(data_type),
  unit = VALUES(unit),
  latest_value = VALUES(latest_value),
  status = VALUES(status),
  enabled = VALUES(enabled),
  updated_time = VALUES(updated_time);

INSERT INTO scada_tag_value (id, tag_code, ts, value, unit, quality)
VALUES
  (1, 'TEMP_001', NOW(3) - INTERVAL 10 MINUTE, 22.5, 'C', 'good'),
  (2, 'PRESS_001', NOW(3) - INTERVAL 5 MINUTE, 1.6, 'bar', 'good'),
  (3, 'TEMP_001', NOW(3), 23.1, 'C', 'good')
ON DUPLICATE KEY UPDATE
  ts = VALUES(ts),
  value = VALUES(value),
  unit = VALUES(unit),
  quality = VALUES(quality);
