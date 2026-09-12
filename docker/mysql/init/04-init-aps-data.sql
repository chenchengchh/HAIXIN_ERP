-- 初始化化妆品行业特有的基础数据
-- 1. 资源数据（化妆品生产设备）
INSERT INTO aps_resource (name, type, capacity, status, created_time, updated_time) VALUES
('乳化锅1', '设备', 1000, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('乳化锅2', '设备', 2000, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('灌装机1', '设备', 500, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('灌装机2', '设备', 1000, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('贴标机', '设备', 1500, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('包装机', '设备', 1200, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('QC检验台', '设备', 800, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('原料仓库', '仓储', 10000, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('成品仓库', '仓储', 8000, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('生产线1', '生产线', 2000, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('生产线2', '生产线', 3000, 'available', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. 工艺数据（化妆品生产工艺）
INSERT INTO aps_process (process_name, workshop, sequence, processing_time, setup_time, teardown_time, created_time, updated_time) VALUES
('原料准备', '配料车间', 1, 30, 15, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('乳化', '乳化车间', 2, 60, 30, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('均质', '乳化车间', 3, 45, 20, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('冷却', '乳化车间', 4, 90, 10, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('灌装', '灌装车间', 5, 30, 20, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('贴标', '包装车间', 6, 20, 10, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('包装', '包装车间', 7, 25, 15, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('QC检验', '检验车间', 8, 40, 15, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('入库', '仓储车间', 9, 15, 5, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. 产品数据（化妆品产品）
INSERT INTO aps_product (product_code, product_name, category, skin_type, ingredient, production_line, created_time, updated_time) VALUES
('CP-001', '焕彩保湿精华液', '精华', '干性', '透明质酸, 维生素E, 玫瑰精油', '生产线1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CP-002', '控油平衡洁面乳', '洁面', '油性', '茶树精油, 水杨酸, 甘草提取物', '生产线1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CP-003', '抗皱紧致面霜', '面霜', '成熟', '视黄醇, 胶原蛋白, 玻尿酸', '生产线2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CP-004', '美白淡斑面膜', '面膜', '混合性', '烟酰胺, 熊果苷, 维生素C', '生产线2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CP-005', '舒缓修复乳液', '乳液', '敏感', '积雪草, 神经酰胺, 洋甘菊', '生产线1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CP-006', '防晒隔离霜', '防晒', '所有', 'SPF50+, PA++++, 二氧化钛', '生产线2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CP-007', '眼部护理精华', '眼部护理', '所有', '咖啡因, 肽类, 维生素K', '生产线1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CP-008', '身体乳', '身体护理', '所有', '乳木果油, 维生素B5, 神经酰胺', '生产线2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. 化妆品生产批号规则
INSERT INTO aps_batch_number_rule (prefix, date_format, serial_length, `separator`, is_active, created_time, updated_time) VALUES
('HX', 'yyyyMMdd', 4, '-', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('CP', 'yyMM', 5, '', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SK', 'yyyyWW', 3, '.', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 5. 算法参数配置
INSERT INTO aps_algorithm_param (algorithm_name, param_name, param_value, param_type, description, is_default, created_time, updated_time) VALUES
('geneticAlgorithm', 'populationSize', '100', 'number', '遗传算法种群大小', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('geneticAlgorithm', 'maxGenerations', '200', 'number', '遗传算法最大迭代次数', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('geneticAlgorithm', 'crossoverRate', '0.8', 'number', '遗传算法交叉概率', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('geneticAlgorithm', 'mutationRate', '0.1', 'number', '遗传算法变异概率', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('simulatedAnnealing', 'initialTemperature', '1000', 'number', '模拟退火初始温度', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('simulatedAnnealing', 'coolingRate', '0.95', 'number', '模拟退火冷却速率', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('simulatedAnnealing', 'maxIterations', '500', 'number', '模拟退火最大迭代次数', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('tabuSearch', 'tabuListSize', '20', 'number', '禁忌搜索禁忌表大小', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('tabuSearch', 'maxIterations', '300', 'number', '禁忌搜索最大迭代次数', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('tabuSearch', 'neighborhoodSize', '50', 'number', '禁忌搜索邻域大小', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 6. 资源约束数据
INSERT INTO aps_resource_constraint (resource_id, resource_name, constraint_type, capacity, available_capacity, created_time, updated_time) VALUES
(1, '乳化锅1', '能力约束', 1000, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '乳化锅2', '能力约束', 2000, 2000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '灌装机1', '能力约束', 500, 500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '灌装机2', '能力约束', 1000, 1000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, '贴标机', '能力约束', 1500, 1500, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, '包装机', '能力约束', 1200, 1200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'QC检验台', '能力约束', 800, 800, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, '原料仓库', '数量约束', 10000, 10000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, '成品仓库', '数量约束', 8000, 8000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, '生产线1', '时间约束', 24, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, '生产线2', '时间约束', 24, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 7. 生产计划示例数据
INSERT INTO aps_production_plan (plan_no, plan_name, plan_type, status, start_time, end_time, remark, created_time, updated_time) VALUES
('PP-20260101-001', '2026年1月第一周生产计划', '主生产计划', '草稿', '2026-01-01 00:00:00', '2026-01-07 23:59:59', '本周生产计划包含焕彩保湿精华液和控油平衡洁面乳', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('PP-20260101-002', '2026年1月第二周生产计划', '主生产计划', '草稿', '2026-01-08 00:00:00', '2026-01-14 23:59:59', '本周生产计划包含抗皱紧致面霜和美白淡斑面膜', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
