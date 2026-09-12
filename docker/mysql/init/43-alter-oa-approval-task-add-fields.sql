-- =====================================================
-- OA审批任务表新增字段（基于权限部门的多节点会签审批流程）
-- 为 oa_approval_task 同步 ApprovalTaskEntity 新增的3个字段：
--   assignee_role     审批节点要求的角色编码（权限校验用）
--   node_index        节点序号（审批历史按节点排序展示）
--   assignee_dept_id  审批人所属部门ID（审计/统计用）
-- 并新增联合索引支持会签流转判断查询
-- 兼容低版本MySQL（不使用 ADD COLUMN IF NOT EXISTS 语法）
-- =====================================================

USE oa_db;

-- 字段新增（幂等：通过 information_schema 检查列是否存在，避免重复执行报错）
DROP PROCEDURE IF EXISTS add_oa_task_columns;
DELIMITER $$
CREATE PROCEDURE add_oa_task_columns()
BEGIN
    -- assignee_role
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'oa_approval_task' AND column_name = 'assignee_role'
    ) THEN
        ALTER TABLE oa_approval_task
            ADD COLUMN assignee_role VARCHAR(64) NULL COMMENT '审批节点要求角色编码' AFTER node_name;
    END IF;

    -- node_index
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'oa_approval_task' AND column_name = 'node_index'
    ) THEN
        ALTER TABLE oa_approval_task
            ADD COLUMN node_index INT NULL COMMENT '节点序号' AFTER assignee_role;
    END IF;

    -- assignee_dept_id
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'oa_approval_task' AND column_name = 'assignee_dept_id'
    ) THEN
        ALTER TABLE oa_approval_task
            ADD COLUMN assignee_dept_id BIGINT NULL COMMENT '审批人部门ID' AFTER assignee_name;
    END IF;
END$$
DELIMITER ;

CALL add_oa_task_columns();
DROP PROCEDURE IF EXISTS add_oa_task_columns;

-- 联合索引（实例+节点+状态），用于会签流转时统计同节点未完成任务数
DROP PROCEDURE IF EXISTS add_task_instance_node_status_index;
DELIMITER $$
CREATE PROCEDURE add_task_instance_node_status_index()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'oa_approval_task'
          AND index_name = 'idx_task_instance_node_status'
    ) THEN
        ALTER TABLE oa_approval_task
            ADD INDEX idx_task_instance_node_status (instance_id, node_id, status);
    END IF;
END$$
DELIMITER ;

CALL add_task_instance_node_status_index();
DROP PROCEDURE IF EXISTS add_task_instance_node_status_index;

-- 验证
SHOW COLUMNS FROM oa_approval_task LIKE 'assignee_role';
SHOW COLUMNS FROM oa_approval_task LIKE 'node_index';
SHOW COLUMNS FROM oa_approval_task LIKE 'assignee_dept_id';
