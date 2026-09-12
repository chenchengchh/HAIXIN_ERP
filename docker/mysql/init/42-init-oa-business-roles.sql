-- =====================================================
-- OA业务角色初始化脚本（基于权限部门的多节点会签审批流程）
-- 预置各业务模块审批流程节点所需的11个业务角色，
-- 并将 admin 关联所有业务角色（兜底，保证端到端测试可一键通过所有节点）
-- 幂等：角色已存在则跳过，admin 已关联则跳过
-- =====================================================

USE oa_db;

-- ========== 1. 预置业务角色 ==========
-- 对应 41-init-oa-approval-templates.sql 中各流程节点 assigneeRole
INSERT INTO oa_role (role_code, role_name, status, created_time, updated_time)
SELECT t.role_code, t.role_name, t.status, NOW(), NOW()
FROM (
    SELECT 'sales_manager'      AS role_code, '销售经理'   AS role_name, 'ACTIVE' AS status
    UNION ALL SELECT 'finance_manager',     '财务经理',   'ACTIVE'
    UNION ALL SELECT 'gm',                 '总经理',     'ACTIVE'
    UNION ALL SELECT 'purchase_manager',   '采购经理',   'ACTIVE'
    UNION ALL SELECT 'production_manager', '生产经理',   'ACTIVE'
    UNION ALL SELECT 'warehouse_manager',  '仓库主管',   'ACTIVE'
    UNION ALL SELECT 'logistics_manager',  '物流经理',   'ACTIVE'
    UNION ALL SELECT 'quality_manager',    '质量经理',   'ACTIVE'
    UNION ALL SELECT 'legal',              '法务',       'ACTIVE'
    UNION ALL SELECT 'service_manager',    '客服主管',   'ACTIVE'
    UNION ALL SELECT 'scm_manager',        '供应链经理', 'ACTIVE'
) t
WHERE NOT EXISTS (SELECT 1 FROM oa_role r WHERE r.role_code = t.role_code);

-- ========== 2. admin 关联所有业务角色（兜底） ==========
-- admin 具备所有业务角色，端到端测试时可用 admin 一键通过所有审批节点
INSERT INTO oa_user_role (user_id, role_id, created_time)
SELECT u.id, r.id, NOW()
FROM oa_user_account u, oa_role r
WHERE u.username = 'admin'
  AND r.role_code IN (
      'sales_manager', 'finance_manager', 'gm', 'purchase_manager', 'production_manager',
      'warehouse_manager', 'logistics_manager', 'quality_manager', 'legal',
      'service_manager', 'scm_manager'
  )
  AND NOT EXISTS (
      SELECT 1 FROM oa_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- ========== 验证 ==========
SELECT role_code, role_name, status FROM oa_role WHERE role_code IN (
    'sales_manager', 'finance_manager', 'gm', 'purchase_manager', 'production_manager',
    'warehouse_manager', 'logistics_manager', 'quality_manager', 'legal',
    'service_manager', 'scm_manager'
) ORDER BY role_code;

SELECT u.username, r.role_code, r.role_name
FROM oa_user_account u
JOIN oa_user_role ur ON ur.user_id = u.id
JOIN oa_role r ON r.id = ur.role_id
WHERE u.username = 'admin'
ORDER BY r.role_code;
