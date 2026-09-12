-- ========================================
-- AGV 模块基础数据脚本
-- 说明：用于 Docker MySQL 初始化阶段注入 AGV 最小可用数据
-- ========================================
USE agv_db;

INSERT INTO agv_device (
  id, agv_code, agv_name, agv_type, model, status, battery_level, voltage, temperature, speed, direction, position, current_task_id, load_status, last_update, remark
) VALUES
  (1, 'AGV001', 'AGV小车1', 'forklift', 'F1', 'running', 85, 48.00, 35.00, 1.20, 'east', 'A区-01', 'TASK001', 'loaded', NOW(), ''),
  (2, 'AGV002', 'AGV小车2', 'forklift', 'F1', 'idle', 92, 49.00, 33.00, 0.00, 'west', 'C区-03', NULL, 'empty', NOW(), ''),
  (3, 'AGV003', 'AGV小车3', 'tugger', 'T1', 'charging', 45, 42.00, 36.00, 0.00, 'north', '充电桩-01', NULL, 'empty', NOW(), '')
ON DUPLICATE KEY UPDATE
  agv_name = VALUES(agv_name),
  agv_type = VALUES(agv_type),
  model = VALUES(model),
  status = VALUES(status),
  battery_level = VALUES(battery_level),
  voltage = VALUES(voltage),
  temperature = VALUES(temperature),
  speed = VALUES(speed),
  direction = VALUES(direction),
  position = VALUES(position),
  current_task_id = VALUES(current_task_id),
  load_status = VALUES(load_status),
  last_update = NOW(),
  remark = VALUES(remark);

INSERT INTO agv_task (
  id, task_id, task_no, task_type, priority, status, agv_code, start_point, end_point, payload, track_status, progress, estimated_completion, create_time, start_time, end_time, remark
) VALUES
  (1, 'TASK001', 'TASK001', 'move', 5, 'running', 'AGV001', 'A区-01', 'B区-05', '{}', 'executing', 30, NULL, NOW(), NOW(), NULL, ''),
  (2, 'TASK010', 'TASK010', 'move', 3, 'pending', NULL, 'A区-01', 'B区-05', '{}', 'waiting', 0, NULL, NOW(), NULL, NULL, '')
ON DUPLICATE KEY UPDATE
  task_no = VALUES(task_no),
  task_type = VALUES(task_type),
  priority = VALUES(priority),
  status = VALUES(status),
  agv_code = VALUES(agv_code),
  start_point = VALUES(start_point),
  end_point = VALUES(end_point),
  payload = VALUES(payload),
  track_status = VALUES(track_status),
  progress = VALUES(progress),
  estimated_completion = VALUES(estimated_completion),
  start_time = VALUES(start_time),
  end_time = VALUES(end_time),
  remark = VALUES(remark);

INSERT INTO agv_task_assignment (id, task_id, agv_code, assigned_time, assigned_by, assignment_status)
VALUES
  (1, 'TASK001', 'AGV001', NOW(), 'system', 'assigned')
ON DUPLICATE KEY UPDATE
  assigned_time = NOW(),
  assigned_by = VALUES(assigned_by),
  assignment_status = VALUES(assignment_status);

INSERT INTO agv_path_plan (
  id, plan_id, task_id, agv_code, start_point, end_point, distance, estimated_time, algorithm, status, create_time
) VALUES
  (1, 'PLAN001', 'TASK010', 'AGV001', 'A区-01', 'B区-05', 125.6000, 2.3000, 'A*', 'executing', NOW())
ON DUPLICATE KEY UPDATE
  task_id = VALUES(task_id),
  agv_code = VALUES(agv_code),
  start_point = VALUES(start_point),
  end_point = VALUES(end_point),
  distance = VALUES(distance),
  estimated_time = VALUES(estimated_time),
  algorithm = VALUES(algorithm),
  status = VALUES(status);

