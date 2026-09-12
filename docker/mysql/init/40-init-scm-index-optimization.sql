-- =====================================================================
-- 40-init-scm-index-optimization.sql
-- SCM 模块数据库索引优化
-- =====================================================================

USE scm_db;

-- 采购订单表索引
CREATE INDEX IF NOT EXISTS idx_scm_po_supplier ON scm_purchase_order(supplier_id);
CREATE INDEX IF NOT EXISTS idx_scm_po_status ON scm_purchase_order(order_status);
CREATE INDEX IF NOT EXISTS idx_scm_po_created_time ON scm_purchase_order(created_time);
CREATE INDEX IF NOT EXISTS idx_scm_po_supplier_status ON scm_purchase_order(supplier_id, order_status);

-- 采购订单明细表索引
CREATE INDEX IF NOT EXISTS idx_scm_poi_order ON scm_purchase_order_item(order_id);
CREATE INDEX IF NOT EXISTS idx_scm_poi_material ON scm_purchase_order_item(material_code);

-- 供应商表索引
CREATE INDEX IF NOT EXISTS idx_scm_supplier_status ON scm_supplier(status);
CREATE INDEX IF NOT EXISTS idx_scm_supplier_level ON scm_supplier(level);

-- 发货单表索引
CREATE INDEX IF NOT EXISTS idx_scm_shipment_order ON scm_shipment(order_no);
CREATE INDEX IF NOT EXISTS idx_scm_shipment_status ON scm_shipment(status);
CREATE INDEX IF NOT EXISTS idx_scm_shipment_created ON scm_shipment(created_time);

-- 运输事件表索引
CREATE INDEX IF NOT EXISTS idx_scm_transport_shipment ON scm_transport_event(shipment_id);
CREATE INDEX IF NOT EXISTS idx_scm_transport_time ON scm_transport_event(event_time);

-- MRP计划表索引
CREATE INDEX IF NOT EXISTS idx_scm_mrp_status ON scm_mrp_plan(status);
CREATE INDEX IF NOT EXISTS idx_scm_mrp_created ON scm_mrp_plan(created_time);

-- 库存策略表索引
CREATE INDEX IF NOT EXISTS idx_scm_inv_strategy_material ON scm_inventory_strategy(material_code);
CREATE INDEX IF NOT EXISTS idx_scm_inv_strategy_warehouse ON scm_inventory_strategy(warehouse_code);

-- 集成任务表索引
CREATE INDEX IF NOT EXISTS idx_scm_task_status ON scm_integration_task(status);
CREATE INDEX IF NOT EXISTS idx_scm_task_type ON scm_integration_task(action_type);
CREATE INDEX IF NOT EXISTS idx_scm_task_created ON scm_integration_task(created_time);
