-- HR系统化妆品行业初始化数据

-- 设置时间格式
SET time_zone = '+8:00';

-- 禁用外键检查，避免删除和插入时的外键约束问题
SET FOREIGN_KEY_CHECKS = 0;

-- 清空现有数据（使用TRUNCATE重置自增ID，保证硬编码外键引用正确）
TRUNCATE TABLE hr_attendance_exception;
TRUNCATE TABLE hr_leave_request;
TRUNCATE TABLE hr_interview;
TRUNCATE TABLE hr_resume;
TRUNCATE TABLE hr_recruitment_demand;
TRUNCATE TABLE hr_resignation_request;
TRUNCATE TABLE hr_salary_adjustment;
TRUNCATE TABLE hr_transfer_record;
TRUNCATE TABLE hr_performance_appraisal;
TRUNCATE TABLE hr_performance_objective;
TRUNCATE TABLE hr_attendance_record;
TRUNCATE TABLE hr_payroll_record;
TRUNCATE TABLE hr_salary_structure;
TRUNCATE TABLE hr_employee;
TRUNCATE TABLE hr_position;
TRUNCATE TABLE hr_department;
TRUNCATE TABLE hr_training_plan;
TRUNCATE TABLE hr_training_participant;
TRUNCATE TABLE hr_social_security_record;
TRUNCATE TABLE hr_benefit_config;
TRUNCATE TABLE hr_benefit_record;
TRUNCATE TABLE hr_attendance_rule;
TRUNCATE TABLE hr_performance_bonus;

-- 1. 部门表初始化数据
INSERT INTO hr_department (name, department_code, parent_id, description, status, level, created_time, created_by) VALUES
-- 总部（根部门）
('总部', 'HQ', NULL, '公司总部', 'ACTIVE', 1, NOW(), 'system'),
-- 研发中心
('研发中心', 'R&D', 1, '负责产品研发、配方设计和技术创新', 'ACTIVE', 2, NOW(), 'system'),
('研发一部', 'R&D-001', 2, '护肤品研发', 'ACTIVE', 3, NOW(), 'system'),
('研发二部', 'R&D-002', 2, '彩妆研发', 'ACTIVE', 3, NOW(), 'system'),
('研发三部', 'R&D-003', 2, '香水研发', 'ACTIVE', 3, NOW(), 'system'),
('产品管理部', 'R&D-PM', 2, '产品生命周期管理', 'ACTIVE', 3, NOW(), 'system'),
-- 生产中心
('生产中心', 'PROD', 1, '负责产品生产和质量控制', 'ACTIVE', 2, NOW(), 'system'),
('生产一部', 'PROD-001', 7, '护肤品生产', 'ACTIVE', 3, NOW(), 'system'),
('生产二部', 'PROD-002', 7, '彩妆生产', 'ACTIVE', 3, NOW(), 'system'),
('质量控制部', 'PROD-QC', 7, '产品质量检测和控制', 'ACTIVE', 3, NOW(), 'system'),
('供应链管理部', 'PROD-SCM', 7, '原材料采购和供应链管理', 'ACTIVE', 3, NOW(), 'system'),
-- 营销中心
('营销中心', 'MARKET', 1, '负责品牌推广和市场营销', 'ACTIVE', 2, NOW(), 'system'),
('品牌管理部', 'MARKET-BRAND', 12, '品牌策略和形象管理', 'ACTIVE', 3, NOW(), 'system'),
('市场策划部', 'MARKET-PLAN', 12, '市场活动策划和执行', 'ACTIVE', 3, NOW(), 'system'),
('数字营销部', 'MARKET-DIGITAL', 12, '线上营销和社交媒体管理', 'ACTIVE', 3, NOW(), 'system'),
('公关部', 'MARKET-PR', 12, '公共关系和媒体合作', 'ACTIVE', 3, NOW(), 'system'),
-- 销售中心
('销售中心', 'SALES', 1, '负责产品销售和渠道管理', 'ACTIVE', 2, NOW(), 'system'),
('线下销售部', 'SALES-OFFLINE', 17, '线下专柜和经销商管理', 'ACTIVE', 3, NOW(), 'system'),
('线上销售部', 'SALES-ONLINE', 17, '电商平台和自营商城管理', 'ACTIVE', 3, NOW(), 'system'),
('大客户部', 'SALES-KA', 17, '大型客户和合作伙伴管理', 'ACTIVE', 3, NOW(), 'system'),
('销售支持部', 'SALES-SUPPORT', 17, '销售数据和行政支持', 'ACTIVE', 3, NOW(), 'system'),
-- 职能部门
('人力资源部', 'HR', 1, '负责人员招聘、培训和绩效管理', 'ACTIVE', 2, NOW(), 'system'),
('财务部', 'FIN', 1, '负责财务管理和成本控制', 'ACTIVE', 2, NOW(), 'system'),
('行政部', 'ADMIN', 1, '负责行政事务和后勤保障', 'ACTIVE', 2, NOW(), 'system'),
('IT部', 'IT', 1, '负责信息系统和技术支持', 'ACTIVE', 2, NOW(), 'system');