INSERT INTO agv_path_point (id, plan_id, seq_no, node_code, x, y, heading, speed_limit, remark)
VALUES
  (1, 'PLAN001', 1, 'A区-01', 0.000000, 0.000000, 0.000000, 1.20, ''),
  (2, 'PLAN001', 2, 'B区-05', 10.000000, 5.000000, 0.000000, 1.20, '')
ON DUPLICATE KEY UPDATE
  node_code = VALUES(node_code),
  x = VALUES(x),
  y = VALUES(y),
  heading = VALUES(heading),
  speed_limit = VALUES(speed_limit),
  remark = VALUES(remark);

INSERT INTO agv_traffic_node (id, node_code, node_name, area_code, status, locked_by_agv_code, locked_time)
VALUES
  (1, 'NODE-001', '交通节点-001', 'AREA-001', 'free', NULL, NULL),
  (2, 'NODE-002', '交通节点-002', 'AREA-001', 'locked', 'AGV001', NOW())
ON DUPLICATE KEY UPDATE
  node_name = VALUES(node_name),
  area_code = VALUES(area_code),
  status = VALUES(status),
  locked_by_agv_code = VALUES(locked_by_agv_code),
  locked_time = VALUES(locked_time);

INSERT INTO agv_traffic_lock (id, node_code, agv_code, lock_status, lock_time, release_time, remark)
VALUES
  (1, 'NODE-002', 'AGV001', 'locked', NOW(), NULL, '')
ON DUPLICATE KEY UPDATE
  agv_code = VALUES(agv_code),
  lock_status = VALUES(lock_status),
  lock_time = NOW(),
  release_time = VALUES(release_time),
  remark = VALUES(remark);

INSERT INTO agv_traffic_rule (id, rule_name, rule_type, priority, enabled, condition_json, action_json)
VALUES
  (1, '默认交通规则', 'mutex', 1, 1, '{"scope":"node"}', '{"action":"lock"}')
ON DUPLICATE KEY UPDATE
  rule_type = VALUES(rule_type),
  priority = VALUES(priority),
  enabled = VALUES(enabled),
  condition_json = VALUES(condition_json),
  action_json = VALUES(action_json);

INSERT INTO agv_fault_alert (id, alert_level, alert_type, agv_code, title, message, status, create_time)
VALUES
  (1, 'warning', 'battery_low', 'AGV003', '电量低', '电池电量低于阈值', 'pending', NOW())
ON DUPLICATE KEY UPDATE
  alert_level = VALUES(alert_level),
  alert_type = VALUES(alert_type),
  title = VALUES(title),
  message = VALUES(message),
  status = VALUES(status);

INSERT INTO agv_collaboration_strategy (id, strategy_id, strategy_name, description, strategy_config_json, status)
VALUES
  (1, 'STRATEGY-001', '默认协同策略', '默认策略（示例）', '{"mode":"balanced"}', 'active')
ON DUPLICATE KEY UPDATE
  strategy_name = VALUES(strategy_name),
  description = VALUES(description),
  strategy_config_json = VALUES(strategy_config_json),
  status = VALUES(status);

INSERT INTO agv_collision_avoidance_config (id, config_key, config_value)
VALUES
  (1, 'global', '{"minDistance":1.0,"maxSpeed":1.2}')
ON DUPLICATE KEY UPDATE
  config_value = VALUES(config_value);

INSERT INTO agv_collaboration_log (id, log_code, strategy_id, agv_code, content, create_time)
VALUES
  (1, 'LOG-001', 'STRATEGY-001', 'AGV001', '协同调度示例日志', NOW())
ON DUPLICATE KEY UPDATE
  content = VALUES(content);

INSERT INTO agv_area (id, area_id, area_name, area_type, polygon_json, status)
VALUES
  (1, 'AREA-001', '默认区域', 'warehouse', '{"points":[{"x":0,"y":0},{"x":10,"y":0},{"x":10,"y":10},{"x":0,"y":10}]}', 'active')
ON DUPLICATE KEY UPDATE
  area_name = VALUES(area_name),
  area_type = VALUES(area_type),
  polygon_json = VALUES(polygon_json),
  status = VALUES(status);
