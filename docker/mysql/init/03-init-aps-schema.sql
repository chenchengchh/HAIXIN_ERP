-- 创建资源表
CREATE TABLE IF NOT EXISTS aps_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建工艺表
CREATE TABLE IF NOT EXISTS aps_process (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    process_name VARCHAR(100) NOT NULL,
    workshop VARCHAR(100) NOT NULL,
    sequence INT NOT NULL,
    processing_time INT NOT NULL,
    setup_time INT NOT NULL,
    teardown_time INT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建产品表
CREATE TABLE IF NOT EXISTS aps_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    skin_type VARCHAR(50) NOT NULL,
    ingredient VARCHAR(500) NOT NULL,
    production_line VARCHAR(50) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建生产批号规则表
CREATE TABLE IF NOT EXISTS aps_batch_number_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prefix VARCHAR(20) NOT NULL,
    date_format VARCHAR(20) NOT NULL,
    serial_length INT NOT NULL,
    `separator` VARCHAR(10) NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建生产计划表
CREATE TABLE IF NOT EXISTS aps_production_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_no VARCHAR(50) NOT NULL UNIQUE,
    plan_name VARCHAR(100) NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    remark VARCHAR(500),
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建调度结果表
CREATE TABLE IF NOT EXISTS aps_schedule_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    schedule_no VARCHAR(50) NOT NULL UNIQUE,
    algorithm VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES aps_production_plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建资源约束表
CREATE TABLE IF NOT EXISTS aps_resource_constraint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    constraint_type VARCHAR(20) NOT NULL,
    capacity INT NOT NULL,
    available_capacity INT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建算法参数配置表
CREATE TABLE IF NOT EXISTS aps_algorithm_param (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    algorithm_name VARCHAR(50) NOT NULL,
    param_name VARCHAR(50) NOT NULL,
    param_value VARCHAR(100) NOT NULL,
    param_type VARCHAR(20) NOT NULL,
    description VARCHAR(200),
    is_default BOOLEAN NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建优化建议表
CREATE TABLE IF NOT EXISTS aps_optimization_suggestion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_result_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    suggestion VARCHAR(500) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_result_id) REFERENCES aps_schedule_result(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建资源负载表
CREATE TABLE IF NOT EXISTS aps_resource_load (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    load_rate DOUBLE NOT NULL,
    total_capacity INT NOT NULL,
    used_capacity INT NOT NULL,
    available_capacity INT NOT NULL,
    time_period VARCHAR(20) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES aps_production_plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建偏差预警表
CREATE TABLE IF NOT EXISTS aps_deviation_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    order_id BIGINT,
    task_id BIGINT,
    type VARCHAR(50) NOT NULL,
    description VARCHAR(500) NOT NULL,
    deviation_value DOUBLE NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES aps_production_plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建工艺路线表
CREATE TABLE IF NOT EXISTS aps_process_route (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_name VARCHAR(100) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    total_time INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建工艺操作表
CREATE TABLE IF NOT EXISTS aps_process_operation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    process_id BIGINT NOT NULL,
    process_name VARCHAR(100) NOT NULL,
    sequence INT NOT NULL,
    resource_id BIGINT NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    processing_time INT NOT NULL,
    setup_time INT NOT NULL,
    teardown_time INT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (route_id) REFERENCES aps_process_route(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建资源日历表
CREATE TABLE IF NOT EXISTS aps_resource_calendar (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    shift VARCHAR(20) NOT NULL,
    available BOOLEAN NOT NULL,
    available_hours DOUBLE NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建资源能力表
CREATE TABLE IF NOT EXISTS aps_resource_capability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    capability_type VARCHAR(50) NOT NULL,
    capability_value DOUBLE NOT NULL DEFAULT 0,
    unit VARCHAR(20) NOT NULL,
    efficiency DOUBLE NOT NULL DEFAULT 100,
    operation_type VARCHAR(255) NOT NULL DEFAULT 'production',
    priority INT NOT NULL DEFAULT 1,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建调度任务表
CREATE TABLE IF NOT EXISTS aps_schedule_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_result_id BIGINT NOT NULL,
    task_no VARCHAR(50) NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    process_id BIGINT NOT NULL,
    process_name VARCHAR(100) NOT NULL,
    resource_id BIGINT NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    duration INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    sequence INT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_result_id) REFERENCES aps_schedule_result(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建调度历史表
CREATE TABLE IF NOT EXISTS aps_schedule_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    schedule_no VARCHAR(50) NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    execution_time DOUBLE NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES aps_production_plan(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建调度约束表
CREATE TABLE IF NOT EXISTS aps_scheduling_constraint (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    constraint_name VARCHAR(100) NOT NULL,
    constraint_type VARCHAR(50) NOT NULL,
    priority INT NOT NULL,
    value VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