-- 2. 职位表初始化数据
INSERT INTO hr_position (name, position_code, level, description, status, created_time, created_by) VALUES
-- 管理层
('总经理', 'GM-001', 'EXECUTIVE', '公司最高管理者', 'ACTIVE', NOW(), 'system'),
('研发总监', 'RD-DIR', 'SENIOR', '研发中心负责人', 'ACTIVE', NOW(), 'system'),
('生产总监', 'PROD-DIR', 'SENIOR', '生产中心负责人', 'ACTIVE', NOW(), 'system'),
('营销总监', 'MARKET-DIR', 'SENIOR', '营销中心负责人', 'ACTIVE', NOW(), 'system'),
('销售总监', 'SALES-DIR', 'SENIOR', '销售中心负责人', 'ACTIVE', NOW(), 'system'),
('人力资源总监', 'HR-DIR', 'SENIOR', '人力资源部负责人', 'ACTIVE', NOW(), 'system'),
('财务总监', 'FIN-DIR', 'SENIOR', '财务部负责人', 'ACTIVE', NOW(), 'system'),
('行政总监', 'ADMIN-DIR', 'SENIOR', '行政部负责人', 'ACTIVE', NOW(), 'system'),
('IT总监', 'IT-DIR', 'SENIOR', 'IT部负责人', 'ACTIVE', NOW(), 'system'),
-- 研发类职位
('研发经理', 'RD-MGR', 'MIDDLE', '研发部门经理', 'ACTIVE', NOW(), 'system'),
('配方师', 'RD-FORM', 'MIDDLE', '化妆品配方开发', 'ACTIVE', NOW(), 'system'),
('研发工程师', 'RD-ENG', 'JUNIOR', '研发助理工程师', 'ACTIVE', NOW(), 'system'),
('产品经理', 'RD-PM', 'MIDDLE', '产品生命周期管理', 'ACTIVE', NOW(), 'system'),
('产品专员', 'RD-PM-ASSIST', 'JUNIOR', '产品管理助理', 'ACTIVE', NOW(), 'system'),
-- 生产类职位
('生产经理', 'PROD-MGR', 'MIDDLE', '生产部门经理', 'ACTIVE', NOW(), 'system'),
('质量控制经理', 'PROD-QC-MGR', 'MIDDLE', '质量控制部门经理', 'ACTIVE', NOW(), 'system'),
('车间主任', 'PROD-SHOP-MGR', 'MIDDLE', '生产车间负责人', 'ACTIVE', NOW(), 'system'),
('质量检测员', 'PROD-QC-INSPECT', 'JUNIOR', '产品质量检测', 'ACTIVE', NOW(), 'system'),
('生产操作员', 'PROD-OP', 'JUNIOR', '生产设备操作', 'ACTIVE', NOW(), 'system'),
-- 营销类职位
('品牌经理', 'MARKET-BRAND-MGR', 'MIDDLE', '品牌管理', 'ACTIVE', NOW(), 'system'),
('市场专员', 'MARKET-EXEC', 'JUNIOR', '市场活动执行', 'ACTIVE', NOW(), 'system'),
('数字营销专员', 'MARKET-DIGITAL-EXEC', 'JUNIOR', '线上营销执行', 'ACTIVE', NOW(), 'system'),
('公关专员', 'MARKET-PR-EXEC', 'JUNIOR', '公关活动执行', 'ACTIVE', NOW(), 'system'),
-- 销售类职位
('销售经理', 'SALES-MGR', 'MIDDLE', '销售部门经理', 'ACTIVE', NOW(), 'system'),
('区域销售经理', 'SALES-REG-MGR', 'MIDDLE', '区域销售负责人', 'ACTIVE', NOW(), 'system'),
('客户经理', 'SALES-ACCOUNT-MGR', 'JUNIOR', '客户关系管理', 'ACTIVE', NOW(), 'system'),
('销售代表', 'SALES-REP', 'JUNIOR', '产品销售', 'ACTIVE', NOW(), 'system'),
-- 职能类职位
('HR专员', 'HR-EXEC', 'JUNIOR', '人力资源事务执行', 'ACTIVE', NOW(), 'system'),
('财务专员', 'FIN-EXEC', 'JUNIOR', '财务事务执行', 'ACTIVE', NOW(), 'system'),
('行政专员', 'ADMIN-EXEC', 'JUNIOR', '行政事务执行', 'ACTIVE', NOW(), 'system'),
('IT专员', 'IT-EXEC', 'JUNIOR', 'IT技术支持', 'ACTIVE', NOW(), 'system');

