-- 修正旧trend数据为数组格式（前端期望[{x,y}]结构）
UPDATE qms_db.qms_forecast_result SET forecast_data_json='[{"x":1,"y":0.92},{"x":2,"y":0.88},{"x":3,"y":0.85}]' WHERE id=1;
UPDATE qms_db.qms_forecast_result SET forecast_data_json='[{"x":1,"y":1.05},{"x":2,"y":1.12},{"x":3,"y":1.20}]' WHERE id=2;

-- 补充pass-rate类型预测数据（趋势预测页默认查询类型）
INSERT INTO qms_db.qms_forecast_result (forecast_type, period, forecast_data_json, confidence_interval, create_time, status, created_time, updated_time)
VALUES ('pass-rate', '2024-02', '[{"x":"2024-01","y":97.2},{"x":"2024-02","y":97.8},{"x":"2024-03","y":98.1},{"x":"2024-04","y":98.4},{"x":"2024-05","y":98.6},{"x":"2024-06","y":98.9}]', 0.95, DATE_ADD(NOW(), INTERVAL 8 HOUR), 'completed', DATE_ADD(NOW(), INTERVAL 8 HOUR), DATE_ADD(NOW(), INTERVAL 8 HOUR));

SELECT id, forecast_type, forecast_data_json FROM qms_db.qms_forecast_result;
