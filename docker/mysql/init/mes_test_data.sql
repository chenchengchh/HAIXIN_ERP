-- MES模块测试数据填充（与化妆品生产场景一致，关联现有工单WO-10~WO-21）
-- 注意：时间统一 +8h 对齐JDBC serverTimezone=Asia/Shanghai 转换逻辑
SET @now8 = DATE_ADD(NOW(), INTERVAL 8 HOUR);

-- ============ 1. 生产执行（execution页面订单/工单、monitoring生产进度） ============
INSERT INTO mes_db.mes_production_execution
(execution_no, production_order_no, erp_production_no, product_code, product_name, plan_quantity, actual_quantity, qualified_quantity, unqualified_quantity, execution_status, workshop, production_line, start_time, end_time, is_deleted, created_by, created_time, updated_by, updated_time) VALUES
('EXE-2026-001','PO-2026-001','ERP-PRD-001','CP-001','焕彩保湿精华液',5000,5200,5100,100,3,'乳化车间','生产线1',DATE_SUB(@now8,INTERVAL 5 DAY),DATE_SUB(@now8,INTERVAL 4 DAY),0,'admin',DATE_SUB(@now8,INTERVAL 5 DAY),'admin',DATE_SUB(@now8,INTERVAL 4 DAY)),
('EXE-2026-002','PO-2026-001','ERP-PRD-001','CP-001','焕彩保湿精华液',1667,1500,1480,20,3,'乳化车间','乳化锅1',DATE_SUB(@now8,INTERVAL 4 DAY),DATE_SUB(@now8,INTERVAL 3 DAY),0,'admin',DATE_SUB(@now8,INTERVAL 4 DAY),'admin',DATE_SUB(@now8,INTERVAL 3 DAY)),
('EXE-2026-003','PO-2026-002','ERP-PRD-002','CP-002','柔润丝滑口红',2000,1200,1180,20,2,'灌装车间','灌装机1',DATE_SUB(@now8,INTERVAL 2 DAY),NULL,0,'admin',DATE_SUB(@now8,INTERVAL 2 DAY),'admin',DATE_SUB(@now8,INTERVAL 6 HOUR)),
('EXE-2026-004','PO-2026-002','ERP-PRD-002','CP-002','柔润丝滑口红',2000,800,790,10,2,'灌装车间','灌装机2',DATE_SUB(@now8,INTERVAL 1 DAY),NULL,0,'admin',DATE_SUB(@now8,INTERVAL 1 DAY),'admin',DATE_SUB(@now8,INTERVAL 3 HOUR)),
('EXE-2026-005','PO-2026-003','ERP-PRD-003','CP-003','净透控油洁面乳',3000,0,0,0,1,'乳化车间','乳化锅2',DATE_ADD(@now8,INTERVAL 1 DAY),NULL,0,'admin',@now8,'admin',@now8),
('EXE-2026-006','PO-2026-003','ERP-PRD-003','CP-003','净透控油洁面乳',3000,1500,1450,50,4,'包装车间','包装机',DATE_SUB(@now8,INTERVAL 12 HOUR),NULL,0,'admin',DATE_SUB(@now8,INTERVAL 12 HOUR),'admin',DATE_SUB(@now8,INTERVAL 2 HOUR)),
('EXE-2026-007','PO-2026-004','ERP-PRD-004','CP-001','焕彩保湿精华液',1000,0,0,0,5,'灌装车间','贴标机',DATE_SUB(@now8,INTERVAL 3 DAY),NULL,0,'admin',DATE_SUB(@now8,INTERVAL 3 DAY),'admin',DATE_SUB(@now8,INTERVAL 2 DAY));

