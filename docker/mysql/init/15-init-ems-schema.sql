SET time_zone = '+8:00';
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS ems_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ems_db;

CREATE TABLE IF NOT EXISTS ems_meter_device (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  type VARCHAR(50) NOT NULL,
  ip_address VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL,
  last_update DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ems_real_time_data (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  energy_type VARCHAR(50) NOT NULL,
  area VARCHAR(100) NOT NULL,
  actual_value DOUBLE NOT NULL,
  unit VARCHAR(20),
  collection_time DATETIME NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_ems_rtd_time (collection_time),
  KEY idx_ems_rtd_type_area (energy_type, area)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ems_calibration_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  meter_id BIGINT NOT NULL,
  meter_name VARCHAR(100) NOT NULL,
  raw_value DOUBLE NOT NULL,
  calibrated_value DOUBLE NOT NULL,
  coefficient DOUBLE NOT NULL,
  reason VARCHAR(500),
  calibration_time DATETIME NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_ems_calibration_meter (meter_id),
  KEY idx_ems_calibration_time (calibration_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ems_energy_anomaly (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  energy_type VARCHAR(50) NOT NULL,
  area VARCHAR(100) NOT NULL,
  anomaly_type VARCHAR(100) NOT NULL,
  actual_value DOUBLE NOT NULL,
  expected_value DOUBLE NOT NULL,
  detection_time DATETIME NOT NULL,
  status VARCHAR(20) NOT NULL,
  processed_time DATETIME,
  processed_by VARCHAR(50),
  processed_remark VARCHAR(500),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_ems_anomaly_time (detection_time),
  KEY idx_ems_anomaly_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ems_optimization_suggestion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  target_area VARCHAR(100) NOT NULL,
  estimated_effect VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ems_optimization_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plan_name VARCHAR(255),
  target_area VARCHAR(255),
  predicted_saving DOUBLE,
  actual_saving DOUBLE,
  status INT,
  exec_content VARCHAR(255),
  report_url VARCHAR(255),
  start_date DATETIME,
  end_date DATETIME,
  created_at DATETIME,
  updated_at DATETIME,
  KEY idx_ems_plan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ems_effect_evaluation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  plan_id BIGINT,
  plan_name VARCHAR(255),
  target_area VARCHAR(255),
  predicted_saving DOUBLE,
  actual_saving DOUBLE,
  evaluation_period VARCHAR(255),
  energy_type VARCHAR(255),
  achievement_rate DOUBLE,
  evaluation_date DATETIME,
  evaluation_content VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME,
  KEY idx_ems_eval_plan (plan_id),
  KEY idx_ems_eval_date (evaluation_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ems_standard_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(180) NOT NULL,
  type VARCHAR(50) NOT NULL,
  frequency VARCHAR(50) NOT NULL,
  last_generated DATETIME,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ems_custom_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(180) NOT NULL,
  creator VARCHAR(80) NOT NULL,
  created_date DATETIME NOT NULL,
  last_modified DATETIME NOT NULL,
  sql_query TEXT,
  template_config TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ems_report_auto_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_name VARCHAR(180) NOT NULL,
  frequency VARCHAR(80) NOT NULL,
  next_execution DATETIME,
  recipients TEXT,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS ems_report_export_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  report_id BIGINT,
  report_name VARCHAR(180) NOT NULL,
  format VARCHAR(20) NOT NULL,
  export_time DATETIME NOT NULL,
  status VARCHAR(20) NOT NULL,
  file_name VARCHAR(255),
  content_type VARCHAR(100),
  content_text LONGTEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

