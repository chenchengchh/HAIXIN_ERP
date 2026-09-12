-- SCADA模块初始化表结构与基础数据

SET time_zone = '+8:00';
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS scada_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scada_db;

CREATE TABLE IF NOT EXISTS scada_device (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  device_code VARCHAR(50) NOT NULL UNIQUE,
  device_name VARCHAR(100) NOT NULL,
  device_type VARCHAR(20),
  device_category VARCHAR(50),
  device_status VARCHAR(20),
  location VARCHAR(100),
  ip_address VARCHAR(50),
  port VARCHAR(10),
  protocol VARCHAR(20),
  collect_interval VARCHAR(20),
  last_online_time DATETIME,
  remark VARCHAR(500),
  created_by VARCHAR(50),
  created_time DATETIME,
  updated_by VARCHAR(50),
  updated_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS scada_collect_point (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tag_code VARCHAR(50) NOT NULL UNIQUE,
  device_name VARCHAR(100),
  protocol VARCHAR(20),
  address VARCHAR(100),
  data_type VARCHAR(30),
  unit VARCHAR(20),
  latest_value DOUBLE,
  status VARCHAR(20),
  enabled TINYINT(1),
  remark VARCHAR(500),
  created_time DATETIME,
  updated_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS scada_protocol (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  type VARCHAR(30) NOT NULL,
  device_count INT,
  status VARCHAR(20),
  enabled TINYINT(1),
  config_json TEXT,
  created_time DATETIME,
  updated_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS scada_preprocess_setting (
  id BIGINT PRIMARY KEY,
  range_conversion TINYINT(1),
  deadband_filter TINYINT(1),
  deadband_value DOUBLE,
  data_validation TINYINT(1),
  updated_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS scada_storage_policy (
  id BIGINT PRIMARY KEY,
  storage_type VARCHAR(30),
  host VARCHAR(100),
  port INT,
  database_name VARCHAR(100),
  username VARCHAR(100),
  password VARCHAR(200),
  connection_status VARCHAR(30),
  realtime_retention INT,
  historical_sampling VARCHAR(30),
  compression_level VARCHAR(30),
  backup_policy VARCHAR(30),
  stored_data INT,
  writes_per_second INT,
  storage_efficiency INT,
  last_write_time DATETIME,
  enabled TINYINT(1),
  updated_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS scada_alarm_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tag_code VARCHAR(50) NOT NULL,
  alarm_name VARCHAR(100),
  severity INT,
  enabled TINYINT(1),
  high_high DOUBLE,
  high DOUBLE,
  low DOUBLE,
  low_low DOUBLE,
  deadband DOUBLE,
  remark VARCHAR(500),
  created_time DATETIME,
  updated_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS scada_alarm_active (
  id VARCHAR(64) PRIMARY KEY,
  tag_code VARCHAR(50),
  alarm_name VARCHAR(100),
  alarm_type VARCHAR(50),
  severity INT,
  status VARCHAR(20),
  trigger_time DATETIME,
  current_value DOUBLE,
  threshold_value DOUBLE,
  device_name VARCHAR(100),
  description VARCHAR(500),
  muted TINYINT(1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS scada_alarm_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tag_code VARCHAR(50),
  alarm_type VARCHAR(50),
  severity INT,
  trigger_time DATETIME,
  confirm_time DATETIME,
  recovery_time DATETIME,
  handler_id BIGINT,
  memo TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS scada_alarm_trigger_config (
  id BIGINT PRIMARY KEY,
  config_json TEXT,
  updated_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS scada_tag_value (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tag_code VARCHAR(50),
  ts DATETIME(3),
  value DOUBLE,
  unit VARCHAR(20),
  quality VARCHAR(20),
  KEY idx_scada_tag_ts (tag_code, ts)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS scada_report_def (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_name VARCHAR(100),
  template_path VARCHAR(255),
  cron_expression VARCHAR(50),
  export_format VARCHAR(20),
  associated_tags TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO scada_protocol (name, type, device_count, status, enabled, config_json, created_time, updated_time)
SELECT 'Modbus TCP', 'modbus', 3, 'active', 1, '{\"host\":\"192.168.1.10\",\"port\":502,\"timeout\":3000,\"retryCount\":3}', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_protocol WHERE name = 'Modbus TCP');

INSERT INTO scada_protocol (name, type, device_count, status, enabled, config_json, created_time, updated_time)
SELECT 'OPC UA', 'opcua', 2, 'active', 1, '{\"endpoint\":\"opc.tcp://192.168.1.20:4840\",\"securityMode\":\"None\"}', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_protocol WHERE name = 'OPC UA');

INSERT INTO scada_protocol (name, type, device_count, status, enabled, config_json, created_time, updated_time)
SELECT 'MQTT', 'mqtt', 5, 'active', 1, '{\"broker\":\"mqtt://192.168.1.30:1883\",\"topic\":\"scada/#\"}', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_protocol WHERE name = 'MQTT');

INSERT INTO scada_device (device_code, device_name, device_type, device_category, device_status, location, ip_address, port, protocol, collect_interval, last_online_time, remark, created_by, created_time, updated_by, updated_time)
SELECT 'DEV-PLC-001', '生产车间PLC-001', 'PLC', '生产', '在线', '生产车间', '192.168.1.101', '502', 'modbus', '5s', NOW(), '', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_device WHERE device_code = 'DEV-PLC-001');

INSERT INTO scada_device (device_code, device_name, device_type, device_category, device_status, location, ip_address, port, protocol, collect_interval, last_online_time, remark, created_by, created_time, updated_by, updated_time)
SELECT 'DEV-PLC-002', '包装车间PLC-002', 'PLC', '生产', '在线', '包装车间', '192.168.1.102', '502', 'modbus', '5s', NOW(), '', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_device WHERE device_code = 'DEV-PLC-002');

INSERT INTO scada_device (device_code, device_name, device_type, device_category, device_status, location, ip_address, port, protocol, collect_interval, last_online_time, remark, created_by, created_time, updated_by, updated_time)
SELECT 'DEV-SENSOR-001', '仓库环境传感器-001', 'SENSOR', '仓储', '离线', '仓库', '192.168.1.103', '1883', 'mqtt', '10s', DATE_SUB(NOW(), INTERVAL 2 HOUR), '', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_device WHERE device_code = 'DEV-SENSOR-001');

INSERT INTO scada_device (device_code, device_name, device_type, device_category, device_status, location, ip_address, port, protocol, collect_interval, last_online_time, remark, created_by, created_time, updated_by, updated_time)
SELECT 'DEV-OPC-001', '实验室OPC采集站-001', 'GATEWAY', '实验', '在线', '实验室', '192.168.1.104', '4840', 'opcua', '10s', NOW(), '', 'system', NOW(), 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_device WHERE device_code = 'DEV-OPC-001');

INSERT INTO scada_collect_point (tag_code, device_name, protocol, address, data_type, unit, latest_value, status, enabled, remark, created_time, updated_time)
SELECT 'TEMP_001', '生产车间PLC-001', 'modbus', '40001', 'float', '℃', 22.5, 'normal', 1, '', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_collect_point WHERE tag_code = 'TEMP_001');

INSERT INTO scada_collect_point (tag_code, device_name, protocol, address, data_type, unit, latest_value, status, enabled, remark, created_time, updated_time)
SELECT 'PRESS_001', '生产车间PLC-001', 'modbus', '40011', 'float', 'bar', 1.6, 'normal', 1, '', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_collect_point WHERE tag_code = 'PRESS_001');

INSERT INTO scada_collect_point (tag_code, device_name, protocol, address, data_type, unit, latest_value, status, enabled, remark, created_time, updated_time)
SELECT 'FLOW_001', '包装车间PLC-002', 'modbus', '40021', 'float', 'm³/h', 12.3, 'normal', 1, '', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_collect_point WHERE tag_code = 'FLOW_001');

