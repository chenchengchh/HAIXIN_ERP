-- PLM模块核心测试数据
-- 注意：时间字段手动+8小时，避免JDBC时区转换导致显示偏差

USE plm_db;

-- 清空现有数据（按外键依赖顺序）
DELETE FROM plm_gantt_link;
DELETE FROM plm_task;
DELETE FROM plm_resource_load;
DELETE FROM plm_bom;
DELETE FROM plm_document;
DELETE FROM plm_process_route;
DELETE FROM plm_process_file;
DELETE FROM plm_change_request;
DELETE FROM plm_trial_plan;
DELETE FROM plm_trial_report;
DELETE FROM plm_quality_issue;
DELETE FROM plm_project;
DELETE FROM plm_product;

-- 1. 产品数据
INSERT INTO plm_product (product_code, product_name, product_model, product_spec, product_type, product_category, unit, status, version, description, created_by, created_time, updated_time) VALUES
('PRD-MTR-001', '伺服电机驱动器', 'SD-750W', 'AC220V 750W 3000rpm', 'finished', '驱动设备', '台', 'RELEASED', 'V2.1', '高性能伺服电机驱动器，适用于自动化产线', '张工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRD-MTR-002', '步进电机', 'SM-42BYG', 'DC24V 1.8° 两相', 'semi', '电机', '台', 'RELEASED', 'V1.5', '两相混合式步进电机', '张工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRD-PLC-001', '可编程控制器', 'PLC-X200', '16DI/16DO 以太网', 'finished', '控制设备', '台', 'DESIGN', 'V1.0', '中型PLC控制器，支持Modbus TCP', '李工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRD-SEN-001', '光电传感器', 'PS-D500', '检测距离500mm NPN', 'purchased', '传感器', '个', 'RELEASED', 'V1.2', '漫反射式光电传感器', '李工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRD-CAB-001', '控制柜总成', 'CB-1200', '1200x800x400mm IP54', 'finished', '电气柜', '套', 'PROTOTYPE', 'V0.9', '标准工业控制柜，含内部线束', '王工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRD-RAW-001', '铝合金型材', 'AL-4040', '40x40mm 6米/根', 'raw', '结构材料', '根', 'RELEASED', 'V1.0', '工业铝型材，用于机架搭建', '王工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRD-RAW-002', '冷轧钢板', 'ST-2.0', '2.0mm 1250x2500mm', 'raw', '钣金材料', '张', 'RELEASED', 'V1.0', 'SPCC冷轧钢板，用于柜体加工', '王工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRD-STD-001', '内六角螺栓', 'M8x20', 'M8x20 8.8级 镀锌', 'standard', '紧固件', '盒', 'RELEASED', 'V1.0', '标准内六角圆柱头螺栓', '李工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 2. 项目数据
INSERT INTO plm_project (project_code, project_name, project_type, manager, status, progress, start_date, end_date, created_by, created_time, updated_time) VALUES
('PRJ-2026-001', '智能仓储AGV调度系统研发', '新产品开发', '陈明', 'in-progress', 65, '2026-03-01', '2026-09-30', '陈明', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRJ-2026-002', '伺服驱动器V3.0升级', '产品迭代', '刘芳', 'in-progress', 40, '2026-05-15', '2026-11-30', '刘芳', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRJ-2026-003', 'PLC-X200控制器研发', '新产品开发', '王强', 'planning', 10, '2026-08-01', '2027-03-31', '王强', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRJ-2025-018', '控制柜标准化设计', '技术改进', '赵敏', 'completed', 100, '2025-10-01', '2026-04-30', '赵敏', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PRJ-2026-005', '传感器选型库建设', '基础建设', '陈明', 'in-progress', 80, '2026-02-01', '2026-08-31', '陈明', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 3. 项目任务（甘特图）- id非自增，需显式指定
INSERT INTO plm_task (id, project_id, task_name, task_type, assignee, status, progress, start_date, end_date, duration, parent_id, created_time, updated_time) VALUES
(1001, 1, '需求分析与规格定义', 'task', '陈明', 'completed', 100, '2026-03-01', '2026-03-20', 20, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1002, 1, '系统架构设计', 'task', '陈明', 'completed', 100, '2026-03-21', '2026-04-15', 26, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1003, 1, '调度算法开发', 'task', '李雷', 'in-progress', 70, '2026-04-16', '2026-07-31', 107, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1004, 1, '路径规划模块', 'task', '韩梅', 'in-progress', 55, '2026-05-01', '2026-08-15', 107, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1005, 1, '联调测试', 'task', '陈明', 'pending', 0, '2026-08-16', '2026-09-15', 31, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1006, 1, '验收发布里程碑', 'milestone', '陈明', 'pending', 0, '2026-09-30', '2026-09-30', 1, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(2001, 2, '硬件原理图设计', 'task', '刘芳', 'completed', 100, '2026-05-15', '2026-06-10', 27, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(2002, 2, 'PCB Layout', 'task', '张鑫', 'in-progress', 60, '2026-06-11', '2026-08-05', 56, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(2003, 2, '固件开发', 'task', '刘芳', 'in-progress', 30, '2026-07-01', '2026-10-15', 107, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(3001, 3, '市场调研', 'task', '王强', 'in-progress', 50, '2026-08-01', '2026-08-31', 31, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 4. 甘特图依赖关系
INSERT INTO plm_gantt_link (id, project_id, source_task_id, target_task_id, link_type, created_time) VALUES
(101, 1, 1001, 1002, '0', DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(102, 1, 1002, 1003, '0', DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(103, 1, 1002, 1004, '0', DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(104, 1, 1003, 1005, '0', DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(105, 1, 1004, 1005, '0', DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(106, 1, 1005, 1006, '0', DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(201, 2, 2001, 2002, '0', DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(202, 2, 2002, 2003, '0', DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 5. 资源负载
INSERT INTO plm_resource_load (project_id, resource_id, resource_name, load_date, load_value, created_time) VALUES
(1, 'R001', '陈明', '2026-07-27', 80, DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1, 'R002', '李雷', '2026-07-27', 95, DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1, 'R003', '韩梅', '2026-07-27', 70, DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(2, 'R004', '刘芳', '2026-07-27', 85, DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(2, 'R005', '张鑫', '2026-07-27', 60, DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1, 'R001', '陈明', '2026-07-28', 75, DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1, 'R002', '李雷', '2026-07-28', 100, DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(3, 'R006', '王强', '2026-07-28', 50, DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 6. 产品BOM（伺服电机驱动器的BOM结构）
INSERT INTO plm_bom (bom_code, product_id, product_code, material_code, material_name, material_spec, parent_id, level, quantity, unit, sort_order, status, version, is_key_part, created_by, created_time, updated_time) VALUES
('EBOM-PRD-MTR-001-V2.1-1', 1, 'PRD-MTR-001', 'PRD-PLC-001', '可编程控制器', '16DI/16DO 以太网', NULL, 1, 1.0000, '台', 1, 'RELEASED', 'V2.1', b'1', '张工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('EBOM-PRD-MTR-001-V2.1-2', 1, 'PRD-MTR-001', 'PRD-SEN-001', '光电传感器', '检测距离500mm NPN', NULL, 1, 2.0000, '个', 2, 'RELEASED', 'V2.1', b'0', '张工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('EBOM-PRD-MTR-001-V2.1-3', 1, 'PRD-MTR-001', 'PRD-STD-001', '内六角螺栓', 'M8x20 8.8级 镀锌', NULL, 1, 8.0000, '个', 3, 'RELEASED', 'V2.1', b'0', '张工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('EBOM-PRD-CAB-001-V0.9-1', 5, 'PRD-CAB-001', 'PRD-RAW-002', '冷轧钢板', '2.0mm 1250x2500mm', NULL, 1, 3.5000, '张', 1, 'DRAFT', 'V0.9', b'1', '王工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('EBOM-PRD-CAB-001-V0.9-2', 5, 'PRD-CAB-001', 'PRD-RAW-001', '铝合金型材', '40x40mm 6米/根', NULL, 1, 4.0000, '根', 2, 'DRAFT', 'V0.9', b'0', '王工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 7. 产品文档
INSERT INTO plm_document (doc_code, title, doc_type, category, version, status, author, file_name, file_size, file_format, remark, created_time, updated_time) VALUES
('DOC-2026-001', '伺服驱动器V2.1设计规格书', 'design', '技术文档', 'V2.1', 'released', '张工', 'SD-750W_设计规格书.pdf', '2458624', 'pdf', '包含电气原理与结构设计', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('DOC-2026-002', 'AGV调度系统需求文档', 'requirement', '需求文档', 'V1.3', 'released', '陈明', 'AGV_需求文档.docx', '1048576', 'docx', '已评审通过', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('DOC-2026-003', 'PLC-X200原理图', 'drawing', '图纸', 'V1.0', 'draft', '王强', 'PLC-X200_原理图.dwg', '5242880', 'dwg', '初版原理图，待评审', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('DOC-2026-004', '控制柜装配作业指导书', 'process', '工艺文档', 'V0.9', 'review', '赵敏', 'CB-1200_装配指导书.pdf', '3145728', 'pdf', '试制版本', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('DOC-2026-005', '步进电机测试报告', 'test', '测试文档', 'V1.5', 'released', '李工', 'SM-42BYG_测试报告.xlsx', '819200', 'xlsx', '温升与扭矩测试数据', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 8. 工艺路线
INSERT INTO plm_process_route (route_code, route_name, product_code, product_name, version, route_type, status, create_user, steps_json, created_time, updated_time) VALUES
('RT-2026-001', '伺服驱动器装配工艺路线', 'PRD-MTR-001', '伺服电机驱动器', 'V2.1', 'assembly', 'released', '张工', '[{"seq":10,"name":"PCB板检验","workCenter":"IQC","stdTime":15},{"seq":20,"name":"贴片元件焊接","workCenter":"SMT线","stdTime":45},{"seq":30,"name":"插件与波峰焊","workCenter":"DIP线","stdTime":30},{"seq":40,"name":"整机组装","workCenter":"组装线","stdTime":60},{"seq":50,"name":"老化测试","workCenter":"测试区","stdTime":240},{"seq":60,"name":"包装入库","workCenter":"包装线","stdTime":10}]', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('RT-2026-002', '控制柜钣金加工工艺', 'PRD-CAB-001', '控制柜总成', 'V0.9', 'machining', 'draft', '赵敏', '[{"seq":10,"name":"钢板下料","workCenter":"激光切割","stdTime":30},{"seq":20,"name":"折弯成型","workCenter":"折弯机","stdTime":45},{"seq":30,"name":"焊接框架","workCenter":"焊接区","stdTime":90},{"seq":40,"name":"表面喷塑","workCenter":"喷涂线","stdTime":60}]', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('RT-2026-003', '步进电机测试工艺', 'PRD-MTR-002', '步进电机', 'V1.5', 'test', 'released', '李工', '[{"seq":10,"name":"外观检查","workCenter":"测试区","stdTime":5},{"seq":20,"name":"绝缘电阻测试","workCenter":"测试区","stdTime":10},{"seq":30,"name":"空载运行测试","workCenter":"测试区","stdTime":20},{"seq":40,"name":"负载扭矩测试","workCenter":"测试区","stdTime":30}]', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 9. 工艺变更
INSERT INTO plm_change_request (change_code, title, change_type, process_code, process_name, reason, content, status, apply_user, apply_time, approve_user, approve_time, implement_time, product_id, created_by, created_time, updated_time) VALUES
('ECN-2026-001', '伺服驱动器散热片材质变更', 'material', 'RT-2026-001', '伺服驱动器装配工艺路线', '原铝制散热片成本上升20%', '将散热片材质由6063铝合金变更为压铸铝，预计单台成本降低15元', 'approved', '张工', '2026-07-10', '刘总', '2026-07-15', '2026-08-01', 1, '张工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('ECN-2026-002', '控制柜焊接工序优化', 'process', 'RT-2026-002', '控制柜钣金加工工艺', '焊接变形导致柜体平整度超差', '增加焊接工装夹具，调整焊接顺序由连续焊改为分段跳焊', 'in-progress', '赵敏', '2026-07-20', NULL, NULL, NULL, 5, '赵敏', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('ECN-2026-003', '步进电机测试项目增加', 'design', 'RT-2026-003', '步进电机测试工艺', '客户反馈高频振动异响', '在测试工艺中增加高频振动测试项目（2000Hz，持续30分钟）', 'pending', '李工', '2026-07-28', NULL, NULL, NULL, 2, '李工', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 10. 试产计划
INSERT INTO plm_trial_plan (plan_code, plan_name, product_id, product_code, product_name, version, trial_type, trial_qty, departments_json, stages_json, responsible_person, status, start_date, end_date, created_time, updated_time) VALUES
('TP-2026-001', '控制柜总成首件试制', 5, 'PRD-CAB-001', '控制柜总成', 'V0.9', 'EVT', 5, '["生产部","质量部","工艺部"]', '[{"name":"物料准备","status":"completed"},{"name":"钣金加工","status":"completed"},{"name":"组装","status":"in-progress"},{"name":"检验","status":"pending"}]', '赵敏', 'in-progress', '2026-07-01', '2026-08-15', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('TP-2026-002', 'PLC-X200工程样机试制', 3, 'PRD-PLC-001', '可编程控制器', 'V1.0', 'DVT', 20, '["研发部","生产部"]', '[{"name":"PCB打样","status":"in-progress"},{"name":"贴片","status":"pending"},{"name":"功能测试","status":"pending"}]', '王强', 'pending', '2026-08-20', '2026-09-30', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('TP-2025-020', '伺服驱动器V2.1小批量验证', 1, 'PRD-MTR-001', '伺服电机驱动器', 'V2.1', 'PVT', 100, '["生产部","质量部"]', '[{"name":"物料准备","status":"completed"},{"name":"试产","status":"completed"},{"name":"检验","status":"completed"},{"name":"总结","status":"completed"}]', '张工', 'completed', '2025-11-01', '2025-12-15', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 11. 试产报告
INSERT INTO plm_trial_report (report_code, plan_id, report_title, content, status, create_user, create_time, approve_user, approve_time, main_issues_json, improvements_json, created_time, updated_time) VALUES
('TR-2025-020', 3, '伺服驱动器V2.1小批量试产总结报告', '本次试产100台伺服驱动器，一次通过率96%，主要不良为虚焊（3台）与螺丝滑牙（1台）。整体工艺稳定，建议转入量产。', 'approved', '张工', '2025-12-20', '刘总', '2025-12-25', '["虚焊3台","螺丝滑牙1台"]', '["优化回流焊温度曲线","更换扭力可调电批"]', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('TR-2026-008', 1, '控制柜首件试制中期报告', '钣金加工完成5套，焊接变形2套超差（>2mm），已启动ECN-2026-002变更优化焊接工艺。组装工序进行中。', 'draft', '赵敏', '2026-07-25', NULL, NULL, '["焊接变形2套"]', '["增加焊接工装","调整焊接顺序"]', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 12. 质量问题
INSERT INTO plm_quality_issue (issue_code, issue_title, product_id, severity, status, resolution, create_user, create_time, resolve_user, resolve_time, created_time, updated_time) VALUES
('QI-2026-001', '控制柜焊接变形超差', 5, 'high', 'in-progress', NULL, '赵敏', '2026-07-22', NULL, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('QI-2026-002', '伺服驱动器高温老化报警', 1, 'medium', 'resolved', '更换散热膏品牌，老化测试通过', '张工', '2026-07-05', '李工', '2026-07-12', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('QI-2026-003', '步进电机高频振动异响', 2, 'medium', 'in-progress', NULL, '李工', '2026-07-28', NULL, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('QI-2026-004', 'PLC样机以太网通讯丢包', 3, 'critical', 'pending', NULL, '王强', '2026-07-30', NULL, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 13. 工艺文件
INSERT INTO plm_process_file (file_code, title, file_type, category, version, status, author, file_size, file_format, file_path, remark, created_time, updated_time) VALUES
('PF-2026-001', '伺服驱动器SMT作业指导书', 'instruction', 'SMT工艺', 'V2.1', 'active', '张工', '2097152', 'pdf', '/data/plm/files/process_files/smt_instruction.pdf', '含炉温曲线设置', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PF-2026-002', '控制柜折弯工艺卡', 'process_card', '钣金工艺', 'V0.9', 'draft', '赵敏', '1048576', 'pdf', '/data/plm/files/process_files/bending_card.pdf', '试制版本', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PF-2026-003', '电机测试规范', 'spec', '测试工艺', 'V1.5', 'active', '李工', '1572864', 'docx', '/data/plm/files/process_files/motor_test_spec.docx', '含高频振动测试新增项', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));
