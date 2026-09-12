-- ========================================
-- WMS 模块（hxcoe003）建表脚本
-- 说明：用于 Docker MySQL 初始化阶段创建 WMS 相关表结构
-- ========================================
USE wms_db;

CREATE TABLE IF NOT EXISTS wms_warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_code VARCHAR(255) NOT NULL,
    warehouse_name VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    manager VARCHAR(255),
    contact VARCHAR(255),
    status INT,
    created_time DATETIME(6),
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_warehouse_code (warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_zone (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_code VARCHAR(64),
    zone_code VARCHAR(64) NOT NULL,
    zone_name VARCHAR(128),
    zone_type VARCHAR(16),
    description VARCHAR(512),
    status VARCHAR(8),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_zone_code (zone_code),
    INDEX idx_wms_zone_warehouse_code (warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_location_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_code VARCHAR(64) NOT NULL,
    type_name VARCHAR(128),
    type_desc VARCHAR(512),
    max_weight DECIMAL(19, 2),
    mix_flag BIT(1),
    status VARCHAR(8),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_location_type_code (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_material_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_code VARCHAR(64) NOT NULL,
    type_name VARCHAR(128),
    description VARCHAR(512),
    status VARCHAR(8),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_material_type_code (type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_barcode_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_code VARCHAR(64) NOT NULL,
    rule_name VARCHAR(128),
    rule_type VARCHAR(32),
    rule_format VARCHAR(256),
    description VARCHAR(512),
    status VARCHAR(8),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_barcode_rule_code (rule_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_code VARCHAR(64),
    zone_code VARCHAR(64),
    location_code VARCHAR(64) NOT NULL,
    location_name VARCHAR(128),
    location_type_code VARCHAR(64),
    status VARCHAR(8),
    remark VARCHAR(512),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_location_code (location_code),
    INDEX idx_wms_location_warehouse_code (warehouse_code),
    INDEX idx_wms_location_zone_code (zone_code),
    INDEX idx_wms_location_type_code (location_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_id BIGINT,
    warehouse_code VARCHAR(255),
    location_code VARCHAR(255),
    material_code VARCHAR(255),
    material_name VARCHAR(255),
    quantity DECIMAL(19, 2),
    unit VARCHAR(255),
    batch_no VARCHAR(255),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    INDEX idx_wms_inventory_warehouse_code (warehouse_code),
    INDEX idx_wms_inventory_location_code (location_code),
    INDEX idx_wms_inventory_material_code (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_inventory_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(255),
    warehouse_code VARCHAR(255),
    location_code VARCHAR(255),
    material_code VARCHAR(255),
    material_name VARCHAR(255),
    quantity DECIMAL(19, 2),
    unit VARCHAR(255),
    batch_no VARCHAR(255),
    source_no VARCHAR(255),
    transaction_time DATETIME(6),
    operator VARCHAR(255),
    INDEX idx_wms_inv_tx_time (transaction_time),
    INDEX idx_wms_inv_tx_material (material_code),
    INDEX idx_wms_inv_tx_location (location_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_asn (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asn_no VARCHAR(255) NOT NULL,
    delivery_note_no VARCHAR(255),
    supplier_code VARCHAR(255),
    supplier_name VARCHAR(255),
    warehouse_id BIGINT,
    warehouse_code VARCHAR(255),
    warehouse_name VARCHAR(255),
    status VARCHAR(255),
    expected_arrival_date DATETIME(6),
    actual_arrival_date DATETIME(6),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_asn_no (asn_no),
    INDEX idx_wms_asn_warehouse_code (warehouse_code),
    INDEX idx_wms_asn_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_asn_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asn_id BIGINT,
    material_code VARCHAR(255),
    material_name VARCHAR(255),
    expected_quantity DECIMAL(19, 2),
    received_quantity DECIMAL(19, 2),
    unit VARCHAR(255),
    batch_no VARCHAR(255),
    INDEX idx_wms_asn_item_asn_id (asn_id),
    INDEX idx_wms_asn_item_material_code (material_code),
    CONSTRAINT fk_wms_asn_item_asn_id
        FOREIGN KEY (asn_id) REFERENCES wms_asn(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_wave (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    wave_no VARCHAR(255) NOT NULL,
    status VARCHAR(255),
    remark VARCHAR(512),
    created_by VARCHAR(64),
    assigned_to VARCHAR(64),
    assigned_time DATETIME(6),
    released_time DATETIME(6),
    completed_time DATETIME(6),
    total_quantity DECIMAL(19, 4),
    order_count INT,
    order_type VARCHAR(32),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_wave_no (wave_no),
    INDEX idx_wms_wave_status (status),
    INDEX idx_wms_wave_assigned_to (assigned_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_outbound_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    type VARCHAR(255),
    source_no VARCHAR(255),
    customer_name VARCHAR(255),
    address VARCHAR(255),
    status VARCHAR(255),
    wave_id BIGINT,
    created_time DATETIME(6),
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_outbound_order_no (order_no),
    INDEX idx_wms_outbound_order_status (status),
    INDEX idx_wms_outbound_order_wave_id (wave_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_outbound_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    outbound_order_id BIGINT,
    material_code VARCHAR(255),
    material_name VARCHAR(255),
    quantity DECIMAL(19, 2),
    unit VARCHAR(255),
    location_code VARCHAR(255),
    batch_no VARCHAR(255),
    INDEX idx_wms_outbound_item_order_id (outbound_order_id),
    INDEX idx_wms_outbound_item_material_code (material_code),
    CONSTRAINT fk_wms_outbound_item_order_id
        FOREIGN KEY (outbound_order_id) REFERENCES wms_outbound_order(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_picking_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_no VARCHAR(64) NOT NULL,
    wave_id BIGINT,
    wave_no VARCHAR(255),
    outbound_order_id BIGINT,
    order_no VARCHAR(64),
    status VARCHAR(32),
    operator_id VARCHAR(64),
    operator_name VARCHAR(64),
    assigned_time DATETIME(6),
    start_time DATETIME(6),
    end_time DATETIME(6),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_picking_task_no (task_no),
    INDEX idx_wms_picking_task_status (status),
    INDEX idx_wms_picking_task_wave_id (wave_id),
    INDEX idx_wms_picking_task_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_picking_task_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT,
    outbound_order_item_id BIGINT,
    material_code VARCHAR(255),
    material_name VARCHAR(255),
    unit VARCHAR(255),
    quantity DECIMAL(19, 2),
    location_code VARCHAR(255),
    batch_no VARCHAR(255),
    status VARCHAR(255),
    pick_time DATETIME(6),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    INDEX idx_wms_pick_item_task_id (task_id),
    INDEX idx_wms_pick_item_material_code (material_code),
    CONSTRAINT fk_wms_picking_task_item_task_id
        FOREIGN KEY (task_id) REFERENCES wms_picking_task(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_stock_count_job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    count_no VARCHAR(64) NOT NULL,
    warehouse_code VARCHAR(64),
    count_type VARCHAR(8),
    status VARCHAR(8),
    create_user VARCHAR(64),
    create_time DATETIME(6),
    start_time DATETIME(6),
    end_time DATETIME(6),
    total_item_count INT,
    finished_item_count INT,
    diff_count INT,
    updated_time DATETIME(6),
    UNIQUE KEY uk_wms_stock_count_job_no (count_no),
    INDEX idx_wms_stock_count_job_status (status),
    INDEX idx_wms_stock_count_job_warehouse_code (warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS wms_stock_count_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_id BIGINT,
    location_code VARCHAR(255),
    material_code VARCHAR(255),
    material_name VARCHAR(255),
    batch_no VARCHAR(255),
    unit VARCHAR(255),
    sys_qty DECIMAL(19, 2),
    count_qty DECIMAL(19, 2),
    diff_qty DECIMAL(19, 2),
    scan_time DATETIME(6),
    created_time DATETIME(6),
    updated_time DATETIME(6),
    INDEX idx_wms_stock_count_item_job_id (job_id),
    INDEX idx_wms_stock_count_item_material_code (material_code),
    CONSTRAINT fk_wms_stock_count_item_job_id
        FOREIGN KEY (job_id) REFERENCES wms_stock_count_job(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
