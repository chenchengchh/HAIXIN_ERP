-- ========================================
-- AGV 模块建表脚本
-- 说明：用于 Docker MySQL 初始化阶段创建 AGV 相关表结构
-- ========================================
CREATE DATABASE IF NOT EXISTS agv_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agv_db;

CREATE TABLE IF NOT EXISTS agv_device (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    agv_code VARCHAR(64) NOT NULL,
    agv_name VARCHAR(128),
    agv_type VARCHAR(64),
    model VARCHAR(64),
    status VARCHAR(32),
    battery_level INT,
    voltage DECIMAL(10, 2),
    temperature DECIMAL(10, 2),
    speed DECIMAL(10, 2),
    direction VARCHAR(32),
    position VARCHAR(128),
    current_task_id VARCHAR(64),
    load_status VARCHAR(32),
    last_update DATETIME,
    remark VARCHAR(512),
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agv_device_code (agv_code),
    INDEX idx_agv_device_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL,
    task_no VARCHAR(64),
    task_type VARCHAR(32),
    priority INT,
    status VARCHAR(32),
    agv_code VARCHAR(64),
    start_point VARCHAR(128),
    end_point VARCHAR(128),
    payload VARCHAR(1024),
    track_status VARCHAR(64),
    progress INT,
    estimated_completion DATETIME,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_time DATETIME,
    end_time DATETIME,
    remark VARCHAR(512),
    UNIQUE KEY uk_agv_task_task_id (task_id),
    UNIQUE KEY uk_agv_task_task_no (task_no),
    INDEX idx_agv_task_status (status),
    INDEX idx_agv_task_agv_code (agv_code),
    INDEX idx_agv_task_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_task_assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL,
    agv_code VARCHAR(64) NOT NULL,
    assigned_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(64),
    assignment_status VARCHAR(32),
    INDEX idx_agv_task_assignment_task_id (task_id),
    INDEX idx_agv_task_assignment_agv_code (agv_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_path_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id VARCHAR(64) NOT NULL,
    task_id VARCHAR(64),
    agv_code VARCHAR(64),
    start_point VARCHAR(128),
    end_point VARCHAR(128),
    distance DECIMAL(19, 4),
    estimated_time DECIMAL(19, 4),
    algorithm VARCHAR(64),
    status VARCHAR(32),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agv_path_plan_plan_id (plan_id),
    INDEX idx_agv_path_plan_task_id (task_id),
    INDEX idx_agv_path_plan_agv_code (agv_code),
    INDEX idx_agv_path_plan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_path_point (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id VARCHAR(64) NOT NULL,
    seq_no INT NOT NULL,
    node_code VARCHAR(128),
    x DECIMAL(19, 6),
    y DECIMAL(19, 6),
    heading DECIMAL(19, 6),
    speed_limit DECIMAL(10, 2),
    remark VARCHAR(512),
    UNIQUE KEY uk_agv_path_point_plan_seq (plan_id, seq_no),
    INDEX idx_agv_path_point_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_traffic_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    node_code VARCHAR(128) NOT NULL,
    node_name VARCHAR(128),
    area_code VARCHAR(64),
    status VARCHAR(32),
    locked_by_agv_code VARCHAR(64),
    locked_time DATETIME,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agv_traffic_node_code (node_code),
    INDEX idx_agv_traffic_node_status (status),
    INDEX idx_agv_traffic_node_area (area_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_traffic_lock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    node_code VARCHAR(128) NOT NULL,
    agv_code VARCHAR(64),
    lock_status VARCHAR(32),
    lock_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    release_time DATETIME,
    remark VARCHAR(512),
    INDEX idx_agv_traffic_lock_node_code (node_code),
    INDEX idx_agv_traffic_lock_agv_code (agv_code),
    INDEX idx_agv_traffic_lock_status (lock_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_traffic_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_name VARCHAR(128) NOT NULL,
    rule_type VARCHAR(64),
    priority INT,
    enabled BIT(1),
    condition_json VARCHAR(2048),
    action_json VARCHAR(2048),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_agv_traffic_rule_enabled (enabled),
    INDEX idx_agv_traffic_rule_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_fault_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    alert_level VARCHAR(32),
    alert_type VARCHAR(64),
    agv_code VARCHAR(64),
    title VARCHAR(256),
    message VARCHAR(1024),
    status VARCHAR(32),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    handle_time DATETIME,
    handle_result VARCHAR(1024),
    operator VARCHAR(64),
    INDEX idx_agv_fault_alert_status (status),
    INDEX idx_agv_fault_alert_agv_code (agv_code),
    INDEX idx_agv_fault_alert_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    log_type VARCHAR(64),
    agv_code VARCHAR(64),
    ref_id VARCHAR(64),
    content VARCHAR(2048),
    operator VARCHAR(64),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_agv_oplog_agv_code (agv_code),
    INDEX idx_agv_oplog_create_time (create_time),
    INDEX idx_agv_oplog_type (log_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_collaboration_strategy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    strategy_id VARCHAR(64) NOT NULL,
    strategy_name VARCHAR(128),
    description VARCHAR(512),
    strategy_config_json VARCHAR(2048),
    status VARCHAR(32),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agv_collab_strategy_id (strategy_id),
    INDEX idx_agv_collab_strategy_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_collision_avoidance_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(64) NOT NULL,
    config_value VARCHAR(2048),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agv_collision_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_collaboration_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    log_code VARCHAR(64),
    strategy_id VARCHAR(64),
    agv_code VARCHAR(64),
    content VARCHAR(2048),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_agv_collab_log_strategy (strategy_id),
    INDEX idx_agv_collab_log_agv_code (agv_code),
    INDEX idx_agv_collab_log_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS agv_area (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    area_id VARCHAR(64) NOT NULL,
    area_name VARCHAR(128),
    area_type VARCHAR(64),
    polygon_json VARCHAR(4096),
    status VARCHAR(32),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agv_area_id (area_id),
    INDEX idx_agv_area_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