-- 3. 员工表初始化数据
INSERT INTO hr_employee (employee_code, name, gender, id_card, phone, email, department_id, position_id, hire_date, status, created_time, created_by) VALUES
-- 管理层
('EMP-00001', '张三', '男', '110101197505151234', '13800138001', 'zhangsan@example.com', 1, 1, '2010-01-01', 'ACTIVE', NOW(), 'system'),
('EMP-00002', '李四', '女', '110101198008202345', '13800138002', 'lisi@example.com', 2, 2, '2012-03-01', 'ACTIVE', NOW(), 'system'),
('EMP-00003', '王五', '男', '110101197803103456', '13800138003', 'wangwu@example.com', 7, 3, '2011-07-01', 'ACTIVE', NOW(), 'system'),
('EMP-00004', '赵六', '女', '110101198212054567', '13800138004', 'zhaoliu@example.com', 12, 4, '2013-05-01', 'ACTIVE', NOW(), 'system'),
('EMP-00005', '孙七', '男', '110101198509185678', '13800138005', 'sunqi@example.com', 17, 5, '2014-02-01', 'ACTIVE', NOW(), 'system'),
('EMP-00006', '周八', '女', '110101198306256789', '13800138006', 'zhouba@example.com', 22, 6, '2015-08-01', 'ACTIVE', NOW(), 'system'),
('EMP-00007', '吴九', '男', '110101197904307890', '13800138007', 'wujiu@example.com', 23, 7, '2010-11-01', 'ACTIVE', NOW(), 'system'),
('EMP-00008', '郑十', '女', '110101198401128901', '13800138008', 'zhengshi@example.com', 24, 8, '2016-04-01', 'ACTIVE', NOW(), 'system'),
('EMP-00009', '钱一', '男', '110101198610089012', '13800138009', 'qianyi@example.com', 25, 9, '2017-01-01', 'ACTIVE', NOW(), 'system'),
-- 研发类员工
('EMP-00010', '陈二', '女', '110101198802201234', '13800138010', 'chener@example.com', 3, 10, '2018-03-01', 'ACTIVE', NOW(), 'system'),
('EMP-00011', '林三', '男', '110101199005152345', '13800138011', 'linsan@example.com', 3, 11, '2019-07-01', 'ACTIVE', NOW(), 'system'),
('EMP-00012', '黄四', '女', '110101199208303456', '13800138012', 'huangs@example.com', 4, 12, '2020-09-01', 'ACTIVE', NOW(), 'system'),
('EMP-00013', '梁五', '男', '110101199104254567', '13800138013', 'liangwu@example.com', 5, 13, '2021-01-01', 'ACTIVE', NOW(), 'system'),
('EMP-00014', '杨六', '女', '110101199307105678', '13800138014', 'yangliu@example.com', 6, 14, '2022-03-01', 'ACTIVE', NOW(), 'system'),
-- 生产类员工
('EMP-00015', '刘七', '男', '110101198701286789', '13800138015', 'liuqi@example.com', 8, 15, '2015-05-01', 'ACTIVE', NOW(), 'system'),
('EMP-00016', '马八', '女', '110101198906127890', '13800138016', 'maba@example.com', 10, 16, '2017-08-01', 'ACTIVE', NOW(), 'system'),
('EMP-00017', '胡九', '男', '110101199009058901', '13800138017', 'hujiu@example.com', 9, 17, '2018-10-01', 'ACTIVE', NOW(), 'system'),
('EMP-00018', '郭十', '女', '110101199211309012', '13800138018', 'guoshi@example.com', 10, 18, '2019-12-01', 'ACTIVE', NOW(), 'system'),
('EMP-00019', '何一', '男', '110101199303201234', '13800138019', 'heyi@example.com', 8, 19, '2020-04-01', 'ACTIVE', NOW(), 'system'),
-- 营销类员工
('EMP-00020', '罗二', '女', '110101199107152345', '13800138020', 'luoer@example.com', 13, 20, '2016-06-01', 'ACTIVE', NOW(), 'system'),
('EMP-00021', '高三', '男', '110101199209283456', '13800138021', 'gaosan@example.com', 14, 21, '2018-01-01', 'ACTIVE', NOW(), 'system'),
('EMP-00022', '唐四', '女', '110101199312104567', '13800138022', 'tangsi@example.com', 15, 22, '2019-03-01', 'ACTIVE', NOW(), 'system'),
('EMP-00023', '冯五', '男', '110101199405055678', '13800138023', 'fengwu@example.com', 16, 23, '2020-07-01', 'ACTIVE', NOW(), 'system'),
-- 销售类员工
('EMP-00024', '于六', '女', '110101198903186789', '13800138024', 'yuliu@example.com', 18, 24, '2014-09-01', 'ACTIVE', NOW(), 'system'),
('EMP-00025', '董七', '男', '110101199008227890', '13800138025', 'dongqi@example.com', 19, 25, '2016-02-01', 'ACTIVE', NOW(), 'system'),
('EMP-00026', '萧八', '女', '110101199111158901', '13800138026', 'xiaoba@example.com', 20, 26, '2017-05-01', 'ACTIVE', NOW(), 'system'),
('EMP-00027', '程九', '男', '110101199204089012', '13800138027', 'chengjiu@example.com', 18, 27, '2018-09-01', 'ACTIVE', NOW(), 'system'),
-- 职能类员工
('EMP-00028', '袁十', '女', '110101199306301234', '13800138028', 'yuanshi@example.com', 22, 28, '2019-01-01', 'ACTIVE', NOW(), 'system'),
('EMP-00029', '许一', '男', '110101199409122345', '13800138029', 'xuyi@example.com', 23, 29, '2020-03-01', 'ACTIVE', NOW(), 'system'),
('EMP-00030', '沈二', '女', '110101199512253456', '13800138030', 'shener@example.com', 24, 30, '2021-06-01', 'ACTIVE', NOW(), 'system'),
('EMP-00031', '韩三', '男', '110101199603184567', '13800138031', 'hansan@example.com', 25, 31, '2022-09-01', 'ACTIVE', NOW(), 'system'),
-- 离职员工
('EMP-00032', '周离职', '男', '110101198504201234', '13800138032', 'zhouzhili@example.com', 18, 27, '2020-01-01', 'INACTIVE', NOW(), 'system'),
('EMP-00033', '吴离职', '女', '110101199008152345', '13800138033', 'wuzhili@example.com', 19, 26, '2021-03-01', 'INACTIVE', NOW(), 'system');

