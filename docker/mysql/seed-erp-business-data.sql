-- ERP 种子数据：物料/供应商/仓库
INSERT INTO erp_material (material_code, material_name, material_type, specification, unit, unit_price, safety_stock, min_stock, max_stock, status, approval_status, is_deleted, created_time, updated_time) VALUES
('M001','高强度钢板','raw_material','Q345B 10mm','张',580.00,100,50,500,1,'approved',0,NOW(),NOW()),
('M002','铝合金型材','raw_material','6063-T5 40x40','根',45.50,200,100,1000,1,'approved',0,NOW(),NOW()),
('M003','伺服电机','component','750W 220V','台',1280.00,20,10,100,1,'approved',0,NOW(),NOW()),
('M004','PLC控制器','component','S7-1200 CPU1214C','台',2350.00,15,5,50,1,'approved',0,NOW(),NOW()),
('M005','智能装配成品','finished_goods','IA-2000 标准型','台',15800.00,10,5,80,1,'approved',0,NOW(),NOW()),
('M006','包装纸箱','auxiliary','600x400x400','个',3.20,500,200,5000,1,'approved',0,NOW(),NOW());

INSERT INTO erp_supplier (supplier_code, supplier_name, supplier_type, contact_person, contact_phone, email, address, rating, status, is_deleted, created_time, updated_time) VALUES
('SUP001','上海钢材贸易有限公司','原材料','张伟','13800001001','zhangwei@shsteel.com','上海市宝山区蕰川路5000号','A',1,0,NOW(),NOW()),
('SUP002','苏州铝业集团','原材料','李娜','13800001002','lina@szalu.com','江苏省苏州市工业园区金鸡湖大道88号','A',1,0,NOW(),NOW()),
('SUP003','深圳电机制造厂','零部件','王强','13800001003','wangqiang@szenm.com','广东省深圳市宝安区新湖路200号','B',1,0,NOW(),NOW()),
('SUP004','北京自动化设备有限公司','设备','赵敏','13800001004','zhaomin@bjauto.com','北京市海淀区中关村南大街32号','A',1,0,NOW(),NOW()),
('SUP005','广州包装材料厂','辅料','陈杰','13800001005','chenjie@gzpack.com','广东省广州市白云区机场路1500号','B',1,0,NOW(),NOW());

INSERT INTO erp_warehouse (warehouse_code, warehouse_name, warehouse_type, location, manager, status, is_deleted, created_time, updated_time) VALUES
('WH001','原材料一号仓','raw_material','上海市嘉定区工业园A区1号库','刘建国',1,0,NOW(),NOW()),
('WH002','半成品周转仓','semi_finished','上海市嘉定区工业园A区2号库','孙丽华',1,0,NOW(),NOW()),
('WH003','成品仓','finished_goods','上海市嘉定区工业园B区1号库','周志强',1,0,NOW(),NOW()),
('WH004','辅料仓','auxiliary','上海市嘉定区工业园A区3号库','吴桂芳',1,0,NOW(),NOW());

-- 财务凭证：覆盖各状态（draft/submitted/approved/posted/rejected）
INSERT INTO erp_voucher (voucher_no, voucher_date, voucher_type, summary, debit_total, credit_total, status, attachment_count, created_by, updated_by, is_deleted, created_time, updated_time) VALUES
('JZ-202607-0001','2026-07-05 09:30:00','journal','支付7月原材料采购款-上海钢材',580000.00,580000.00,'posted',3,'admin','admin',0,NOW(),NOW()),
('JZ-202607-0002','2026-07-08 14:20:00','journal','收到客户回款-华东区',1250000.00,1250000.00,'approved',2,'admin','admin',0,NOW(),NOW()),
('SK-202607-0003','2026-07-12 10:05:00','receipt','销售智能装配产品收款',948000.00,948000.00,'approved',1,'admin','admin',0,NOW(),NOW()),
('FK-202607-0004','2026-07-15 16:40:00','payment','支付设备采购定金-北京自动化',235000.00,235000.00,'submitted',1,'admin','admin',0,NOW(),NOW()),
('JZ-202607-0005','2026-07-18 11:15:00','journal','计提7月生产人工成本',368000.00,368000.00,'draft',0,'admin','admin',0,NOW(),NOW()),
('ZZ-202607-0006','2026-07-20 15:30:00','transfer','原材料仓转半成品仓内部结转',156000.00,156000.00,'draft',0,'admin','admin',0,NOW(),NOW()),
('JZ-202607-0007','2026-07-22 09:00:00','journal','报销差旅费-销售部',12800.00,12800.00,'rejected',2,'admin','财务经理',0,NOW(),NOW()),
('JZ-202606-0008','2026-06-28 17:00:00','journal','6月水电费分摊',45600.00,45600.00,'posted',1,'admin','admin',0,NOW(),NOW());