INSERT INTO scada_alarm_rule (tag_code, alarm_name, severity, enabled, high_high, high, low, low_low, deadband, remark, created_time, updated_time)
SELECT 'TEMP_001', '温度超限', 2, 1, 35.0, 30.0, NULL, NULL, 0.5, '', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_alarm_rule WHERE tag_code = 'TEMP_001');

INSERT INTO scada_alarm_rule (tag_code, alarm_name, severity, enabled, high_high, high, low, low_low, deadband, remark, created_time, updated_time)
SELECT 'PRESS_001', '压力超限', 2, 1, 2.5, 2.0, 1.0, 0.8, 0.1, '', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM scada_alarm_rule WHERE tag_code = 'PRESS_001');

INSERT INTO scada_alarm_trigger_config (id, config_json, updated_time)
VALUES (1, '{\"triggerCondition\":\"value-exceed\",\"tagCode\":\"TEMP_001\",\"triggerValue\":30,\"duration\":5,\"severity\":2,\"triggerMethods\":[\"screen\",\"sound\"]}', NOW())
ON DUPLICATE KEY UPDATE
  config_json = VALUES(config_json),
  updated_time = VALUES(updated_time);

DELIMITER //
DROP PROCEDURE IF EXISTS seed_scada_tag_values//
CREATE PROCEDURE seed_scada_tag_values()
BEGIN
  DECLARE i INT DEFAULT 0;
  IF (SELECT COUNT(*) FROM scada_tag_value) = 0 THEN
    WHILE i < 288 DO
      INSERT INTO scada_tag_value(tag_code, ts, value, unit, quality)
      VALUES ('TEMP_001', DATE_SUB(NOW(3), INTERVAL (i * 5) MINUTE), 20 + RAND() * 8, '℃', 'good');
      INSERT INTO scada_tag_value(tag_code, ts, value, unit, quality)
      VALUES ('PRESS_001', DATE_SUB(NOW(3), INTERVAL (i * 5) MINUTE), 1.0 + RAND() * 1.5, 'bar', 'good');
      INSERT INTO scada_tag_value(tag_code, ts, value, unit, quality)
      VALUES ('FLOW_001', DATE_SUB(NOW(3), INTERVAL (i * 5) MINUTE), 8 + RAND() * 10, 'm³/h', 'good');
      SET i = i + 1;
    END WHILE;
  END IF;