-- ============ 2. 工序派工（execution页面派工） ============
INSERT INTO mes_db.mes_process_assignment
(work_order_id, work_order_no, step_id, step_name, workstation_id, workstation_name, operator_id, operator_name, start_time, end_time, status, create_time, update_time) VALUES
(3,'EXE-2026-003','STEP-001','配料混合','WS-001','混合车间','OP-001','张三',DATE_SUB(@now8,INTERVAL 2 DAY),DATE_SUB(@now8,INTERVAL 1 DAY),'completed',DATE_SUB(@now8,INTERVAL 2 DAY),DATE_SUB(@now8,INTERVAL 1 DAY)),
(3,'EXE-2026-003','STEP-002','乳化搅拌','WS-002','乳化车间','OP-002','李四',DATE_SUB(@now8,INTERVAL 1 DAY),NULL,'in_progress',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 6 HOUR)),
(3,'EXE-2026-003','STEP-003','灌装封口','WS-003','灌装车间','OP-003','王五',NULL,NULL,'assigned',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 1 DAY)),
(4,'EXE-2026-004','STEP-001','配料混合','WS-001','混合车间','OP-004','赵六',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 20 HOUR),'completed',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 20 HOUR)),
(4,'EXE-2026-004','STEP-002','乳化搅拌','WS-002','乳化车间','OP-005','钱七',DATE_SUB(@now8,INTERVAL 18 HOUR),NULL,'in_progress',DATE_SUB(@now8,INTERVAL 18 HOUR),DATE_SUB(@now8,INTERVAL 3 HOUR)),
(6,'EXE-2026-006','STEP-001','贴标喷码','WS-004','包装车间','OP-006','孙八',DATE_SUB(@now8,INTERVAL 10 HOUR),NULL,'in_progress',DATE_SUB(@now8,INTERVAL 10 HOUR),DATE_SUB(@now8,INTERVAL 2 HOUR)),
(1,'EXE-2026-001','STEP-001','配料混合','WS-001','混合车间','OP-001','张三',DATE_SUB(@now8,INTERVAL 5 DAY),DATE_SUB(@now8,INTERVAL 4 DAY),'completed',DATE_SUB(@now8,INTERVAL 5 DAY),DATE_SUB(@now8,INTERVAL 4 DAY));

-- ============ 3. 生产报工（reporting页面，状态: reported/verified/approved） ============
INSERT INTO mes_db.mes_production_report
(report_no, work_order_no, step_name, workstation_name, operator_name, good_qty, scrap_qty, rework_qty, working_hours, machine_hours, start_time, end_time, status, remark, create_time, update_time) VALUES
('RPT-2026-002','EXE-2026-003','配料混合','混合车间','张三',480,5,2,7.5,7.0,DATE_SUB(@now8,INTERVAL 2 DAY),DATE_SUB(@now8,INTERVAL 1 DAY),'approved','批次正常',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 20 HOUR)),
('RPT-2026-003','EXE-2026-003','乳化搅拌','乳化车间','李四',460,8,3,6.5,6.0,DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 6 HOUR),'verified','乳化温度正常',DATE_SUB(@now8,INTERVAL 6 HOUR),DATE_SUB(@now8,INTERVAL 5 HOUR)),
('RPT-2026-004','EXE-2026-004','配料混合','混合车间','赵六',390,4,1,6.0,5.5,DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 20 HOUR),'reported',NULL,DATE_SUB(@now8,INTERVAL 20 HOUR),DATE_SUB(@now8,INTERVAL 20 HOUR)),
('RPT-2026-005','EXE-2026-004','乳化搅拌','乳化车间','钱七',400,6,2,5.5,5.0,DATE_SUB(@now8,INTERVAL 18 HOUR),DATE_SUB(@now8,INTERVAL 3 HOUR),'reported','粘度略高待复检',DATE_SUB(@now8,INTERVAL 3 HOUR),DATE_SUB(@now8,INTERVAL 3 HOUR)),
('RPT-2026-006','EXE-2026-001','配料混合','混合车间','张三',2500,30,10,8.0,7.5,DATE_SUB(@now8,INTERVAL 5 DAY),DATE_SUB(@now8,INTERVAL 4 DAY),'approved','首件检验合格',DATE_SUB(@now8,INTERVAL 4 DAY),DATE_SUB(@now8,INTERVAL 4 DAY)),
('RPT-2026-007','EXE-2026-006','贴标喷码','包装车间','孙八',700,12,5,4.0,3.5,DATE_SUB(@now8,INTERVAL 10 HOUR),DATE_SUB(@now8,INTERVAL 2 HOUR),'reported',NULL,DATE_SUB(@now8,INTERVAL 2 HOUR),DATE_SUB(@now8,INTERVAL 2 HOUR));

