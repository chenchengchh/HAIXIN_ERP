SET time_zone = '+8:00';
SET FOREIGN_KEY_CHECKS = 0;

USE eam_db;

-- 1. 资产管理表
CREATE TABLE IF NOT EXISTS eam_asset_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT COMMENT '父分类ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备分类表';

CREATE TABLE IF NOT EXISTS eam_asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '设备名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编码',
    category_id BIGINT COMMENT '分类ID',
    category_name VARCHAR(100) COMMENT '分类名称快照',
    model VARCHAR(100) COMMENT '规格型号',
    manufacturer VARCHAR(100) COMMENT '制造商',
    status VARCHAR(20) DEFAULT 'running' COMMENT '状态: running/stopped/maintenance/scrapped',
    location VARCHAR(100) COMMENT '安装位置',
    purchase_date DATE COMMENT '采购日期',
    start_date DATE COMMENT '启用日期',
    original_value DECIMAL(15,2) COMMENT '原值',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备台账表';

CREATE TABLE IF NOT EXISTS eam_asset_hierarchy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NOT NULL COMMENT '父设备ID',
    child_id BIGINT NOT NULL COMMENT '子设备ID',
    component_type VARCHAR(50) COMMENT '组件类型: core/consumable/accessory',
    quantity INT DEFAULT 1 COMMENT '数量',
    remark VARCHAR(200) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备层级关系表';

-- 2. 维护管理表
CREATE TABLE IF NOT EXISTS eam_maintenance_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    equipment_name VARCHAR(100) COMMENT '设备名称快照',
    type VARCHAR(50) NOT NULL COMMENT '维护类型: preventive/check/lubrication',
    frequency VARCHAR(50) COMMENT '频率描述',
    next_execution DATE COMMENT '下次执行日期',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active/inactive',
    description TEXT COMMENT '计划描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维护计划表';

CREATE TABLE IF NOT EXISTS eam_workorder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50) NOT NULL COMMENT '类型: preventive/repair/emergency',
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    equipment_name VARCHAR(100) COMMENT '设备名称快照',
    priority VARCHAR(20) DEFAULT 'medium' COMMENT '优先级: high/medium/low',
    assignee VARCHAR(50) COMMENT '指派给',
    description TEXT COMMENT '工单描述',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/processing/completed/cancelled',
    plan_id BIGINT COMMENT '关联计划ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单表';

CREATE TABLE IF NOT EXISTS eam_fault_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    equipment_name VARCHAR(100) COMMENT '设备名称快照',
    type VARCHAR(50) COMMENT '故障类型',
    description TEXT COMMENT '故障描述',
    reporter VARCHAR(50) COMMENT '报修人',
    contact VARCHAR(50) COMMENT '联系方式',
    report_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报修时间',
    status VARCHAR(20) DEFAULT 'reported' COMMENT '状态: reported/processing/completed',
    workorder_id BIGINT COMMENT '关联工单ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='故障记录表';

CREATE TABLE IF NOT EXISTS eam_maintenance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    equipment_name VARCHAR(100) COMMENT '设备名称快照',
    type VARCHAR(50) COMMENT '维护类型',
    content TEXT COMMENT '维护内容',
    maintenance_time DATETIME COMMENT '维护时间',
    maintainer VARCHAR(50) COMMENT '维护人员',
    cost DECIMAL(10,2) DEFAULT 0 COMMENT '维护成本',
    workorder_id BIGINT COMMENT '关联工单ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维护履历表';

-- 3. 备件管理表
CREATE TABLE IF NOT EXISTS eam_spare_part (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '备件名称',
    code VARCHAR(50) UNIQUE COMMENT '备件编码',
    model VARCHAR(100) COMMENT '规格型号',
    category VARCHAR(50) COMMENT '分类',
    supplier VARCHAR(100) COMMENT '供应商',
    unit VARCHAR(20) COMMENT '单位',
    safety_stock INT DEFAULT 0 COMMENT '安全库存',
    current_stock INT DEFAULT 0 COMMENT '当前总库存',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备件基础信息表';

CREATE TABLE IF NOT EXISTS eam_spare_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spare_id BIGINT NOT NULL COMMENT '备件ID',
    location VARCHAR(50) COMMENT '库位',
    quantity INT DEFAULT 0 COMMENT '库存数量',
    status VARCHAR(20) DEFAULT 'normal' COMMENT '状态: normal/low/excess',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备件库存明细表';

CREATE TABLE IF NOT EXISTS eam_spare_demand_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spare_id BIGINT NOT NULL COMMENT '备件ID',
    required_qty INT NOT NULL COMMENT '需求数量',
    suggested_date DATE COMMENT '建议采购日期',
    maintenance_plan_id BIGINT COMMENT '关联维护计划ID',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/ordered/fulfilled',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备件需求计划表';

CREATE TABLE IF NOT EXISTS eam_spare_issue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    spare_id BIGINT NOT NULL COMMENT '备件ID',
    quantity INT NOT NULL COMMENT '数量',
    applicant VARCHAR(50) COMMENT '申请人',
    application_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/approved/rejected',
    return_status VARCHAR(20) DEFAULT 'not_returned' COMMENT '归还状态: not_returned/returned/consumed',
    remark VARCHAR(200) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备件领用记录表';

-- 4. 绩效指标表
CREATE TABLE IF NOT EXISTS eam_asset_kpi (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asset_id BIGINT NOT NULL COMMENT '设备ID',
    record_month VARCHAR(7) NOT NULL COMMENT '月份 YYYY-MM',
    oee_value DECIMAL(5,2) COMMENT 'OEE值',
    mtbf_hours DECIMAL(10,2) COMMENT '平均故障间隔',
    mttr_hours DECIMAL(10,2) COMMENT '平均修复时间',
    maintenance_cost DECIMAL(15,2) COMMENT '维护成本',
    downtime_minutes INT COMMENT '停机分钟数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_asset_month (asset_id, record_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备绩效指标表';

-- 5. 初始化部分测试数据
INSERT INTO eam_asset_category (name, parent_id) VALUES 
('加工设备', NULL), ('辅助设备', NULL), ('检测设备', NULL),
('车床', 1), ('铣床', 1), ('钻床', 1),
('空压机', 2), ('冷却塔', 2);

INSERT INTO eam_asset (name, code, category_id, category_name, model, manufacturer, status) VALUES
('数控车床', 'CNC-001', 4, '车床', 'CK6150', '沈阳机床', 'running'),
('立式铣床', 'MILL-001', 5, '铣床', 'X5032', '北京铣床厂', 'running'),
('摇臂钻床', 'DRILL-001', 6, '钻床', 'Z3050', '上海机床厂', 'running'),
('螺杆空压机', 'AIR-001', 7, '空压机', 'GA75', '阿特拉斯', 'running');

INSERT INTO eam_spare_part (name, code, model, category, supplier, unit, safety_stock, current_stock) VALUES
('电机轴承', 'SP001', '6205-2RS', '机械部件', '轴承供应商A', '个', 50, 32),
('控制继电器', 'SP002', 'RXM2LB2BD', '电气部件', '电气供应商B', '个', 30, 25),
('液压油缸', 'SP003', 'HOB80*100', '液压部件', '液压供应商C', '个', 10, 8);

SET FOREIGN_KEY_CHECKS = 1;
