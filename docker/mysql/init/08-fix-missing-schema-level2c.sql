USE hxcoe003;

DELIMITER //
DROP PROCEDURE IF EXISTS fix_missing_schema_level2c//
CREATE PROCEDURE fix_missing_schema_level2c()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'oa_document_category'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_document_category' AND column_name = 'sort'
    ) THEN
      ALTER TABLE oa_document_category ADD COLUMN sort INT NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'production_order_no'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN production_order_no VARCHAR(32) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'product_code'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN product_code VARCHAR(32) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'product_name'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN product_name VARCHAR(128) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'plan_quantity'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN plan_quantity INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'actual_quantity'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN actual_quantity INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'qualified_quantity'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN qualified_quantity INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'unqualified_quantity'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN unqualified_quantity INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'execution_status'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN execution_status INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'workshop'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN workshop VARCHAR(32) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'production_line'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN production_line VARCHAR(32) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'created_by'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN created_by VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'created_time'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'updated_by'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN updated_by VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'updated_time'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution' AND column_name = 'is_deleted'
    ) THEN
      ALTER TABLE mes_production_execution ADD COLUMN is_deleted INT NOT NULL DEFAULT 0;
    END IF;
  END IF;
END//
DELIMITER ;

CALL fix_missing_schema_level2c();
DROP PROCEDURE fix_missing_schema_level2c;

USE srm_db;

DELIMITER //
DROP PROCEDURE IF EXISTS fix_missing_schema_srm_level2c//
CREATE PROCEDURE fix_missing_schema_srm_level2c()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_contract'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_contract' AND column_name = 'supplier_name'
    ) THEN
      ALTER TABLE srm_contract ADD COLUMN supplier_name VARCHAR(255) NULL;
    END IF;
  END IF;
END//
DELIMITER ;

CALL fix_missing_schema_srm_level2c();
DROP PROCEDURE fix_missing_schema_srm_level2c;
