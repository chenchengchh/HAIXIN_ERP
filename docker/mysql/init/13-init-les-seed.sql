USE les_db;

INSERT INTO les_vehicle (id, license_plate, vehicle_type, load_capacity, status, create_time)
VALUES
    (1, '粤A12345', '厢式货车', 5.00, 'available', NOW()),
    (2, '粤A67890', '冷藏车', 3.00, 'active', NOW())
ON DUPLICATE KEY UPDATE
    vehicle_type = VALUES(vehicle_type),
    load_capacity = VALUES(load_capacity),
    status = VALUES(status);

INSERT INTO les_driver (id, name, phone, license_no, status, create_time)
VALUES
    (1, '张三', '13800138001', 'A123456789', 'available', NOW()),
    (2, '李四', '13900139001', 'A987654321', 'active', NOW())
ON DUPLICATE KEY UPDATE
    phone = VALUES(phone),
    license_no = VALUES(license_no),
    status = VALUES(status);

INSERT INTO les_route (id, route_name, start_location, end_location, distance, estimated_time, create_time)
VALUES
    (1, '广州-深圳', '广州天河', '深圳福田', 130.00, 120, NOW()),
    (2, '广州-珠海', '广州白云', '珠海香洲', 150.00, 150, NOW())
ON DUPLICATE KEY UPDATE
    start_location = VALUES(start_location),
    end_location = VALUES(end_location),
    distance = VALUES(distance),
    estimated_time = VALUES(estimated_time);

INSERT INTO les_transport_plan (id, plan_no, sales_order_no, source_type, source_no, vehicle_id, driver_id, route_id, status, cost_estimated, create_time, update_time)
VALUES
    (1, 'PLAN-20251222-001', 'SO-20251221-001', 'LOCAL', 'SO-20251221-001', 1, 1, 1, 2, 500.00, NOW(), NOW()),
    (2, 'PLAN-20251222-002', 'SO-20251221-002', 'LOCAL', 'SO-20251221-002', 2, 2, 2, 1, 800.00, NOW(), NOW()),
    (3, 'PLAN-20251222-003', 'SO-20251221-003', 'LOCAL', 'SO-20251221-003', 1, 1, 1, 3, 600.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    sales_order_no = VALUES(sales_order_no),
    source_type = VALUES(source_type),
    source_no = VALUES(source_no),
    vehicle_id = VALUES(vehicle_id),
    driver_id = VALUES(driver_id),
    route_id = VALUES(route_id),
    status = VALUES(status),
    cost_estimated = VALUES(cost_estimated),
    update_time = VALUES(update_time);

INSERT INTO les_transport_task (id, task_no, plan_id, driver_id, vehicle_id, status, create_time, start_time, end_time)
VALUES
    (1, 'TASK-20251222-001', 1, 1, 1, 'in_progress', NOW(), NOW(), NULL),
    (2, 'TASK-20251222-002', 2, 2, 2, 'pending', NOW(), NULL, NULL)
ON DUPLICATE KEY UPDATE
    plan_id = VALUES(plan_id),
    driver_id = VALUES(driver_id),
    vehicle_id = VALUES(vehicle_id),
    status = VALUES(status);

INSERT INTO les_monitor_log (id, plan_id, vehicle_id, longitude, latitude, current_status, is_anomaly, record_time)
VALUES
    (1, 1, 1, 113.32452000, 23.10229000, '行驶中', 0, NOW() - INTERVAL 60 MINUTE),
    (2, 1, 1, 113.33452000, 23.11229000, '行驶中', 0, NOW() - INTERVAL 30 MINUTE),
    (3, 1, 1, 113.34452000, 23.12229000, '卸货中', 0, NOW())
ON DUPLICATE KEY UPDATE
    current_status = VALUES(current_status),
    is_anomaly = VALUES(is_anomaly),
    record_time = VALUES(record_time);

INSERT INTO les_vehicle_location (id, plan_id, vehicle_id, license_plate, longitude, latitude, speed, direction, record_time)
VALUES
    (1, 1, 1, '粤A12345', 113.32452000, 23.10229000, 60.00, 90.00, NOW() - INTERVAL 60 MINUTE),
    (2, 1, 1, '粤A12345', 113.33452000, 23.11229000, 55.00, 90.00, NOW() - INTERVAL 30 MINUTE),
    (3, 1, 1, '粤A12345', 113.34452000, 23.12229000, 20.00, 90.00, NOW())
ON DUPLICATE KEY UPDATE
    longitude = VALUES(longitude),
    latitude = VALUES(latitude),
    speed = VALUES(speed),
    direction = VALUES(direction),
    record_time = VALUES(record_time);

INSERT INTO les_anomaly_event (id, plan_id, vehicle_id, event_no, event_type, event_desc, event_description, event_time, handling_status, handling_result, create_time)
VALUES
    (1, 1, 1, 'AE-20251222-001', '延误', '车辆在高速路段长时间低速行驶', '车辆在高速路段长时间低速行驶', NOW() - INTERVAL 25 MINUTE, 'pending', '', NOW())
ON DUPLICATE KEY UPDATE
    handling_status = VALUES(handling_status),
    handling_result = VALUES(handling_result);

INSERT INTO les_sign_voucher (id, plan_id, customer_sign, photo_urls, arrival_time, on_time_status, status, create_time, update_time)
VALUES
    (1, 3, '', '', NOW() - INTERVAL 1 DAY, 1, 'signed', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    status = VALUES(status),
    update_time = VALUES(update_time);

INSERT INTO les_transport_cost (id, plan_id, fuel_cost, toll_cost, driver_salary, other_cost, total_cost, create_time)
VALUES
    (1, 1, 300.00, 100.00, 150.00, 50.00, 600.00, NOW())
ON DUPLICATE KEY UPDATE
    fuel_cost = VALUES(fuel_cost),
    toll_cost = VALUES(toll_cost),
    driver_salary = VALUES(driver_salary),
    other_cost = VALUES(other_cost),
    total_cost = VALUES(total_cost);

INSERT INTO les_service_quality (id, plan_id, sign_success_rate, cargo_integrity_rate, on_time_rate, customer_satisfaction, create_time)
VALUES
    (1, 1, 100.00, 100.00, 90.00, 4.50, NOW())
ON DUPLICATE KEY UPDATE
    sign_success_rate = VALUES(sign_success_rate),
    cargo_integrity_rate = VALUES(cargo_integrity_rate),
    on_time_rate = VALUES(on_time_rate),
    customer_satisfaction = VALUES(customer_satisfaction);
