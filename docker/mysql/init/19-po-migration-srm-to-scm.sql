USE scm_db;

INSERT INTO scm_purchase_order (
  order_no, supplier_id, supplier_code, supplier_name,
  purchase_type, order_status, order_amount,
  expected_delivery_date, actual_delivery_date,
  created_by, created_time, updated_by, updated_time, remark
)
SELECT
  s.order_no,
  COALESCE(s.supplier_id, 0),
  COALESCE(s.supplier_code, ''),
  COALESCE(s.supplier_name, ''),
  0,
  CASE UPPER(COALESCE(s.status, 'CREATED'))
    WHEN 'CREATED' THEN 10
    WHEN 'SUBMITTED' THEN 20
    WHEN 'APPROVED' THEN 30
    WHEN 'CONFIRMED' THEN 40
    WHEN 'PARTIALLY_RECEIVED' THEN 50
    WHEN 'PARTIAL_RECEIVED' THEN 50
    WHEN 'QC_HOLD' THEN 58
    WHEN 'HOLD' THEN 58
    WHEN 'COMPLETED' THEN 60
    WHEN 'RECEIVED' THEN 60
    WHEN 'CLOSED' THEN 70
    WHEN 'CANCELLED' THEN 90
    ELSE 10
  END,
  COALESCE(s.total_amount, 0),
  s.expected_delivery_date,
  NULL,
  'system-migration',
  COALESCE(s.created_time, NOW()),
  'system-migration',
  COALESCE(s.updated_time, NOW()),
  CONCAT('migrated_from_srm;', COALESCE(s.remarks, ''))
FROM srm_db.srm_purchase_order s
LEFT JOIN scm_purchase_order p ON p.order_no = s.order_no
WHERE p.id IS NULL;

DELETE i
FROM scm_purchase_order_item i
JOIN scm_purchase_order p ON p.id = i.order_id
WHERE p.created_by = 'system-migration' AND p.remark LIKE 'migrated_from_srm;%';

INSERT INTO scm_purchase_order_item (
  order_id, order_no,
  material_code, material_name, material_spec, unit,
  quantity, unit_price, amount, received_quantity,
  created_by, created_time, updated_by, updated_time, remark
)
SELECT
  p.id,
  p.order_no,
  si.material_code,
  COALESCE(si.material_name, ''),
  NULL,
  si.unit,
  COALESCE(si.quantity, 0),
  COALESCE(si.unit_price, 0),
  COALESCE(si.subtotal, 0),
  COALESCE(si.received_quantity, 0),
  'system-migration',
  NOW(),
  'system-migration',
  NOW(),
  'migrated_from_srm'
FROM srm_db.srm_purchase_order_item si
JOIN srm_db.srm_purchase_order s ON s.id = si.purchase_order_id
JOIN scm_purchase_order p ON p.order_no = s.order_no
WHERE p.created_by = 'system-migration' AND p.remark LIKE 'migrated_from_srm;%';
