-- ========================================
-- OA模块数据库初始化脚本
-- 创建日期：2024-12-24
-- 说明：创建OA模块的所有数据库表
-- ========================================

USE oa_db;

-- ========================================
-- 1. 流程审批表
-- ========================================

-- 审批流程定义表
CREATE TABLE IF NOT EXISTS oa_approval_process (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '流程名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '流程编码',
    description TEXT COMMENT '流程描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    process_type VARCHAR(50) NOT NULL COMMENT '流程类型',
    process_definition TEXT NOT NULL COMMENT '流程定义JSON',
    form_config TEXT COMMENT '表单配置JSON',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater_id BIGINT COMMENT '更新人ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除状态：0-未删除，1-已删除',
    INDEX idx_process_type (process_type),
    INDEX idx_status (status),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='审批流程定义表';

-- 审批实例表
CREATE TABLE IF NOT EXISTS oa_approval_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    process_id BIGINT NOT NULL COMMENT '流程定义ID',
    process_code VARCHAR(50) NOT NULL COMMENT '流程定义编码',
    title VARCHAR(200) NOT NULL COMMENT '流程实例标题',
    description TEXT COMMENT '流程实例描述',
    initiator_id BIGINT NOT NULL COMMENT '发起者ID',
    initiator_name VARCHAR(50) NOT NULL COMMENT '发起者名称',
    current_node_id VARCHAR(50) COMMENT '当前节点ID',
    current_node_name VARCHAR(100) COMMENT '当前节点名称',
    status VARCHAR(20) NOT NULL COMMENT '状态：PENDING-待审批，APPROVED-已通过，REJECTED-已拒绝，CANCELLED-已取消，COMPLETED-已完成',
    form_data TEXT COMMENT '表单数据JSON',
    process_variables TEXT COMMENT '流程变量JSON',
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (process_id) REFERENCES oa_approval_process(id),
    INDEX idx_process (process_id),
    INDEX idx_process_code (process_code),
    INDEX idx_initiator (initiator_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='审批流程实例表';

-- 审批任务表
CREATE TABLE IF NOT EXISTS oa_approval_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    name VARCHAR(100) NOT NULL COMMENT '任务名称',
    description TEXT COMMENT '任务描述',
    node_id VARCHAR(50) NOT NULL COMMENT '节点ID',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    assignee_id BIGINT NOT NULL COMMENT '审批人ID',
    assignee_name VARCHAR(50) NOT NULL COMMENT '审批人名称',
    status VARCHAR(20) NOT NULL COMMENT '任务状态：PENDING-待处理，COMPLETED-已完成，CANCELLED-已取消',
    result VARCHAR(20) COMMENT '审批结果：APPROVED-同意，REJECTED-拒绝',
    comment TEXT COMMENT '审批意见',
    approve_time DATETIME COMMENT '审批时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    due_time DATETIME COMMENT '过期时间',
    FOREIGN KEY (instance_id) REFERENCES oa_approval_instance(id) ON DELETE CASCADE,
    INDEX idx_instance (instance_id),
    INDEX idx_assignee (assignee_id),
    INDEX idx_status (status),
    INDEX idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='审批任务表';

-- ========================================
-- 2. 文档管理表
-- ========================================

-- 文档分类表
CREATE TABLE IF NOT EXISTS oa_document_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description TEXT COMMENT '分类描述',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
    path VARCHAR(255) NOT NULL COMMENT '分类路径，如：0,1,2',
    level INT NOT NULL DEFAULT 1 COMMENT '分类层级',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater_id BIGINT COMMENT '更新人ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent (parent_id),
    INDEX idx_path (path),
    INDEX idx_level (level),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='文档分类表';

-- 文档表
CREATE TABLE IF NOT EXISTS oa_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '文档标题',
    description TEXT COMMENT '文档描述',
    category_id BIGINT NOT NULL COMMENT '文档分类ID',
    category_name VARCHAR(100) NOT NULL COMMENT '文档分类名称',
    file_type VARCHAR(20) NOT NULL COMMENT '文档类型：TXT-文本文件，WORD-Word文档，EXCEL-Excel文档，PPT-PowerPoint文档，PDF-PDF文档，IMAGE-图片，OTHER-其他类型',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_hash VARCHAR(100) COMMENT '文件哈希值（用于版本控制）',
    status VARCHAR(20) NOT NULL COMMENT '文档状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档，DELETED-已删除',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    creator_name VARCHAR(50) NOT NULL COMMENT '创建人名称',
    last_modifier_id BIGINT COMMENT '最后修改人ID',
    last_modifier_name VARCHAR(50) COMMENT '最后修改人名称',
    access_permission VARCHAR(20) NOT NULL COMMENT '访问权限：PUBLIC-公开，PRIVATE-私有，DEPARTMENT-部门可见，ROLE-角色可见',
    download_count INT NOT NULL DEFAULT 0 COMMENT '下载次数',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    publish_time DATETIME COMMENT '发布时间',
    FOREIGN KEY (category_id) REFERENCES oa_document_category(id),
    INDEX idx_category (category_id),
    INDEX idx_creator (creator_id),
    INDEX idx_status (status),
    INDEX idx_file_type (file_type),
    INDEX idx_access_permission (access_permission)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='文档表';

-- 文档版本表
CREATE TABLE IF NOT EXISTS oa_document_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    document_id BIGINT NOT NULL COMMENT '文档ID',
    version VARCHAR(20) NOT NULL COMMENT '版本号',
    version_name VARCHAR(100) NOT NULL COMMENT '版本名称',
    description TEXT COMMENT '版本描述',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_hash VARCHAR(100) COMMENT '文件哈希值',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    creator_name VARCHAR(50) NOT NULL COMMENT '创建人名称',
    is_current TINYINT NOT NULL DEFAULT 0 COMMENT '是否为当前版本：1-是，0-否',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (document_id) REFERENCES oa_document(id) ON DELETE CASCADE,
    INDEX idx_document (document_id),
    INDEX idx_is_current (is_current)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='文档版本表';

-- ========================================
-- 3. 会议管理表
-- ========================================

-- 会议室表
CREATE TABLE IF NOT EXISTS oa_meeting_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    room_code VARCHAR(50) NOT NULL UNIQUE COMMENT '会议室编码',
    room_name VARCHAR(100) NOT NULL COMMENT '会议室名称',
    location VARCHAR(255) COMMENT '位置',
    capacity INT COMMENT '容纳人数',
    equipment TEXT COMMENT '设备清单',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='会议室表';

-- 会议预约表
CREATE TABLE IF NOT EXISTS oa_meeting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    meeting_no VARCHAR(50) NOT NULL UNIQUE COMMENT '会议编号',
    meeting_title VARCHAR(200) NOT NULL COMMENT '会议主题',
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    room_name VARCHAR(100) NOT NULL COMMENT '会议室名称',
    organizer_id BIGINT NOT NULL COMMENT '组织者ID',
    organizer_name VARCHAR(50) NOT NULL COMMENT '组织者姓名',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    attendees TEXT COMMENT '参会人员（JSON数组）',
    agenda TEXT COMMENT '会议议程',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待召开，1-进行中，2-已结束，3-已取消',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (room_id) REFERENCES oa_meeting_room(id),
    INDEX idx_room (room_id),
    INDEX idx_organizer (organizer_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='会议预约表';

-- ========================================
-- 4. 公告通知表
-- ========================================

-- 公告表
CREATE TABLE IF NOT EXISTS oa_announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    announcement_no VARCHAR(50) NOT NULL UNIQUE COMMENT '公告编号',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    announcement_type TINYINT NOT NULL COMMENT '公告类型：1-公司公告，2-部门公告，3-紧急通知',
    priority TINYINT DEFAULT 3 COMMENT '优先级：1-紧急，2-重要，3-普通',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    publisher_name VARCHAR(50) NOT NULL COMMENT '发布人姓名',
    publish_date DATETIME NOT NULL COMMENT '发布时间',
    target_scope TINYINT DEFAULT 1 COMMENT '目标范围：1-全员，2-指定部门，3-指定人员',
    target_ids TEXT COMMENT '目标ID列表（JSON数组）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已撤回',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_announcement_type (announcement_type),
    INDEX idx_publish_date (publish_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='公告表';

-- ========================================
-- 插入初始化数据
-- ========================================

-- 插入默认流程定义
INSERT INTO oa_approval_process (name, code, process_type, process_definition, creator_id, status) VALUES
('请假审批流程', 'LEAVE-001', 'leave', '{"nodes": [], "edges": []}', 1, 1),
('报销审批流程', 'EXPENSE-001', 'expense', '{"nodes": [], "edges": []}', 1, 1),
('采购审批流程', 'PURCHASE-001', 'purchase', '{"nodes": [], "edges": []}', 1, 1),
('出差审批流程', 'TRAVEL-001', 'travel', '{"nodes": [], "edges": []}', 1, 1);

-- 插入默认会议室
INSERT INTO oa_meeting_room (room_code, room_name, location, capacity, status) VALUES
('MR001', '大会议室', '1楼', 50, 1),
('MR002', '小会议室A', '2楼', 10, 1),
('MR003', '小会议室B', '2楼', 10, 1);

-- 插入默认文档分类
INSERT INTO oa_document_category (name, parent_id, path, level, sort_order, status, creator_id) VALUES
('公司制度', 0, '0', 1, 1, 1, 1),
('技术文档', 0, '0', 1, 2, 1, 1),
('培训资料', 0, '0', 1, 3, 1, 1);