-- 4. 更新部门负责人
UPDATE hr_department SET manager_id = 1 WHERE id = 1; -- 总部负责人：张三
UPDATE hr_department SET manager_id = 2 WHERE id = 2; -- 研发中心负责人：李四
UPDATE hr_department SET manager_id = 3 WHERE id = 7; -- 生产中心负责人：王五
UPDATE hr_department SET manager_id = 4 WHERE id = 12; -- 营销中心负责人：赵六
UPDATE hr_department SET manager_id = 5 WHERE id = 17; -- 销售中心负责人：孙七
UPDATE hr_department SET manager_id = 6 WHERE id = 22; -- 人力资源部负责人：周八
UPDATE hr_department SET manager_id = 7 WHERE id = 23; -- 财务部负责人：吴九
UPDATE hr_department SET manager_id = 8 WHERE id = 24; -- 行政部负责人：郑十
UPDATE hr_department SET manager_id = 9 WHERE id = 25; -- IT部负责人：钱一

-- 5. 薪酬结构表初始化数据
INSERT INTO hr_salary_structure (name, basic_salary, bonus, allowance, deduction, effective_date, status, created_time, created_by) VALUES
('总经理薪酬结构', 50000.00, 100000.00, 5000.00, 2000.00, '2023-01-01', 'ACTIVE', NOW(), 'system'),
('总监薪酬结构', 30000.00, 60000.00, 3000.00, 1500.00, '2023-01-01', 'ACTIVE', NOW(), 'system'),
('部门经理薪酬结构', 20000.00, 40000.00, 2000.00, 1000.00, '2023-01-01', 'ACTIVE', NOW(), 'system'),
('研发工程师薪酬结构', 15000.00, 30000.00, 1500.00, 800.00, '2023-01-01', 'ACTIVE', NOW(), 'system'),
('生产经理薪酬结构', 18000.00, 35000.00, 1800.00, 900.00, '2023-01-01', 'ACTIVE', NOW(), 'system'),
('营销专员薪酬结构', 12000.00, 25000.00, 1200.00, 600.00, '2023-01-01', 'ACTIVE', NOW(), 'system'),
('销售代表薪酬结构', 10000.00, 50000.00, 1000.00, 500.00, '2023-01-01', 'ACTIVE', NOW(), 'system'),
('职能专员薪酬结构', 10000.00, 20000.00, 800.00, 400.00, '2023-01-01', 'ACTIVE', NOW(), 'system');

-- 6. 薪资记录表初始化数据
INSERT INTO hr_payroll_record (employee_id, month, basic_salary, performance_salary, bonus, allowance, deduction, actual_salary, status, created_time, created_by) VALUES
-- 2023年12月薪资记录
(1, '2023-12', 50000.00, 20000.00, 30000.00, 5000.00, 2000.00, 103000.00, 1, NOW(), 'system'),
(2, '2023-12', 30000.00, 15000.00, 20000.00, 3000.00, 1500.00, 66500.00, 1, NOW(), 'system'),
(3, '2023-12', 30000.00, 14000.00, 18000.00, 3000.00, 1500.00, 63500.00, 1, NOW(), 'system'),
(4, '2023-12', 30000.00, 16000.00, 22000.00, 3000.00, 1500.00, 69500.00, 1, NOW(), 'system'),
(5, '2023-12', 30000.00, 18000.00, 25000.00, 3000.00, 1500.00, 74500.00, 1, NOW(), 'system'),
(6, '2023-12', 30000.00, 13000.00, 17000.00, 3000.00, 1500.00, 61500.00, 1, NOW(), 'system'),
(7, '2023-12', 30000.00, 15000.00, 19000.00, 3000.00, 1500.00, 65500.00, 1, NOW(), 'system'),
(8, '2023-12', 30000.00, 12000.00, 16000.00, 3000.00, 1500.00, 59500.00, 1, NOW(), 'system'),
(9, '2023-12', 30000.00, 14000.00, 18000.00, 3000.00, 1500.00, 63500.00, 1, NOW(), 'system'),
(10, '2023-12', 20000.00, 10000.00, 15000.00, 2000.00, 1000.00, 46000.00, 1, NOW(), 'system'),
(11, '2023-12', 20000.00, 8000.00, 12000.00, 1500.00, 800.00, 39700.00, 1, NOW(), 'system');

