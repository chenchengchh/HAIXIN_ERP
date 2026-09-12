USE srm_db;

DELIMITER //
DROP TRIGGER IF EXISTS trg_srm_purchase_order_write_gate_ins//
CREATE TRIGGER trg_srm_purchase_order_write_gate_ins
BEFORE INSERT ON srm_purchase_order
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'srm_purchase_order is read-only (owner=SCM)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_srm_purchase_order_write_gate_upd//
CREATE TRIGGER trg_srm_purchase_order_write_gate_upd
BEFORE UPDATE ON srm_purchase_order
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'srm_purchase_order is read-only (owner=SCM)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_srm_purchase_order_write_gate_del//
CREATE TRIGGER trg_srm_purchase_order_write_gate_del
BEFORE DELETE ON srm_purchase_order
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'srm_purchase_order is read-only (owner=SCM)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_srm_purchase_order_item_write_gate_ins//
CREATE TRIGGER trg_srm_purchase_order_item_write_gate_ins
BEFORE INSERT ON srm_purchase_order_item
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'srm_purchase_order_item is read-only (owner=SCM)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_srm_purchase_order_item_write_gate_upd//
CREATE TRIGGER trg_srm_purchase_order_item_write_gate_upd
BEFORE UPDATE ON srm_purchase_order_item
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'srm_purchase_order_item is read-only (owner=SCM)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_srm_purchase_order_item_write_gate_del//
CREATE TRIGGER trg_srm_purchase_order_item_write_gate_del
BEFORE DELETE ON srm_purchase_order_item
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'srm_purchase_order_item is read-only (owner=SCM)';
  END IF;
END//
DELIMITER ;