END//
DELIMITER ;
CALL seed_scada_tag_values();
DROP PROCEDURE seed_scada_tag_values;

INSERT INTO scada_alarm_active (id, tag_code, alarm_name, alarm_type, severity, status, trigger_time, current_value, threshold_value, device_name, description, muted)
SELECT REPLACE(UUID(), '-', ''), 'TEMP_001', '温度超限', 'high', 2, '未确认', NOW(), 33.2, 30.0, '生产车间PLC-001', '温度超过阈值', 0
WHERE NOT EXISTS (SELECT 1 FROM scada_alarm_active LIMIT 1);

INSERT INTO scada_alarm_history (tag_code, alarm_type, severity, trigger_time, confirm_time, recovery_time, handler_id, memo)
SELECT 'TEMP_001', 'high', 2, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 110 MINUTE), DATE_SUB(NOW(), INTERVAL 100 MINUTE), NULL, '自动生成历史报警'
WHERE NOT EXISTS (SELECT 1 FROM scada_alarm_history LIMIT 1);

INSERT INTO scada_preprocess_setting (id, range_conversion, deadband_filter, deadband_value, data_validation, updated_time)
VALUES (1, 1, 1, 0.5, 1, NOW())
ON DUPLICATE KEY UPDATE
  range_conversion = VALUES(range_conversion),
  deadband_filter = VALUES(deadband_filter),
  deadband_value = VALUES(deadband_value),
  data_validation = VALUES(data_validation),
  updated_time = VALUES(updated_time);

INSERT INTO scada_storage_policy (id, storage_type, host, port, database_name, username, password, connection_status, realtime_retention, historical_sampling, compression_level, backup_policy, stored_data, writes_per_second, storage_efficiency, last_write_time, enabled, updated_time)
VALUES (1, 'mysql', 'mysql', 3306, 'scada_db', 'root', 'root', 'connected', 7, '5m', 'medium', 'daily', 0, 0, 0, NOW(), 1, NOW())
ON DUPLICATE KEY UPDATE
  storage_type = VALUES(storage_type),
  host = VALUES(host),
  port = VALUES(port),
  database_name = VALUES(database_name),
  username = VALUES(username),
  password = VALUES(password),
  connection_status = VALUES(connection_status),
  realtime_retention = VALUES(realtime_retention),
  historical_sampling = VALUES(historical_sampling),
  compression_level = VALUES(compression_level),
  backup_policy = VALUES(backup_policy),
  stored_data = VALUES(stored_data),
  writes_per_second = VALUES(writes_per_second),
  storage_efficiency = VALUES(storage_efficiency),
  last_write_time = VALUES(last_write_time),
  enabled = VALUES(enabled),
  updated_time = VALUES(updated_time);

SET FOREIGN_KEY_CHECKS = 1;
