CREATE DATABASE IF NOT EXISTS les_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE les_db;

CREATE TABLE IF NOT EXISTS les_vehicle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    license_plate VARCHAR(50) NOT NULL,
    vehicle_type VARCHAR(50) NULL,
    load_capacity DECIMAL(15,2) NULL,
    status VARCHAR(20) NULL,
    create_time DATETIME NULL,
    UNIQUE KEY uk_les_vehicle_license_plate (license_plate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS les_driver (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NULL,
    license_no VARCHAR(50) NULL,
    status VARCHAR(20) NULL,
    create_time DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS les_route (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    route_name VARCHAR(100) NOT NULL,
    start_location VARCHAR(100) NULL,
    end_location VARCHAR(100) NULL,
    distance DECIMAL(15,2) NULL,
    estimated_time INT NULL,
    create_time DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS les_transport_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_no VARCHAR(50) NOT NULL,
    sales_order_no VARCHAR(50) NULL COMMENT '关联销售订单号（或WMS出库单号）',
    source_type VARCHAR(20) NULL COMMENT '来源类型：WMS/CRM/LOCAL',
    source_no VARCHAR(50) NULL COMMENT '来源单据号',
    vehicle_id BIGINT NULL,
    driver_id BIGINT NULL,
    route_id BIGINT NULL,
    status INT NULL COMMENT '1:计划中 2:运输中 3:已签收 4:异常中断',
    cost_estimated DECIMAL(15,2) NULL,
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    UNIQUE KEY uk_les_transport_plan_plan_no (plan_no),
    KEY idx_les_transport_plan_status (status),
    KEY idx_les_transport_plan_sales_order_no (sales_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS les_transport_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_no VARCHAR(50) NOT NULL,
    plan_id BIGINT NULL,
    driver_id BIGINT NULL,
    vehicle_id BIGINT NULL,
    status VARCHAR(20) NULL,
    create_time DATETIME NULL,
    start_time DATETIME NULL,
    end_time DATETIME NULL,
    UNIQUE KEY uk_les_transport_task_task_no (task_no),
    KEY idx_les_transport_task_plan_id (plan_id),
    KEY idx_les_transport_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS les_monitor_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NULL,
    vehicle_id BIGINT NULL,
    longitude DECIMAL(12,8) NULL,
    latitude DECIMAL(12,8) NULL,
    current_status VARCHAR(50) NULL,
    is_anomaly TINYINT(1) DEFAULT 0,
    record_time DATETIME NULL,
    KEY idx_les_monitor_log_plan_id (plan_id),
    KEY idx_les_monitor_log_vehicle_id (vehicle_id),
    KEY idx_les_monitor_log_record_time (record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS les_anomaly_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NULL,
    vehicle_id BIGINT NULL,
    event_no VARCHAR(50) NULL,
    event_type VARCHAR(50) NULL,
    event_desc VARCHAR(500) NULL,
    event_description VARCHAR(500) NULL,
    event_time DATETIME NULL,
    handling_status VARCHAR(20) NULL,
    handling_result VARCHAR(500) NULL,
    create_time DATETIME NULL,
    KEY idx_les_anomaly_event_plan_id (plan_id),
    KEY idx_les_anomaly_event_vehicle_id (vehicle_id),
    KEY idx_les_anomaly_event_handling_status (handling_status),
    KEY idx_les_anomaly_event_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS les_vehicle_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NULL,
    vehicle_id BIGINT NULL,
    license_plate VARCHAR(50) NULL,
    longitude DECIMAL(12,8) NULL,
    latitude DECIMAL(12,8) NULL,
    speed DECIMAL(10,2) NULL,
    direction DECIMAL(10,2) NULL,
    record_time DATETIME NULL,
    KEY idx_les_vehicle_location_vehicle_id (vehicle_id),
    KEY idx_les_vehicle_location_plan_id (plan_id),
    KEY idx_les_vehicle_location_record_time (record_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS les_sign_voucher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NULL,
    customer_sign VARCHAR(200) NULL,
    photo_urls TEXT NULL,
    arrival_time DATETIME NULL,
    on_time_status INT NULL COMMENT '1:准时 2:早到 3:延误',
    status VARCHAR(20) NULL COMMENT 'signed/pending/exception',
    create_time DATETIME NULL,
    update_time DATETIME NULL,
    KEY idx_les_sign_voucher_plan_id (plan_id),
    KEY idx_les_sign_voucher_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS les_transport_cost (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NULL,
    fuel_cost DECIMAL(15,2) NULL,
    toll_cost DECIMAL(15,2) NULL,
    driver_salary DECIMAL(15,2) NULL,
    other_cost DECIMAL(15,2) NULL,
    total_cost DECIMAL(15,2) NULL,
    create_time DATETIME NULL,
    KEY idx_les_transport_cost_plan_id (plan_id),
    KEY idx_les_transport_cost_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS les_service_quality (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NULL,
    sign_success_rate DECIMAL(10,2) NULL,
    cargo_integrity_rate DECIMAL(10,2) NULL,
    on_time_rate DECIMAL(10,2) NULL,
    customer_satisfaction DECIMAL(10,2) NULL,
    create_time DATETIME NULL,
    KEY idx_les_service_quality_plan_id (plan_id),
    KEY idx_les_service_quality_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

