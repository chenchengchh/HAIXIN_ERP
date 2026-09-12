USE scm_db;

DELIMITER //
DROP TRIGGER IF EXISTS trg_scm_supplier_write_gate_ins//
CREATE TRIGGER trg_scm_supplier_write_gate_ins
BEFORE INSERT ON scm_supplier
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'scm_app@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'scm_supplier is read-only (owner=SRM)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_scm_supplier_write_gate_upd//
CREATE TRIGGER trg_scm_supplier_write_gate_upd
BEFORE UPDATE ON scm_supplier
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'scm_app@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'scm_supplier is read-only (owner=SRM)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_scm_supplier_write_gate_del//
CREATE TRIGGER trg_scm_supplier_write_gate_del
BEFORE DELETE ON scm_supplier
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'scm_app@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'scm_supplier is read-only (owner=SRM)';
  END IF;
END//
DELIMITER ;