-- 7. 考勤记录表初始化数据
INSERT INTO hr_attendance_record (employee_id, attendance_date, check_in_time, check_out_time, status, created_time, created_by) VALUES
-- 员工1的考勤记录（2023年12月1日至2023年12月5日）
(1, '2023-12-01', '2023-12-01 08:30:00', '2023-12-01 18:00:00', 'NORMAL', NOW(), 'system'),
(1, '2023-12-02', '2023-12-02 08:25:00', '2023-12-02 18:10:00', 'NORMAL', NOW(), 'system'),
(1, '2023-12-03', '2023-12-03 08:35:00', '2023-12-03 17:55:00', 'NORMAL', NOW(), 'system'),
(1, '2023-12-04', '2023-12-04 08:40:00', '2023-12-04 18:20:00', 'LATE', NOW(), 'system'),
(1, '2023-12-05', '2023-12-05 08:20:00', '2023-12-05 17:30:00', 'NORMAL', NOW(), 'system'),
-- 员工2的考勤记录（2023年12月1日至2023年12月5日）
(2, '2023-12-01', '2023-12-01 08:28:00', '2023-12-01 18:05:00', 'NORMAL', NOW(), 'system'),
(2, '2023-12-02', '2023-12-02 08:32:00', '2023-12-02 18:15:00', 'NORMAL', NOW(), 'system'),
(2, '2023-12-03', '2023-12-03 08:29:00', '2023-12-03 17:58:00', 'NORMAL', NOW(), 'system'),
(2, '2023-12-04', '2023-12-04 08:31:00', '2023-12-04 18:02:00', 'NORMAL', NOW(), 'system'),
(2, '2023-12-05', '2023-12-05 08:27:00', '2023-12-05 17:35:00', 'NORMAL', NOW(), 'system'),
-- 员工3的考勤记录（2023年12月1日至2023年12月5日）
(3, '2023-12-01', '2023-12-01 08:33:00', '2023-12-01 18:00:00', 'LATE', NOW(), 'system'),
(3, '2023-12-02', '2023-12-02 08:26:00', '2023-12-02 18:10:00', 'NORMAL', NOW(), 'system'),
(3, '2023-12-03', '2023-12-03 08:24:00', '2023-12-03 17:55:00', 'NORMAL', NOW(), 'system'),
(3, '2023-12-04', '2023-12-04 08:30:00', '2023-12-04 18:05:00', 'NORMAL', NOW(), 'system'),
(3, '2023-12-05', '2023-12-05 08:28:00', '2023-12-05 17:40:00', 'NORMAL', NOW(), 'system');

-- 8. 绩效目标表初始化数据
INSERT INTO hr_performance_objective (employee_id, objective_content, target_value, weight, start_date, end_date, status, created_time, created_by) VALUES
-- 总经理绩效目标
(1, '公司年度销售额', '10亿元', 0.3, '2023-01-01', '2023-12-31', 'COMPLETED', NOW(), 'system'),
(1, '新产品研发数量', '10个', 0.2, '2023-01-01', '2023-12-31', 'COMPLETED', NOW(), 'system'),
(1, '市场份额增长', '10%', 0.2, '2023-01-01', '2023-12-31', 'COMPLETED', NOW(), 'system'),
(1, '员工满意度', '90%', 0.15, '2023-01-01', '2023-12-31', 'COMPLETED', NOW(), 'system'),
(1, '成本控制', '降低5%', 0.15, '2023-01-01', '2023-12-31', 'COMPLETED', NOW(), 'system'),
-- 研发总监绩效目标
(2, '新产品研发成功率', '80%', 0.3, '2023-01-01', '2023-12-31', 'COMPLETED', NOW(), 'system'),
(2, '研发周期缩短', '15%', 0.2, '2023-01-01', '2023-12-31', 'COMPLETED', NOW(), 'system'),
(2, '专利申请数量', '5个', 0.2, '2023-01-01', '2023-12-31', 'COMPLETED', NOW(), 'system'),
(2, '研发成本控制', '预算内', 0.15, '2023-01-01', '2023-12-31', 'COMPLETED', NOW(), 'system'),
(2, '研发团队建设', '新增10人', 0.15, '2023-01-01', '2023-12-31', 'COMPLETED', NOW(), 'system');

