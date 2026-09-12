-- OA模块数据初始化脚本
-- 版本：V10
-- 说明：OA 侧审批/会议等业务数据初始化；账号域数据由 oa_user_account/role/permission 自身维护
-- 注意：脚本中包含TRUNCATE语句

USE oa_db;

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 1. 部门数据 (已统一到 hr_department)
-- ==========================================
-- 不在 OA 脚本中初始化部门，应由 HR 脚本初始化

-- ==========================================
-- 2. 初始化审批流程数据
-- ==========================================
TRUNCATE TABLE oa_approval_process;
INSERT INTO oa_approval_process (
    id, name, code, description, status, 
    process_type, 
    process_definition, 
    form_config, 
    creator_id, updater_id,
    create_time, update_time
) VALUES
(1, '采购审批流程', 'PURCHASE_APPROVAL', '用于采购申请的审批流程', 1, 
 'purchase',
 '{"nodes":[{"id":"start","name":"开始","type":"start"},{"id":"dept_manager","name":"部门经理审批","type":"approve"},{"id":"finance_manager","name":"财务经理审批","type":"approve"},{"id":"end","name":"结束","type":"end"}],"edges":[{"from":"start","to":"dept_manager"},{"from":"dept_manager","to":"finance_manager"},{"from":"finance_manager","to":"end"}]}', 
 '{"fields":[{"name":"purchaseType","label":"采购类型","type":"select","options":["原材料","包材","设备","其他"]},{"name":"amount","label":"采购金额","type":"number"},{"name":"reason","label":"采购理由","type":"textarea"},{"name":"attachments","label":"附件","type":"file"}]}',
 1, 1, NOW(), NOW()
),
(2, '请假审批流程', 'LEAVE_APPROVAL', '用于员工请假的审批流程', 1, 
 'leave',
 '{"nodes":[{"id":"start","name":"开始","type":"start"},{"id":"dept_manager","name":"部门经理审批","type":"approve"},{"id":"hr_approval","name":"HR审批","type":"approve"},{"id":"end","name":"结束","type":"end"}],"edges":[{"from":"start","to":"dept_manager"},{"from":"dept_manager","to":"hr_approval"},{"from":"hr_approval","to":"end"}]}',
 '{"fields":[{"name":"leaveType","label":"请假类型","type":"select","options":["年假","病假","事假","婚假","产假"]},{"name":"startDate","label":"开始日期","type":"date"},{"name":"endDate","label":"结束日期","type":"date"},{"name":"reason","label":"请假理由","type":"textarea"}]}',
 1, 1, NOW(), NOW()
),
(3, '报销审批流程', 'EXPENSE_APPROVAL', '用于员工报销的审批流程', 1, 
 'expense',
 '{"nodes":[{"id":"start","name":"开始","type":"start"},{"id":"dept_manager","name":"部门经理审批","type":"approve"},{"id":"finance_approval","name":"财务审批","type":"approve"},{"id":"end","name":"结束","type":"end"}],"edges":[{"from":"start","to":"dept_manager"},{"from":"dept_manager","to":"finance_approval"},{"from":"finance_approval","to":"end"}]}',
 '{"fields":[{"name":"expenseType","label":"报销类型","type":"select","options":["差旅费","办公费","业务招待费","其他"]},{"name":"amount","label":"报销金额","type":"number"},{"name":"expenseDate","label":"支出日期","type":"date"},{"name":"reason","label":"报销理由","type":"textarea"},{"name":"attachments","label":"发票附件","type":"file"}]}',
 1, 1, NOW(), NOW()
);

-- ==========================================
-- 4. 初始化会议室数据
-- ==========================================
TRUNCATE TABLE oa_meeting_room;
INSERT INTO oa_meeting_room (
    id, room_code, room_name, 
    location, capacity, equipment, status, description, 
    creator_id, created_at, updated_at
) VALUES
(1, 'RM001', '牡丹会议室', '1楼', 20, '投影仪、白板、音响、视频会议系统', 1, '大型会议室，适合公司级会议', 1, NOW(), NOW()),
(2, 'RM002', '玫瑰会议室', '1楼', 12, '投影仪、白板、音响', 1, '中型会议室，适合部门会议', 1, NOW(), NOW()),
(3, 'RM003', '茉莉会议室', '2楼', 8, '投影仪、白板', 1, '小型会议室，适合小组讨论', 1, NOW(), NOW()),
(4, 'RM004', '百合会议室', '2楼', 6, '白板', 1, '小型会议室，适合一对一沟通', 1, NOW(), NOW()),
(5, 'RM005', '研发专用会议室', '3楼', 15, '投影仪、白板、音响、实验设备', 1, '研发部门专用会议室', 4, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;

-- 数据初始化完成
SELECT 'OA模块数据统一初始化完成-V10' AS result;