-- ============ 4. 设备数据（monitoring设备状态/工艺参数、data-collection设备数据） ============
INSERT INTO mes_db.mes_equipment_data
(equipment_id, equipment_name, equipment_type, parameter_name, parameter_value, status, timestamp, unit) VALUES
('EQ-RHG-001','乳化锅1','乳化设备','asset_status',NULL,'running',DATE_SUB(@now8,INTERVAL 10 MINUTE),NULL),
('EQ-RHG-001','乳化锅1','乳化设备','温度',65.50,'normal',DATE_SUB(@now8,INTERVAL 10 MINUTE),'°C'),
('EQ-RHG-001','乳化锅1','乳化设备','搅拌速度',1200.00,'normal',DATE_SUB(@now8,INTERVAL 10 MINUTE),'rpm'),
('EQ-RHG-001','乳化锅1','乳化设备','真空度',-0.08,'normal',DATE_SUB(@now8,INTERVAL 10 MINUTE),'MPa'),
('EQ-RHG-002','乳化锅2','乳化设备','asset_status',NULL,'running',DATE_SUB(@now8,INTERVAL 8 MINUTE),NULL),
('EQ-RHG-002','乳化锅2','乳化设备','温度',72.30,'warning',DATE_SUB(@now8,INTERVAL 8 MINUTE),'°C'),
('EQ-RHG-002','乳化锅2','乳化设备','搅拌速度',1350.00,'normal',DATE_SUB(@now8,INTERVAL 8 MINUTE),'rpm'),
('EQ-GZJ-001','灌装机1','灌装设备','asset_status',NULL,'running',DATE_SUB(@now8,INTERVAL 5 MINUTE),NULL),
('EQ-GZJ-001','灌装机1','灌装设备','灌装压力',0.45,'normal',DATE_SUB(@now8,INTERVAL 5 MINUTE),'MPa'),
('EQ-GZJ-001','灌装机1','灌装设备','灌装速度',85.00,'normal',DATE_SUB(@now8,INTERVAL 5 MINUTE),'瓶/分'),
('EQ-GZJ-002','灌装机2','灌装设备','asset_status',NULL,'idle',DATE_SUB(@now8,INTERVAL 30 MINUTE),NULL),
('EQ-GZJ-002','灌装机2','灌装设备','灌装压力',0.00,'normal',DATE_SUB(@now8,INTERVAL 30 MINUTE),'MPa'),
('EQ-TBJ-001','贴标机','贴标设备','asset_status',NULL,'down',DATE_SUB(@now8,INTERVAL 2 HOUR),NULL),
('EQ-TBJ-001','贴标机','贴标设备','贴标速度',0.00,'alarm',DATE_SUB(@now8,INTERVAL 2 HOUR),'张/分'),
('EQ-BZJ-001','包装机','包装设备','asset_status',NULL,'running',DATE_SUB(@now8,INTERVAL 3 MINUTE),NULL),
('EQ-BZJ-001','包装机','包装设备','包装速度',60.00,'normal',DATE_SUB(@now8,INTERVAL 3 MINUTE),'盒/分'),
('EQ-BZJ-001','包装机','包装设备','热封温度',150.00,'normal',DATE_SUB(@now8,INTERVAL 3 MINUTE),'°C');

-- ============ 5. 设备故障（monitoring故障历史） ============
INSERT INTO mes_db.mes_equipment_fault
(equipment_id, equipment_name, fault_type, fault_description, occur_time, repair_person, repair_time, status, create_time) VALUES
('EQ-TBJ-001','贴标机','传动异常','贴标头卡标导致停机',DATE_SUB(@now8,INTERVAL 2 HOUR),'维修-周工',NULL,'repairing',DATE_SUB(@now8,INTERVAL 2 HOUR)),
('EQ-RHG-002','乳化锅2','温度异常','加热管温度波动超±3°C',DATE_SUB(@now8,INTERVAL 8 HOUR),'维修-吴工',DATE_SUB(@now8,INTERVAL 6 HOUR),'resolved',DATE_SUB(@now8,INTERVAL 8 HOUR)),
('EQ-GZJ-001','灌装机1','密封泄漏','灌装针头密封圈老化渗漏',DATE_SUB(@now8,INTERVAL 2 DAY),'维修-周工',DATE_SUB(@now8,INTERVAL 1 DAY),'resolved',DATE_SUB(@now8,INTERVAL 2 DAY)),
('EQ-BZJ-001','包装机','传感器异常','光电传感器误触发',DATE_SUB(@now8,INTERVAL 30 MINUTE),NULL,NULL,'reported',DATE_SUB(@now8,INTERVAL 30 MINUTE));