-- 9. 绩效评估表初始化数据
INSERT INTO hr_performance_appraisal (employee_id, appraisal_period, objective_score, competency_score, total_score, appraisal_status, appraiser_id, created_time, created_by) VALUES
-- 2023年度绩效评估
(1, '2023年度', 95.0, 90.0, 93.5, 'COMPLETED', 1, NOW(), 'system'),
(2, '2023年度', 92.0, 88.0, 90.8, 'COMPLETED', 1, NOW(), 'system'),
(3, '2023年度', 88.0, 90.0, 89.0, 'COMPLETED', 1, NOW(), 'system'),
(4, '2023年度', 90.0, 89.0, 89.7, 'COMPLETED', 1, NOW(), 'system'),
(5, '2023年度', 93.0, 87.0, 90.6, 'COMPLETED', 1, NOW(), 'system'),
(6, '2023年度', 87.0, 88.0, 87.3, 'COMPLETED', 1, NOW(), 'system'),
(7, '2023年度', 89.0, 91.0, 89.6, 'COMPLETED', 1, NOW(), 'system'),
(8, '2023年度', 86.0, 89.0, 87.1, 'COMPLETED', 1, NOW(), 'system'),
(9, '2023年度', 88.0, 90.0, 88.8, 'COMPLETED', 1, NOW(), 'system'),
(10, '2023年度', 91.0, 88.0, 89.9, 'COMPLETED', 2, NOW(), 'system');

-- 10. 职位调整表初始化数据
INSERT INTO hr_transfer_record (employee_id, old_department_id, new_department_id, old_position_id, new_position_id, reason, transfer_date, status, created_time, created_by) VALUES
-- 员工转岗记录
(12, 3, 4, 13, 12, '产品结构调整，从护肤品研发转彩妆研发', '2023-06-01 00:00:00', 1, NOW(), 'system'),
(15, 8, 9, 15, 15, '生产部门调整，从护肤品生产转彩妆生产', '2023-09-01 00:00:00', 1, NOW(), 'system'),
(24, 18, 19, 24, 25, '销售区域调整，从线下销售转线上销售', '2023-10-01 00:00:00', 1, NOW(), 'system');

-- 11. 调薪记录表初始化数据
INSERT INTO hr_salary_adjustment (employee_id, old_salary, new_salary, reason, adjustment_date, status, created_time, created_by) VALUES
-- 员工调薪记录
(1, 45000.00, 50000.00, '年度调薪，基于2022年度绩效', '2023-01-01 00:00:00', 1, NOW(), 'system'),
(2, 28000.00, 30000.00, '年度调薪，基于2022年度绩效', '2023-01-01 00:00:00', 1, NOW(), 'system'),
(3, 28000.00, 30000.00, '年度调薪，基于2022年度绩效', '2023-01-01 00:00:00', 1, NOW(), 'system'),
(4, 28000.00, 30000.00, '年度调薪，基于2022年度绩效', '2023-01-01 00:00:00', 1, NOW(), 'system'),
(5, 28000.00, 30000.00, '年度调薪，基于2022年度绩效', '2023-01-01 00:00:00', 1, NOW(), 'system'),
(10, 18000.00, 20000.00, '晋升为研发经理', '2023-07-01 00:00:00', 1, NOW(), 'system'),
(15, 16000.00, 18000.00, '晋升为生产经理', '2023-08-01 00:00:00', 1, NOW(), 'system'),
(20, 10000.00, 12000.00, '晋升为品牌经理', '2023-09-01 00:00:00', 1, NOW(), 'system');

-- 12. 离职申请表初始化数据
INSERT INTO hr_resignation_request (employee_id, resignation_type, reason, apply_date, expected_resign_date, actual_resign_date, status, created_time, created_by) VALUES
-- 员工离职记录
(32, 0, '个人职业发展规划', '2023-11-01 00:00:00', '2023-12-01 00:00:00', '2023-12-01 00:00:00', 3, NOW(), 'system'),
(33, 1, '公司业务调整，部门裁撤', '2023-10-15 00:00:00', '2023-11-15 00:00:00', '2023-11-15 00:00:00', 3, NOW(), 'system');

