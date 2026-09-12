USE erp_db;

DELIMITER //
DROP TRIGGER IF EXISTS trg_erp_supplier_write_gate_ins//
CREATE TRIGGER trg_erp_supplier_write_gate_ins
BEFORE INSERT ON erp_supplier
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'erp_app@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'erp_supplier is read-only (owner=SRM)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_erp_supplier_write_gate_upd//
CREATE TRIGGER trg_erp_supplier_write_gate_upd
BEFORE UPDATE ON erp_supplier
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'erp_app@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'erp_supplier is read-only (owner=SRM)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_erp_supplier_write_gate_del//
CREATE TRIGGER trg_erp_supplier_write_gate_del
BEFORE DELETE ON erp_supplier
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'erp_app@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'erp_supplier is read-only (owner=SRM)';
  END IF;
END//
DELIMITER ;