-- ============ 6. 质量检验记录（data-collection质量数据，result: PASS/FAIL，status: completed/pending/rejected） ============
INSERT INTO mes_db.mes_quality_inspection
(inspection_id, inspection_name, work_order_no, sn_code, step_id, step_name, defect_type, defect_description, result, status, inspector_id, inspector_name, inspection_time, inspection_items_json, create_time) VALUES
('QI-2026-001','乳化半成品检验','EXE-2026-003','SN-2026-0001','STEP-002','乳化搅拌',NULL,NULL,'PASS','completed','QC-001','质检-陈静',DATE_SUB(@now8,INTERVAL 5 HOUR),'[{"itemName":"粘度","standard":"2000-3000cP","actual":"2500cP","result":"PASS"},{"itemName":"pH值","standard":"5.5-7.0","actual":"6.2","result":"PASS"}]',DATE_SUB(@now8,INTERVAL 5 HOUR)),
('QI-2026-002','灌装首件检验','EXE-2026-003','SN-2026-0002','STEP-003','灌装封口','净含量偏差','首件净含量偏差+2.5g','FAIL','rejected','QC-002','质检-刘洋',DATE_SUB(@now8,INTERVAL 4 HOUR),'[{"itemName":"净含量","standard":"50±1g","actual":"52.5g","result":"FAIL"}]',DATE_SUB(@now8,INTERVAL 4 HOUR)),
('QI-2026-003','灌装巡检','EXE-2026-004','SN-2026-0003','STEP-003','灌装封口',NULL,NULL,'PASS','completed','QC-001','质检-陈静',DATE_SUB(@now8,INTERVAL 2 HOUR),'[{"itemName":"净含量","standard":"50±1g","actual":"50.3g","result":"PASS"},{"itemName":"封口完整性","standard":"无泄漏","actual":"无泄漏","result":"PASS"}]',DATE_SUB(@now8,INTERVAL 2 HOUR)),
('QI-2026-004','贴标外观检验','EXE-2026-006','SN-2026-0004','STEP-001','贴标喷码',NULL,NULL,'PASS','pending','QC-003','质检-王芳',DATE_SUB(@now8,INTERVAL 1 HOUR),NULL,DATE_SUB(@now8,INTERVAL 1 HOUR)),
('QI-2026-005','成品出厂检验','EXE-2026-001','SN-2026-0005','STEP-004','成品包装',NULL,NULL,'PASS','completed','QC-002','质检-刘洋',DATE_SUB(@now8,INTERVAL 3 DAY),'[{"itemName":"微生物","standard":"菌落总数<100CFU/g","actual":"合格","result":"PASS"}]',DATE_SUB(@now8,INTERVAL 3 DAY));

-- ============ 7. 人工上报（data-collection人工数据，status: submitted/verified/approved） ============
INSERT INTO mes_db.mes_manual_reporting
(work_order_no, step_id, step_name, workstation_id, workstation_name, operator_id, operator_name, good_qty, scrap_qty, rework_qty, working_hours, start_time, end_time, status, remarks, create_time, update_time) VALUES
('EXE-2026-003','STEP-002','乳化搅拌','WS-002','乳化车间','OP-002','李四',460,8,3,6,DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 6 HOUR),'approved','乳化完成，已送检',DATE_SUB(@now8,INTERVAL 6 HOUR),DATE_SUB(@now8,INTERVAL 5 HOUR)),
('EXE-2026-004','STEP-002','乳化搅拌','WS-002','乳化车间','OP-005','钱七',400,6,2,5,DATE_SUB(@now8,INTERVAL 18 HOUR),DATE_SUB(@now8,INTERVAL 3 HOUR),'verified','粘度略高',DATE_SUB(@now8,INTERVAL 3 HOUR),DATE_SUB(@now8,INTERVAL 2 HOUR)),
('EXE-2026-006','STEP-001','贴标喷码','WS-004','包装车间','OP-006','孙八',700,12,5,4,DATE_SUB(@now8,INTERVAL 10 HOUR),DATE_SUB(@now8,INTERVAL 2 HOUR),'submitted','卡标导致废品偏多',DATE_SUB(@now8,INTERVAL 2 HOUR),DATE_SUB(@now8,INTERVAL 2 HOUR)),
('EXE-2026-001','STEP-001','配料混合','WS-001','混合车间','OP-001','张三',2500,30,10,8,DATE_SUB(@now8,INTERVAL 5 DAY),DATE_SUB(@now8,INTERVAL 4 DAY),'approved',NULL,DATE_SUB(@now8,INTERVAL 4 DAY),DATE_SUB(@now8,INTERVAL 4 DAY));