-- 13. 招聘需求表初始化数据
INSERT INTO hr_recruitment_demand (position_name, department_id, demand_number, required_skills, expected_salary, status, created_time, created_by) VALUES
-- 研发中心招聘需求
('研发工程师（护肤品）', 3, 2, '化妆品配方开发，熟悉护肤品原料', '15-20K', 'ACTIVE', NOW(), 'system'),
('研发工程师（彩妆）', 4, 3, '化妆品配方开发，熟悉彩妆原料和工艺', '15-20K', 'ACTIVE', NOW(), 'system'),
('产品经理', 6, 1, '产品生命周期管理，市场调研', '20-25K', 'ACTIVE', NOW(), 'system'),
-- 生产中心招聘需求
('质量检测员', 10, 2, '化妆品质量检测，熟悉相关标准', '8-12K', 'ACTIVE', NOW(), 'system'),
('生产操作员', 8, 5, '化妆品生产设备操作', '6-8K', 'ACTIVE', NOW(), 'system'),
-- 营销中心招聘需求
('数字营销专员', 15, 2, '社交媒体运营，内容创作', '10-15K', 'ACTIVE', NOW(), 'system'),
('品牌策划师', 14, 1, '品牌活动策划，市场推广', '15-20K', 'ACTIVE', NOW(), 'system'),
-- 销售中心招聘需求
('电商运营专员', 19, 2, '电商平台运营，数据分析', '10-15K', 'ACTIVE', NOW(), 'system'),
('销售代表', 18, 3, '化妆品销售，客户开发', '8-15K', 'ACTIVE', NOW(), 'system');

-- 14. 简历表初始化数据
INSERT INTO hr_resume (candidate_name, gender, phone, email, position_id, resume_url, status, created_time, created_by) VALUES
-- 应聘简历
('张小明', '男', '13900139001', 'zhangxiaoming@example.com', 12, '/resumes/zhangxiaoming.pdf', 'SCREENING', NOW(), 'system'),
('李小红', '女', '13900139002', 'lixiaohong@example.com', 13, '/resumes/lixiaohong.pdf', 'SCREENING', NOW(), 'system'),
('王小强', '男', '13900139003', 'wangxiaoqiang@example.com', 14, '/resumes/wangxiaoqiang.pdf', 'INTERVIEW', NOW(), 'system'),
('赵小美', '女', '13900139004', 'zhaoxiaomei@example.com', 21, '/resumes/zhaoxiaomei.pdf', 'INTERVIEW', NOW(), 'system'),
('刘小华', '男', '13900139005', 'liuxiaohua@example.com', 26, '/resumes/liuxiaohua.pdf', 'OFFER', NOW(), 'system');

-- 15. 面试表初始化数据
INSERT INTO hr_interview (resume_id, interviewer_id, interview_time, interview_type, interview_result, created_time, created_by) VALUES
-- 面试记录
(1, 2, '2023-12-10 10:00:00', 'TECHNICAL', 'PASS', NOW(), 'system'),
(2, 2, '2023-12-11 14:00:00', 'TECHNICAL', 'PASS', NOW(), 'system'),
(3, 2, '2023-12-12 10:00:00', 'MANAGERIAL', 'PASS', NOW(), 'system'),
(4, 4, '2023-12-13 14:00:00', 'TECHNICAL', 'WAITING', NOW(), 'system'),
(5, 5, '2023-12-14 10:00:00', 'MANAGERIAL', 'OFFER', NOW(), 'system');

-- 16. 请假申请表初始化数据
INSERT INTO hr_leave_request (employee_id, leave_type, start_date, end_date, duration, reason, status, created_time, created_by) VALUES
-- 员工请假记录
(10, 'ANNUAL', '2023-12-10', '2023-12-12', 3.0, '年度休假', 1, NOW(), 'system'),
(15, 'SICK', '2023-12-05', '2023-12-06', 2.0, '感冒发烧', 1, NOW(), 'system'),
(20, 'PERSONAL', '2023-12-15', '2023-12-15', 1.0, '个人事务', 0, NOW(), 'system'),
(25, 'ANNUAL', '2023-12-20', '2023-12-22', 3.0, '年度休假', 0, NOW(), 'system');

-- 17. 考勤异常表初始化数据
INSERT INTO hr_attendance_exception (employee_id, exception_date, exception_type, description, status, created_time, created_by) VALUES
-- 考勤异常记录
(1, '2023-12-04', 'LATE', '迟到40分钟，交通拥堵', 1, NOW(), 'system'),
(3, '2023-12-01', 'LATE', '迟到33分钟，闹钟故障', 1, NOW(), 'system'),
(15, '2023-12-05', 'ABSENT', '旷工1天，未请假', 0, NOW(), 'system'),
(22, '2023-12-03', 'EARLY_LEAVE', '早退1小时，个人原因', 0, NOW(), 'system');

-- 18. 培训计划表初始化数据
INSERT INTO hr_training_plan (training_name, trainer, start_date, end_date, location, status, description, created_time, created_by) VALUES
('新员工入职培训', '周八', '2023-12-01', '2023-12-05', '总部培训室A', 2, '企业文化、规章制度、产品知识培训', NOW(), 'system'),
('化妆品配方师进阶培训', '李四', '2023-12-10', '2023-12-20', '研发中心实验室', 1, '护肤品配方设计进阶课程', NOW(), 'system'),
('安全生产培训', '王五', '2024-01-08', '2024-01-09', '生产中心会议室', 0, '生产车间安全操作规程培训', NOW(), 'system'),
('销售技巧提升培训', '孙七', '2023-11-15', '2023-11-16', '总部培训室B', 2, '大客户销售技巧与谈判策略', NOW(), 'system');

