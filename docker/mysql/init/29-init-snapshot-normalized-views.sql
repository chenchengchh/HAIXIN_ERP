USE scm_db;

CREATE OR REPLACE VIEW vw_scm_purchase_order_snapshot AS
SELECT
  id,
  order_no,
  supplier_id,
  supplier_code,
  supplier_name AS supplier_name_snapshot,
  purchase_type,
  order_status,
  order_amount,
  expected_delivery_date,
  actual_delivery_date,
  remark AS remark_snapshot,
  created_by,
  created_time,
  updated_by,
  updated_time
FROM scm_purchase_order;

CREATE OR REPLACE VIEW vw_scm_purchase_order_item_snapshot AS
SELECT
  id,
  order_id,
  order_no,
  material_code,
  material_name AS material_name_snapshot,
  material_spec AS material_spec_snapshot,
  unit AS unit_snapshot,
  quantity,
  unit_price,
  amount,
  received_quantity,
  remark AS remark_snapshot,
  created_by,
  created_time,
  updated_by,
  updated_time
FROM scm_purchase_order_item;

USE srm_db;

CREATE OR REPLACE VIEW vw_srm_purchase_order_snapshot AS
SELECT
  id,
  order_no,
  supplier_id,
  supplier_code,
  supplier_name AS supplier_name_snapshot,
  total_amount,
  currency,
  status,
  order_date,
  expected_delivery_date,
  remarks AS remarks_snapshot,
  created_time,
  updated_time
FROM srm_purchase_order;

CREATE OR REPLACE VIEW vw_srm_purchase_order_item_snapshot AS
SELECT
  id,
  purchase_order_id,
  material_code,
  material_name AS material_name_snapshot,
  unit AS unit_snapshot,
  quantity,
  unit_price,
  subtotal,
  received_quantity
FROM srm_purchase_order_item;

