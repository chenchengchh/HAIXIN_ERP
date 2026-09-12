SET time_zone = '+8:00';

USE ems_db;

INSERT INTO ems_meter_device (name, type, ip_address, status, last_update, created_at, updated_at)
SELECT '智能电表-001', '电表', '192.168.1.101', 'online', NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ems_meter_device LIMIT 1);

INSERT INTO ems_meter_device (name, type, ip_address, status, last_update, created_at, updated_at)
SELECT '水表-1', 'water', '192.168.1.102', 'offline', NOW(), NOW(), NOW()
WHERE (SELECT COUNT(1) FROM ems_meter_device) = 1;

INSERT INTO ems_real_time_data (energy_type, area, actual_value, unit, collection_time, status, created_at, updated_at)
SELECT '电力', '车间1', 128.5, 'kWh', NOW(), 'normal', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ems_real_time_data LIMIT 1);

INSERT INTO ems_real_time_data (energy_type, area, actual_value, unit, collection_time, status, created_at, updated_at)
SELECT 'water', '车间A', 32.1, 'm3', NOW(), 'normal', NOW(), NOW()
WHERE (SELECT COUNT(1) FROM ems_real_time_data) = 1;

INSERT INTO ems_energy_anomaly (energy_type, area, anomaly_type, actual_value, expected_value, detection_time, status, created_at, updated_at)
SELECT '电力', '车间1', '突增', 180.0, 120.0, NOW(), 'pending', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ems_energy_anomaly LIMIT 1);

INSERT INTO ems_optimization_suggestion (title, content, target_area, estimated_effect, status, created_at, updated_at)
SELECT '优化空压机启停策略', '建议根据负荷曲线调整空压机启停阈值，减少空载运行。', '车间A', '预计节能5%', 'pending', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ems_optimization_suggestion LIMIT 1);

INSERT INTO ems_optimization_plan (plan_name, target_area, predicted_saving, actual_saving, status, exec_content, report_url, start_date, end_date, created_at, updated_at)
SELECT '空压机优化方案', '车间A', 5.0, NULL, 2, '调整启停阈值并启用巡检', NULL, NOW(), NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ems_optimization_plan LIMIT 1);

INSERT INTO ems_effect_evaluation (plan_id, plan_name, target_area, predicted_saving, actual_saving, evaluation_period, energy_type, achievement_rate, evaluation_date, evaluation_content, created_at, updated_at)
SELECT 1, '空压机优化方案', '车间1', 5.0, 4.2, '月度', '电力', 84.0, NOW(), '节能效果良好，建议持续优化。', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ems_effect_evaluation LIMIT 1);

INSERT INTO ems_standard_report (name, type, frequency, last_generated, status, created_at, updated_at)
SELECT '能耗日报', '日报', '每日', NOW(), 'active', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ems_standard_report LIMIT 1);

INSERT INTO ems_standard_report (name, type, frequency, last_generated, status, created_at, updated_at)
SELECT '能耗月报', '月报', '每月', NOW(), 'active', NOW(), NOW()
WHERE (SELECT COUNT(1) FROM ems_standard_report) = 1;

INSERT INTO ems_custom_report (name, creator, created_date, last_modified, sql_query, template_config)
SELECT '车间1电耗明细', '管理员', NOW(), NOW(), 'SELECT * FROM ems_real_time_data WHERE energy_type = ''电力'' AND area = ''车间1''', '{}'
WHERE NOT EXISTS (SELECT 1 FROM ems_custom_report LIMIT 1);

INSERT INTO ems_report_auto_task (report_name, frequency, next_execution, recipients, status, created_at, updated_at)
SELECT '能耗日报', '每日', DATE_ADD(NOW(), INTERVAL 1 DAY), 'admin@example.com', 'enabled', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM ems_report_auto_task LIMIT 1);