-- 19. 培训参与记录表初始化数据
INSERT INTO hr_training_participant (training_id, employee_id, attendance_status, completion_status, score, created_time, created_by) VALUES
(1, 19, 1, 1, 92.0, NOW(), 'system'),
(1, 23, 1, 1, 88.5, NOW(), 'system'),
(2, 11, 1, 1, 95.0, NOW(), 'system'),
(2, 12, 1, 0, NULL, NOW(), 'system'),
(3, 15, 0, 0, NULL, NOW(), 'system'),
(4, 24, 1, 1, 90.0, NOW(), 'system'),
(4, 26, 1, 1, 85.5, NOW(), 'system');

-- 20. 社保公积金记录表初始化数据
INSERT INTO hr_social_security_record (employee_id, insurance_month, base_amount, pension_company, pension_personal, medical_company, medical_personal, unemployment_company, unemployment_personal, housing_fund_company, housing_fund_personal, status, created_time, created_by) VALUES
(1, '2023-12', 50000.00, 8000.00, 4000.00, 4750.00, 1000.00, 250.00, 250.00, 6000.00, 6000.00, 1, NOW(), 'system'),
(2, '2023-12', 30000.00, 4800.00, 2400.00, 2850.00, 600.00, 150.00, 150.00, 3600.00, 3600.00, 1, NOW(), 'system'),
(3, '2023-12', 30000.00, 4800.00, 2400.00, 2850.00, 600.00, 150.00, 150.00, 3600.00, 3600.00, 1, NOW(), 'system'),
(10, '2023-12', 20000.00, 3200.00, 1600.00, 1900.00, 400.00, 100.00, 100.00, 2400.00, 2400.00, 1, NOW(), 'system'),
(11, '2023-12', 15000.00, 2400.00, 1200.00, 1425.00, 300.00, 75.00, 75.00, 1800.00, 1800.00, 0, NOW(), 'system');

-- 21. 福利配置表初始化数据
INSERT INTO hr_benefit_config (benefit_name, benefit_type, standard_amount, frequency, status, description, created_time, created_by) VALUES
('春节福利', 0, 1000.00, 2, 1, '春节期间发放的节日福利', NOW(), 'system'),
('中秋节福利', 0, 500.00, 2, 1, '中秋佳节礼品或礼金', NOW(), 'system'),
('生日福利', 1, 300.00, 2, 1, '员工生日当月发放', NOW(), 'system'),
('年度健康体检', 2, 800.00, 2, 1, '每年一次员工健康体检', NOW(), 'system'),
('交通补贴', 3, 500.00, 1, 1, '每月交通费用补贴', NOW(), 'system');

-- 22. 福利发放记录表初始化数据
INSERT INTO hr_benefit_record (benefit_id, employee_id, distribute_date, amount, status, created_time, created_by) VALUES
(1, 1, '2023-01-20', 1000.00, 1, NOW(), 'system'),
(1, 2, '2023-01-20', 1000.00, 1, NOW(), 'system'),
(1, 3, '2023-01-20', 1000.00, 1, NOW(), 'system'),
(3, 4, '2023-12-05', 300.00, 1, NOW(), 'system'),
(5, 10, '2023-12-01', 500.00, 1, NOW(), 'system'),
(5, 11, '2023-12-01', 500.00, 0, NOW(), 'system');

-- 23. 考勤规则表初始化数据
INSERT INTO hr_attendance_rule (rule_name, work_start_time, work_end_time, late_tolerance, early_leave_tolerance, applicable_departments, status, created_time, created_by) VALUES
('标准工作制', '09:00', '18:00', 10, 10, '研发中心,人力资源部,财务部,行政部,IT部', 1, NOW(), 'system'),
('弹性工作制', '09:30', '18:30', 30, 0, '营销中心,销售中心', 1, NOW(), 'system'),
('生产轮班制', '08:00', '16:00', 5, 5, '生产中心', 1, NOW(), 'system');

-- 24. 绩效奖金表初始化数据
INSERT INTO hr_performance_bonus (employee_id, appraisal_period, performance_score, performance_level, bonus_amount, status, evaluator_id, created_time, created_by) VALUES
(1, '2023年度', 93.5, 'A', 50000.00, 2, 1, NOW(), 'system'),
(2, '2023年度', 90.8, 'A', 35000.00, 2, 1, NOW(), 'system'),
(3, '2023年度', 89.0, 'B', 28000.00, 1, 1, NOW(), 'system'),
(4, '2023年度', 89.7, 'B', 29000.00, 1, 1, NOW(), 'system'),
(5, '2023年度', 90.6, 'A', 33000.00, 0, 1, NOW(), 'system'),
(10, '2023年度', 89.9, 'B', 15000.00, 0, 2, NOW(), 'system');

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;