-- ============ 8. 批次（wip页面批次，status: in_process/completed/scrapped） ============
INSERT INTO mes_db.mes_batch
(batch_no, work_order_no, material_id, material_name, qty, status, create_time, update_time) VALUES
('BATCH-2026-001','EXE-2026-001','CP-001','焕彩保湿精华液',5000,'completed',DATE_SUB(@now8,INTERVAL 5 DAY),DATE_SUB(@now8,INTERVAL 4 DAY)),
('BATCH-2026-002','EXE-2026-003','CP-002','柔润丝滑口红',1200,'in_process',DATE_SUB(@now8,INTERVAL 2 DAY),DATE_SUB(@now8,INTERVAL 6 HOUR)),
('BATCH-2026-003','EXE-2026-004','CP-002','柔润丝滑口红',800,'in_process',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 3 HOUR)),
('BATCH-2026-004','EXE-2026-006','CP-003','净透控油洁面乳',1500,'in_process',DATE_SUB(@now8,INTERVAL 12 HOUR),DATE_SUB(@now8,INTERVAL 2 HOUR)),
('BATCH-2026-005','EXE-2026-007','CP-001','焕彩保湿精华液',100,'scrapped',DATE_SUB(@now8,INTERVAL 3 DAY),DATE_SUB(@now8,INTERVAL 2 DAY));

-- ============ 9. 在制品位置（wip页面，status: queuing/processing/completed/scrapped） ============
INSERT INTO mes_db.mes_wip_location
(sn_code, work_order_no, current_station_id, current_station_name, current_step_id, current_step_name, status, create_time, update_time) VALUES
('SN-2026-0001','EXE-2026-003','WS-003','灌装车间','STEP-003','灌装封口','queuing',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 5 HOUR)),
('SN-2026-0002','EXE-2026-003','WS-003','灌装车间','STEP-003','灌装封口','processing',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 4 HOUR)),
('SN-2026-0003','EXE-2026-004','WS-002','乳化车间','STEP-002','乳化搅拌','processing',DATE_SUB(@now8,INTERVAL 18 HOUR),DATE_SUB(@now8,INTERVAL 2 HOUR)),
('SN-2026-0004','EXE-2026-006','WS-004','包装车间','STEP-001','贴标喷码','processing',DATE_SUB(@now8,INTERVAL 10 HOUR),DATE_SUB(@now8,INTERVAL 2 HOUR)),
('SN-2026-0005','EXE-2026-001','WS-005','成品仓库','STEP-004','成品包装','completed',DATE_SUB(@now8,INTERVAL 5 DAY),DATE_SUB(@now8,INTERVAL 3 DAY)),
('SN-2026-0006','EXE-2026-007','WS-004','包装车间','STEP-001','贴标喷码','scrapped',DATE_SUB(@now8,INTERVAL 3 DAY),DATE_SUB(@now8,INTERVAL 2 DAY));