-- 生产订单：production_status 0=草稿 1=已审核 2=生产中 3=已完成 5=已取消
INSERT INTO erp_production (production_no, product_code, product_name, production_quantity, completed_quantity, workshop, production_line, plan_start_time, plan_end_time, production_status, priority, remark, created_by, updated_by, is_deleted, created_time, updated_time) VALUES
('PO-202607-0001','M005','智能装配成品',50,0,'一号车间','A线','2026-07-25 08:00:00','2026-08-05 18:00:00',0,1,'Q3客户订单备产','admin','admin',0,NOW(),NOW()),
('PO-202607-0002','M005','智能装配成品',30,0,'一号车间','B线','2026-07-20 08:00:00','2026-07-30 18:00:00',1,2,NULL,'admin','admin',0,NOW(),NOW()),
('PO-202607-0003','M003','伺服电机',200,86,'二号车间','C线','2026-07-10 08:00:00','2026-07-28 18:00:00',2,2,NULL,'admin','admin',0,NOW(),NOW()),
('PO-202607-0004','M005','智能装配成品',80,80,'一号车间','A线','2026-07-01 08:00:00','2026-07-15 18:00:00',3,1,'已按期交付','admin','admin',0,NOW(),NOW()),
('PO-202607-0005','M004','PLC控制器',100,0,'三号车间','D线','2026-07-15 08:00:00','2026-07-25 18:00:00',5,3,'客户取消订单','admin','admin',0,NOW(),NOW());

-- 供应链单据：business_type 1=采购入库 2=销售出库 3=调拨
INSERT INTO erp_supply_chain (sc_no, business_type, material_code, material_name, quantity, unit, unit_price, total_amount, warehouse_code, status, created_by, updated_by, is_deleted, created_time, updated_time) VALUES
('SC-202607-0001',1,'M001','高强度钢板',300,'张',580.00,174000.00,'WH001',1,'admin','admin',0,'2026-07-03 10:00:00','2026-07-03 10:00:00'),
('SC-202607-0002',1,'M002','铝合金型材',500,'根',45.50,22750.00,'WH001',1,'admin','admin',0,'2026-07-06 14:00:00','2026-07-06 14:00:00'),
('SC-202607-0003',2,'M005','智能装配成品',40,'台',15800.00,632000.00,'WH003',1,'admin','admin',0,'2026-07-12 09:00:00','2026-07-12 09:00:00'),
('SC-202607-0004',2,'M005','智能装配成品',20,'台',15800.00,316000.00,'WH003',1,'admin','admin',0,'2026-07-16 15:00:00','2026-07-16 15:00:00'),
('SC-202607-0005',1,'M003','伺服电机',150,'台',1280.00,192000.00,'WH001',1,'admin','admin',0,'2026-07-08 11:00:00','2026-07-08 11:00:00'),
('SC-202607-0006',3,'M001','高强度钢板',100,'张',580.00,58000.00,'WH002',1,'admin','admin',0,'2026-07-20 10:00:00','2026-07-20 10:00:00'),
('SC-202607-0007',1,'M006','包装纸箱',2000,'个',3.20,6400.00,'WH004',1,'admin','admin',0,'2026-07-22 09:00:00','2026-07-22 09:00:00'),
('SC-202607-0008',2,'M003','伺服电机',60,'台',1280.00,76800.00,'WH003',1,'admin','admin',0,'2026-07-25 14:00:00','2026-07-25 14:00:00');
