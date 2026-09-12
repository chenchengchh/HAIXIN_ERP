-- APS模块数据库表结构（与JPA实体定义保持一致；实际建表由Hibernate ddl-auto=update完成，此脚本作为参考DDL与初始化脚本使用）

-- 创建资源表
CREATE TABLE IF NOT EXISTS aps_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_aps_resource_name (name)
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
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_aps_process_name_workshop (process_name, workshop)
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

-- 创建生产批号规则表（列名为sep，与实体BatchNumberRuleEntity的@Column(name="sep")一致）
CREATE TABLE IF NOT EXISTS aps_batch_number_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prefix VARCHAR(20) NOT NULL,
    date_format VARCHAR(20) NOT NULL,
    serial_length INT NOT NULL,
    sep VARCHAR(10) NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_aps_batch_rule_prefix (prefix)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建生产计划表（包含产品编码/名称/数量列，与ProductionPlanEntity一致）
CREATE TABLE IF NOT EXISTS aps_production_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_no VARCHAR(50) NOT NULL UNIQUE,
    plan_name VARCHAR(100) NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    product_code VARCHAR(255),
    product_name VARCHAR(255),
    quantity DECIMAL(19, 2),
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

-- 创建调度详情表（与ScheduleDetailEntity一致，存放每个资源上的排程片段）
CREATE TABLE IF NOT EXISTS aps_schedule_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    schedule_result_id BIGINT COMMENT '所属排程结果ID：区分同一计划的多次排程',
    resource_id BIGINT NOT NULL,
    resource_name VARCHAR(255),
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    quantity DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_aps_detail_result (schedule_result_id),
    KEY idx_aps_detail_plan_status (plan_id, status)
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
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_aps_res_constraint_rid (resource_id)
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
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_aps_algo_param_name (algorithm_name, param_name)
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

-- 创建多周期计划主表
CREATE TABLE IF NOT EXISTS aps_multi_period_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_no VARCHAR(50) NOT NULL UNIQUE COMMENT '多周期计划编号',
    source_plan_id BIGINT NOT NULL COMMENT '源生产计划ID',
    source_plan_no VARCHAR(50) COMMENT '源生产计划编号',
    plan_name VARCHAR(100) NOT NULL COMMENT '计划名称',
    period_type VARCHAR(20) NOT NULL COMMENT '周期类型：daily/weekly/monthly/quarterly',
    period_count INT NOT NULL COMMENT '周期数量',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    algorithm VARCHAR(50) COMMENT '排程算法',
    status VARCHAR(20) NOT NULL COMMENT '状态：已生成/执行中/已完成/已取消',
    remark VARCHAR(500) COMMENT '备注',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (source_plan_id) REFERENCES aps_production_plan(id),
    KEY idx_multi_period_plan_source (source_plan_id),
    KEY idx_multi_period_plan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多周期计划主表';

-- 创建多周期计划明细表
CREATE TABLE IF NOT EXISTS aps_multi_period_plan_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL COMMENT '多周期计划ID',
    period_index INT NOT NULL COMMENT '周期序号',
    period_start DATE NOT NULL COMMENT '周期开始日期',
    period_end DATE NOT NULL COMMENT '周期结束日期',
    quantity DECIMAL(19, 2) NOT NULL COMMENT '计划数量',
    status VARCHAR(20) NOT NULL COMMENT '状态：已生成/执行中/已完成/已取消',
    remark VARCHAR(500) COMMENT '备注',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES aps_multi_period_plan(id) ON DELETE CASCADE,
    KEY idx_multi_period_item_plan (plan_id),
    KEY idx_multi_period_item_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多周期计划明细表';
