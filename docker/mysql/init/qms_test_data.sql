-- QMS模块测试数据填充脚本
-- 注意：MySQL容器时区为UTC，JDBC URL带serverTimezone=Asia/Shanghai，需手动+8小时

USE qms_db;

-- 1. 检验标准数据
INSERT INTO qms_inspection_standard (standard_no, material_code, material_name, version, aql_level, status, inspection_items_json, creator, created_time, updated_time) VALUES
('STD-2024-001', 'MAT-001', '电子元器件', 'V1.0', 'AQL-1.0', 'active', '[{"itemName":"外观检查","standard":"无划痕、无氧化","method":"目视检查","tool":"放大镜"},{"itemName":"尺寸测量","standard":"符合图纸公差","method":"卡尺测量","tool":"数显卡尺"}]', 'admin', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('STD-2024-002', 'MAT-002', '机械零部件', 'V1.0', 'AQL-1.5', 'active', '[{"itemName":"硬度测试","standard":"HRC 45-50","method":"硬度计测试","tool":"洛氏硬度计"},{"itemName":"表面处理","standard":"镀层厚度≥10μm","method":"测厚仪","tool":"镀层测厚仪"}]', 'admin', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('STD-2024-003', 'MAT-003', '包装材料', 'V1.0', 'AQL-2.5', 'active', '[{"itemName":"耐破强度","standard":"≥1500kPa","method":"耐破度仪","tool":"耐破度测试仪"},{"itemName":"防潮性能","standard":"24h增重≤5%","method":"恒温恒湿箱","tool":"恒温恒湿试验箱"}]', 'admin', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('STD-2024-004', 'MAT-004', '化学原料', 'V1.0', 'AQL-0.65', 'inactive', '[{"itemName":"纯度分析","standard":"≥99.5%","method":"色谱分析","tool":"气相色谱仪"},{"itemName":"水分含量","standard":"≤0.5%","method":"卡尔费休法","tool":"水分测定仪"}]', 'admin', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('STD-2024-005', 'MAT-005', '成品整机', 'V2.0', 'AQL-0.4', 'active', '[{"itemName":"功能测试","standard":"全部功能正常","method":"老化测试","tool":"老化测试台"},{"itemName":"安全测试","standard":"符合GB4943","method":"安规测试","tool":"安规测试仪"}]', 'admin', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 2. 检验计划数据
INSERT INTO qms_inspection_plan (plan_no, plan_name, plan_type, material_code, material_name, inspection_standard_id, inspection_standard_no, sampling_plan, sampling_rule, inspection_frequency, inspection_department, inspection_person, batch_size, effective_date, expiry_date, status, description, creator, created_time, updated_time) VALUES
('PLAN-2024-001', '电子元器件进料检验计划', 'IQC', 'MAT-001', '电子元器件', 1, 'STD-2024-001', 'GB/T 2828.1', '正常检验一次抽样', '每批检验', '质量部', '张三', 1000, '2024-01-01', '2024-12-31', 'active', '电子元器件常规进料检验', 'admin', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PLAN-2024-002', '机械零部件过程检验计划', 'PQC', 'MAT-002', '机械零部件', 2, 'STD-2024-002', 'GB/T 2828.1', '加严检验一次抽样', '每批检验', '质量部', '李四', 500, '2024-01-01', '2024-12-31', 'active', '机械零部件关键工序检验', 'admin', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PLAN-2024-003', '包装材料进料检验计划', 'IQC', 'MAT-003', '包装材料', 3, 'STD-2024-003', 'GB/T 2828.1', '正常检验一次抽样', '每批检验', '质量部', '王五', 2000, '2024-01-01', '2024-12-31', 'active', '包装材料常规进料检验', 'admin', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PLAN-2024-004', '成品出厂检验计划', 'OQC', 'MAT-005', '成品整机', 5, 'STD-2024-005', '全检', '100%检验', '每批检验', '质量部', '赵六', 100, '2024-01-01', '2024-12-31', 'active', '成品出厂全数检验', 'admin', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('PLAN-2024-005', '化学原料进料检验计划', 'IQC', 'MAT-004', '化学原料', 4, 'STD-2024-004', 'GB/T 2828.1', '正常检验一次抽样', '每批检验', '质量部', '张三', 200, '2024-01-01', '2024-12-31', 'inactive', '化学原料进料检验（暂停）', 'admin', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 3. 检验任务数据
INSERT INTO qms_inspection_task (task_no, task_name, plan_id, plan_no, plan_name, material_code, material_name, batch_no, quantity, task_type, assignee, assign_time, due_time, status, inspection_result, description, actual_finish_time, created_time, updated_time) VALUES
('TASK-2024-001', '电子元器件批次20240101检验', 1, 'PLAN-2024-001', '电子元器件进料检验计划', 'MAT-001', '电子元器件', 'BATCH-2024-001', 1000.00, 'IQC', '张三', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL 3 DAY), 'completed', 'pass', '批次20240101常规检验', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('TASK-2024-002', '电子元器件批次20240102检验', 1, 'PLAN-2024-001', '电子元器件进料检验计划', 'MAT-001', '电子元器件', 'BATCH-2024-002', 1500.00, 'IQC', '张三', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL 3 DAY), 'in_progress', NULL, '批次20240102常规检验', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('TASK-2024-003', '机械零部件批次20240101检验', 2, 'PLAN-2024-002', '机械零部件过程检验计划', 'MAT-002', '机械零部件', 'BATCH-2024-003', 500.00, 'PQC', '李四', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL 2 DAY), 'pending', NULL, '批次20240101过程检验', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('TASK-2024-004', '包装材料批次20240101检验', 3, 'PLAN-2024-003', '包装材料进料检验计划', 'MAT-003', '包装材料', 'BATCH-2024-004', 2000.00, 'IQC', '王五', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL 3 DAY), 'completed', 'fail', '批次20240101进料检验', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('TASK-2024-005', '成品整机批次20240101检验', 4, 'PLAN-2024-004', '成品出厂检验计划', 'MAT-005', '成品整机', 'BATCH-2024-005', 100.00, 'OQC', '赵六', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL 1 DAY), 'pending', NULL, '批次20240101出厂检验', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('TASK-2024-006', '电子元器件批次20240103检验', 1, 'PLAN-2024-001', '电子元器件进料检验计划', 'MAT-001', '电子元器件', 'BATCH-2024-006', 800.00, 'IQC', NULL, NULL, DATE_ADD(DATE_ADD(NOW(), INTERVAL 8 HOUR), INTERVAL 3 DAY), 'pending', NULL, '批次20240103待分配', NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 4. 检验结果数据
INSERT INTO qms_inspection_result (result_no, task_id, task_no, material_code, material_name, batch_no, inspection_quantity, qualified_quantity, unqualified_quantity, inspection_result, inspection_time, inspector, audit_status, auditor, audit_time, audit_remark, inspection_items_json, description, created_time, updated_time) VALUES
('RESULT-2024-001', 1, 'TASK-2024-001', 'MAT-001', '电子元器件', 'BATCH-2024-001', 1000.00, 995.00, 5.00, 'pass', DATE_ADD(NOW(), INTERVAL 8 HOUR), '张三', 'approved', '质量经理', DATE_ADD(NOW(), INTERVAL 8 HOUR), '符合AQL标准', '[{"itemName":"外观检查","result":"pass","defectCount":2},{"itemName":"尺寸测量","result":"pass","defectCount":3}]', '批次20240101检验合格', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('RESULT-2024-002', 4, 'TASK-2024-004', 'MAT-003', '包装材料', 'BATCH-2024-004', 2000.00, 1850.00, 150.00, 'fail', DATE_ADD(NOW(), INTERVAL 8 HOUR), '王五', 'approved', '质量经理', DATE_ADD(NOW(), INTERVAL 8 HOUR), '超出AQL标准，判定不合格', '[{"itemName":"耐破强度","result":"fail","defectCount":120},{"itemName":"防潮性能","result":"pass","defectCount":30}]', '批次20240101检验不合格', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('RESULT-2024-003', 2, 'TASK-2024-002', 'MAT-001', '电子元器件', 'BATCH-2024-002', 800.00, 798.00, 2.00, 'pass', DATE_ADD(NOW(), INTERVAL 8 HOUR), '张三', 'pending', NULL, NULL, NULL, '[{"itemName":"外观检查","result":"pass","defectCount":1},{"itemName":"尺寸测量","result":"pass","defectCount":1}]', '批次20240102部分检验完成', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 5. 不合格品登记数据
INSERT INTO qms_nc_registration (registration_no, material_code, material_name, batch_no, quantity, defect_type, defect_level, defect_description, discovery_time, discovery_location, discovery_department, discovery_person, registrant, registration_time, status, review_status, disposal_status, tracking_status, created_time, updated_time) VALUES
('NC-2024-001', 'MAT-003', '包装材料', 'BATCH-2024-004', 150.00, '性能不合格', 'major', '耐破强度未达到标准要求', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'IQC检验区', '质量部', '王五', '王五', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'registered', 'pending', 'pending', 'pending', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('NC-2024-002', 'MAT-001', '电子元器件', 'BATCH-2024-001', 5.00, '外观缺陷', 'minor', '少量引脚氧化', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'IQC检验区', '质量部', '张三', '张三', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'registered', 'approved', 'processing', 'pending', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('NC-2024-003', 'MAT-002', '机械零部件', 'BATCH-2024-003', 20.00, '尺寸超差', 'critical', '关键尺寸超出公差范围', DATE_ADD(NOW(), INTERVAL 8 HOUR), '生产车间', '生产部', '李四', '李四', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'registered', 'pending', 'pending', 'pending', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 6. 质量异常报告数据
INSERT INTO qms_anomaly_report (report_no, title, anomaly_type, severity, occurrence_time, occurrence_location, description, impact_assessment, related_products_json, reporter, report_time, status, created_time, updated_time) VALUES
('ANOM-2024-001', '包装材料耐破强度异常', '来料质量异常', 'major', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'IQC检验区', '供应商A提供的包装材料耐破强度连续两批低于标准要求', '可能导致产品在运输过程中破损', '[{"materialCode":"MAT-003","materialName":"包装材料","batchNo":"BATCH-2024-004","quantity":150}]', '王五', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'reported', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('ANOM-2024-002', '生产设备精度偏移', '过程质量异常', 'critical', DATE_ADD(NOW(), INTERVAL 8 HOUR), '生产车间', 'CNC加工中心精度出现偏移，导致机械零部件尺寸超差', '影响产品装配精度，可能导致整机故障', '[{"materialCode":"MAT-002","materialName":"机械零部件","batchNo":"BATCH-2024-003","quantity":20}]', '李四', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'analyzing', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('ANOM-2024-003', '成品功能测试失败率上升', '成品质量异常', 'minor', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'OQC检验区', '近期成品功能测试失败率从0.5%上升至1.2%', '可能影响客户使用体验', '[{"materialCode":"MAT-005","materialName":"成品整机","batchNo":"BATCH-2024-005","quantity":100}]', '赵六', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'closed', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 7. CAPA预防措施数据
INSERT INTO qms_capa (report_id, report_no, corrective_measure, preventive_measure, implementation_team_json, implementation_deadline, implementation_status, actual_completion_date, review_result, reviewer, review_date, verify_status, verify_result, verify_date, created_time, updated_time) VALUES
(1, 'ANOM-2024-001', '对不合格批次进行退货处理，要求供应商整改', '加强供应商审核，增加进料检验频次', '[{"name":"采购部","responsibility":"供应商管理"},{"name":"质量部","responsibility":"检验标准更新"}]', '2024-03-31', 'completed', '2024-03-15', 'approved', '质量总监', '2024-03-20', 'verified', '整改有效，供应商已恢复供货', '2024-04-01', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(2, 'ANOM-2024-002', '对CNC加工中心进行精度校准，对已生产批次全检', '建立设备定期校准制度，增加过程巡检频次', '[{"name":"设备部","responsibility":"设备维护"},{"name":"质量部","responsibility":"过程监控"}]', '2024-02-28', 'in_progress', NULL, NULL, NULL, NULL, 'pending', NULL, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 8. 不合格品评审数据
INSERT INTO qms_nc_review (registration_id, registration_no, disposal_plan, review_status, review_opinion, review_team_json, reviewer, review_time, review_date, created_time, updated_time) VALUES
(2, 'NC-2024-002', 'rework', 'approved', '轻微外观缺陷，可让步接收', '[{"name":"质量部","role":"评审"}]', '质量经理', DATE_ADD(NOW(), INTERVAL 8 HOUR), '2024-01-15', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1, 'NC-2024-001', 'return', 'rejected', '性能不合格，不可接收', '[{"name":"质量部","role":"评审"}]', '质量经理', DATE_ADD(NOW(), INTERVAL 8 HOUR), '2024-01-15', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 9. 不合格品处理数据
INSERT INTO qms_nc_disposal (registration_id, registration_no, review_id, disposal_plan, disposal_description, handler, start_time, end_time, process_result, disposal_status, created_time, updated_time) VALUES
(2, 'NC-2024-002', 1, 'rework', '返工处理', '李四', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR), '返工完成，检验合格', 'completed', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1, 'NC-2024-001', 2, 'return', '退货处理', '采购员', DATE_ADD(NOW(), INTERVAL 8 HOUR), NULL, NULL, 'processing', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 10. 不合格品追踪数据
INSERT INTO qms_nc_tracking (registration_id, registration_no, disposal_id, disposal_plan, tracking_content, tracker, tracking_time, effectiveness, improvement_suggestions, tracking_status, disposal_status, created_time, updated_time) VALUES
(2, 'NC-2024-002', 1, 'rework', '现场跟踪返工过程', '质量工程师', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'effective', '加强供应商来料管控', 'completed', 'completed', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(1, 'NC-2024-001', 2, 'return', '文档跟踪退货流程', '质量工程师', DATE_ADD(NOW(), INTERVAL 8 HOUR), NULL, NULL, 'in_progress', 'processing', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 11. 数据采集数据
INSERT INTO qms_data_collection (collection_no, collection_name, data_type, collection_date, collector, source, data_items_json, status, created_time, updated_time) VALUES
('DC-2024-001', '进料检验数据采集', 'inspection', '2024-01-15', '张三', 'IQC检验系统', '[{"item":"外观检查","value":"合格"},{"item":"尺寸测量","value":"合格"}]', 'active', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('DC-2024-002', '过程检验数据采集', 'process', '2024-01-15', '李四', 'PQC检验系统', '[{"item":"硬度测试","value":"HRC 48"},{"item":"表面处理","value":"12μm"}]', 'active', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('DC-2024-003', '出货检验数据采集', 'shipment', '2024-01-15', '赵六', 'OQC检验系统', '[{"item":"功能测试","value":"通过"},{"item":"安全测试","value":"通过"}]', 'active', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 12. 质量报告数据
INSERT INTO qms_quality_report (report_no, report_name, report_type, period, content_json, creator, create_time, status, created_time, updated_time) VALUES
('RPT-2024-001', '2024年1月质量月报', 'monthly', '2024-01', '{"totalInspections":260,"passedInspections":235,"failedInspections":25,"passRate":90.38}', '质量经理', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'generated', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('RPT-2024-002', '2024年第3周质量周报', 'weekly', '2024-W03', '{"totalInspections":60,"passedInspections":57,"failedInspections":3,"passRate":95.00}', '质量主管', DATE_ADD(NOW(), INTERVAL 8 HOUR), 'generated', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 13. 预测结果数据
INSERT INTO qms_forecast_result (forecast_type, period, forecast_data_json, confidence_interval, status, create_time, created_time, updated_time) VALUES
('trend', '2024-02', '{"target":"电子元器件不良率","forecastValue":0.85,"trend":"下降"}', 0.92, 'active', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
('trend', '2024-02', '{"target":"机械零部件不良率","forecastValue":1.20,"trend":"上升"}', 0.88, 'active', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 14. 异常分析数据
INSERT INTO qms_anomaly_analysis (report_id, report_no, analysis_method, analysis_status, analyzer, analysis_time, analysis_date, man_factor, machine_factor, material_factor, method_factor, environment_factor, measurement_factor, root_cause, analysis_team_json, created_time, updated_time) VALUES
(1, 'ANOM-2024-001', '5Why分析法', 'completed', '质量工程师', DATE_ADD(NOW(), INTERVAL 8 HOUR), '2024-01-15', '操作人员培训不足', '设备正常', '供应商原材料质量下降', '检验方法正确', '环境符合要求', '测量设备校准正常', '供应商更换原材料供应商，质量控制不严', '[{"name":"质量部","role":"分析"}]', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(2, 'ANOM-2024-002', '鱼骨图分析', 'in_progress', '设备工程师', DATE_ADD(NOW(), INTERVAL 8 HOUR), '2024-01-15', '操作规范', '设备长期未校准，刀具磨损', '材料符合要求', '加工参数设置正确', '环境温度正常', '测量设备正常', '设备维护计划执行不到位，刀具更换不及时', '[{"name":"设备部","role":"分析"}]', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

-- 15. 异常处理数据
INSERT INTO qms_anomaly_disposal (report_id, report_no, disposal_plan, responsible_person, start_date, deadline, actual_completion_date, disposal_result, disposal_status, verification_result, verifier, verification_time, created_time, updated_time) VALUES
(1, 'ANOM-2024-001', '退货处理，供应商整改', '采购经理', '2024-01-15', '2024-02-15', '2024-02-10', '供应商已整改，恢复供货', 'completed', 'verified', '质量经理', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR)),
(2, 'ANOM-2024-002', '设备校准，刀具更换', '设备主管', '2024-01-15', '2024-02-28', NULL, NULL, 'processing', NULL, NULL, NULL, DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

SELECT 'QMS测试数据填充完成' AS message;