-- ============ 10. 在制品位置历史 ============
INSERT INTO mes_db.mes_wip_location_history
(wip_location_id, station_id, station_name, step_id, step_name, start_time, end_time) VALUES
(1,'WS-001','混合车间','STEP-001','配料混合',DATE_SUB(@now8,INTERVAL 2 DAY),DATE_SUB(@now8,INTERVAL 1 DAY)),
(1,'WS-002','乳化车间','STEP-002','乳化搅拌',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 5 HOUR)),
(1,'WS-003','灌装车间','STEP-003','灌装封口',DATE_SUB(@now8,INTERVAL 5 HOUR),NULL),
(2,'WS-001','混合车间','STEP-001','配料混合',DATE_SUB(@now8,INTERVAL 2 DAY),DATE_SUB(@now8,INTERVAL 1 DAY)),
(2,'WS-002','乳化车间','STEP-002','乳化搅拌',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 4 HOUR)),
(3,'WS-001','混合车间','STEP-001','配料混合',DATE_SUB(@now8,INTERVAL 1 DAY),DATE_SUB(@now8,INTERVAL 20 HOUR)),
(3,'WS-002','乳化车间','STEP-002','乳化搅拌',DATE_SUB(@now8,INTERVAL 20 HOUR),NULL),
(5,'WS-001','混合车间','STEP-001','配料混合',DATE_SUB(@now8,INTERVAL 5 DAY),DATE_SUB(@now8,INTERVAL 4 DAY)),
(5,'WS-005','成品仓库','STEP-004','成品包装',DATE_SUB(@now8,INTERVAL 4 DAY),DATE_SUB(@now8,INTERVAL 3 DAY));

-- ============ 11. 流转记录（wip页面，status: success/failed） ============
INSERT INTO mes_db.mes_flow_record
(sn_code, batch_no, from_station_id, from_station_name, from_step_id, from_step_name, to_station_id, to_station_name, to_step_id, to_step_name, operator_id, operator_name, status, remarks, timestamp) VALUES
('SN-2026-0001','BATCH-2026-002','WS-001','混合车间','STEP-001','配料混合','WS-002','乳化车间','STEP-002','乳化搅拌','OP-001','张三','success','正常流转',DATE_SUB(@now8,INTERVAL 1 DAY)),
('SN-2026-0001','BATCH-2026-002','WS-002','乳化车间','STEP-002','乳化搅拌','WS-003','灌装车间','STEP-003','灌装封口','OP-002','李四','success','检验合格后流转',DATE_SUB(@now8,INTERVAL 5 HOUR)),
('SN-2026-0002','BATCH-2026-002','WS-002','乳化车间','STEP-002','乳化搅拌','WS-003','灌装车间','STEP-003','灌装封口','OP-002','李四','success',NULL,DATE_SUB(@now8,INTERVAL 4 HOUR)),
('SN-2026-0003','BATCH-2026-003','WS-001','混合车间','STEP-001','配料混合','WS-002','乳化车间','STEP-002','乳化搅拌','OP-004','赵六','success',NULL,DATE_SUB(@now8,INTERVAL 20 HOUR)),
('SN-2026-0004','BATCH-2026-004','WS-002','乳化车间','STEP-002','乳化搅拌','WS-004','包装车间','STEP-001','贴标喷码','OP-006','孙八','success',NULL,DATE_SUB(@now8,INTERVAL 10 HOUR)),
('SN-2026-0006','BATCH-2026-005','WS-004','包装车间','STEP-001','贴标喷码','WS-006','报废区','STEP-099','报废','OP-006','孙八','failed','贴标不良报废',DATE_SUB(@now8,INTERVAL 2 DAY)),
('SN-2026-0005','BATCH-2026-001','WS-004','包装车间','STEP-003','成品包装','WS-005','成品仓库','STEP-004','入库','OP-007','周九','success','成品入库',DATE_SUB(@now8,INTERVAL 3 DAY));

SELECT 'production_execution' t, COUNT(*) c FROM mes_db.mes_production_execution
UNION SELECT 'process_assignment', COUNT(*) FROM mes_db.mes_process_assignment
UNION SELECT 'production_report', COUNT(*) FROM mes_db.mes_production_report
UNION SELECT 'equipment_data', COUNT(*) FROM mes_db.mes_equipment_data
UNION SELECT 'equipment_fault', COUNT(*) FROM mes_db.mes_equipment_fault
UNION SELECT 'quality_inspection', COUNT(*) FROM mes_db.mes_quality_inspection
UNION SELECT 'manual_reporting', COUNT(*) FROM mes_db.mes_manual_reporting
UNION SELECT 'batch', COUNT(*) FROM mes_db.mes_batch
UNION SELECT 'wip_location', COUNT(*) FROM mes_db.mes_wip_location
UNION SELECT 'wip_location_history', COUNT(*) FROM mes_db.mes_wip_location_history
UNION SELECT 'flow_record', COUNT(*) FROM mes_db.mes_flow_record;
