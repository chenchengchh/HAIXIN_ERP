USE hxcoe003;

DELIMITER //
DROP PROCEDURE IF EXISTS fix_missing_schema_level2b//
CREATE PROCEDURE fix_missing_schema_level2b()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'oa_document_category'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_document_category' AND column_name = 'parent_name'
    ) THEN
      ALTER TABLE oa_document_category ADD COLUMN parent_name VARCHAR(100) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_nc_review'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_review' AND column_name = 'review_team_json'
    ) THEN
      ALTER TABLE qms_nc_review ADD COLUMN review_team_json LONGTEXT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_review' AND column_name = 'review_date'
    ) THEN
      ALTER TABLE qms_nc_review ADD COLUMN review_date DATE NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_review' AND column_name = 'review_opinion'
    ) THEN
      ALTER TABLE qms_nc_review ADD COLUMN review_opinion VARCHAR(1000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_review' AND column_name = 'disposal_plan'
    ) THEN
      ALTER TABLE qms_nc_review ADD COLUMN disposal_plan VARCHAR(32) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_review' AND column_name = 'review_status'
    ) THEN
      ALTER TABLE qms_nc_review ADD COLUMN review_status VARCHAR(16) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_nc_disposal'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_disposal' AND column_name = 'review_id'
    ) THEN
      ALTER TABLE qms_nc_disposal ADD COLUMN review_id BIGINT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_disposal' AND column_name = 'disposal_plan'
    ) THEN
      ALTER TABLE qms_nc_disposal ADD COLUMN disposal_plan VARCHAR(32) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_disposal' AND column_name = 'disposal_description'
    ) THEN
      ALTER TABLE qms_nc_disposal ADD COLUMN disposal_description VARCHAR(1000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_disposal' AND column_name = 'handler'
    ) THEN
      ALTER TABLE qms_nc_disposal ADD COLUMN handler VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_disposal' AND column_name = 'start_time'
    ) THEN
      ALTER TABLE qms_nc_disposal ADD COLUMN start_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_disposal' AND column_name = 'end_time'
    ) THEN
      ALTER TABLE qms_nc_disposal ADD COLUMN end_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_disposal' AND column_name = 'disposal_status'
    ) THEN
      ALTER TABLE qms_nc_disposal ADD COLUMN disposal_status VARCHAR(16) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_disposal' AND column_name = 'process_result'
    ) THEN
      ALTER TABLE qms_nc_disposal ADD COLUMN process_result VARCHAR(1000) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_nc_tracking'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_tracking' AND column_name = 'disposal_id'
    ) THEN
      ALTER TABLE qms_nc_tracking ADD COLUMN disposal_id BIGINT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_tracking' AND column_name = 'disposal_plan'
    ) THEN
      ALTER TABLE qms_nc_tracking ADD COLUMN disposal_plan VARCHAR(32) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_tracking' AND column_name = 'disposal_status'
    ) THEN
      ALTER TABLE qms_nc_tracking ADD COLUMN disposal_status VARCHAR(16) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_tracking' AND column_name = 'tracking_content'
    ) THEN
      ALTER TABLE qms_nc_tracking ADD COLUMN tracking_content VARCHAR(1000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_tracking' AND column_name = 'tracking_status'
    ) THEN
      ALTER TABLE qms_nc_tracking ADD COLUMN tracking_status VARCHAR(16) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_tracking' AND column_name = 'effectiveness'
    ) THEN
      ALTER TABLE qms_nc_tracking ADD COLUMN effectiveness VARCHAR(16) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_nc_tracking' AND column_name = 'improvement_suggestions'
    ) THEN
      ALTER TABLE qms_nc_tracking ADD COLUMN improvement_suggestions VARCHAR(1000) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis' AND column_name = 'analysis_team_json'
    ) THEN
      ALTER TABLE qms_anomaly_analysis ADD COLUMN analysis_team_json LONGTEXT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis' AND column_name = 'analysis_date'
    ) THEN
      ALTER TABLE qms_anomaly_analysis ADD COLUMN analysis_date DATE NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis' AND column_name = 'man_factor'
    ) THEN
      ALTER TABLE qms_anomaly_analysis ADD COLUMN man_factor VARCHAR(1000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis' AND column_name = 'machine_factor'
    ) THEN
      ALTER TABLE qms_anomaly_analysis ADD COLUMN machine_factor VARCHAR(1000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis' AND column_name = 'material_factor'
    ) THEN
      ALTER TABLE qms_anomaly_analysis ADD COLUMN material_factor VARCHAR(1000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis' AND column_name = 'method_factor'
    ) THEN
      ALTER TABLE qms_anomaly_analysis ADD COLUMN method_factor VARCHAR(1000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis' AND column_name = 'environment_factor'
    ) THEN
      ALTER TABLE qms_anomaly_analysis ADD COLUMN environment_factor VARCHAR(1000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis' AND column_name = 'measurement_factor'
    ) THEN
      ALTER TABLE qms_anomaly_analysis ADD COLUMN measurement_factor VARCHAR(1000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis' AND column_name = 'analysis_status'
    ) THEN
      ALTER TABLE qms_anomaly_analysis ADD COLUMN analysis_status VARCHAR(16) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis' AND column_name = 'analyzer'
    ) THEN
      ALTER TABLE qms_anomaly_analysis ADD COLUMN analyzer VARCHAR(64) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal' AND column_name = 'disposal_plan'
    ) THEN
      ALTER TABLE qms_anomaly_disposal ADD COLUMN disposal_plan VARCHAR(2000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal' AND column_name = 'responsible_person'
    ) THEN
      ALTER TABLE qms_anomaly_disposal ADD COLUMN responsible_person VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal' AND column_name = 'start_date'
    ) THEN
      ALTER TABLE qms_anomaly_disposal ADD COLUMN start_date DATE NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal' AND column_name = 'deadline'
    ) THEN
      ALTER TABLE qms_anomaly_disposal ADD COLUMN deadline DATE NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal' AND column_name = 'actual_completion_date'
    ) THEN
      ALTER TABLE qms_anomaly_disposal ADD COLUMN actual_completion_date DATE NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal' AND column_name = 'disposal_status'
    ) THEN
      ALTER TABLE qms_anomaly_disposal ADD COLUMN disposal_status VARCHAR(16) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal' AND column_name = 'disposal_result'
    ) THEN
      ALTER TABLE qms_anomaly_disposal ADD COLUMN disposal_result VARCHAR(2000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal' AND column_name = 'verifier'
    ) THEN
      ALTER TABLE qms_anomaly_disposal ADD COLUMN verifier VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal' AND column_name = 'verification_time'
    ) THEN
      ALTER TABLE qms_anomaly_disposal ADD COLUMN verification_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal' AND column_name = 'verification_result'
    ) THEN
      ALTER TABLE qms_anomaly_disposal ADD COLUMN verification_result VARCHAR(16) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_data'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_data' AND column_name = 'equipment_id'
    ) THEN
      ALTER TABLE mes_equipment_data ADD COLUMN equipment_id VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_data' AND column_name = 'equipment_type'
    ) THEN
      ALTER TABLE mes_equipment_data ADD COLUMN equipment_type VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_data' AND column_name = 'parameter_name'
    ) THEN
      ALTER TABLE mes_equipment_data ADD COLUMN parameter_name VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_data' AND column_name = 'parameter_value'
    ) THEN
      ALTER TABLE mes_equipment_data ADD COLUMN parameter_value DECIMAL(18,6) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_data' AND column_name = 'unit'
    ) THEN
      ALTER TABLE mes_equipment_data ADD COLUMN unit VARCHAR(16) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_data' AND column_name = 'timestamp'
    ) THEN
      ALTER TABLE mes_equipment_data ADD COLUMN timestamp DATETIME NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_fault'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_fault' AND column_name = 'equipment_id'
    ) THEN
      ALTER TABLE mes_equipment_fault ADD COLUMN equipment_id VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_fault' AND column_name = 'fault_type'
    ) THEN
      ALTER TABLE mes_equipment_fault ADD COLUMN fault_type VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_fault' AND column_name = 'fault_description'
    ) THEN
      ALTER TABLE mes_equipment_fault ADD COLUMN fault_description VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_fault' AND column_name = 'occur_time'
    ) THEN
      ALTER TABLE mes_equipment_fault ADD COLUMN occur_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_fault' AND column_name = 'repair_time'
    ) THEN
      ALTER TABLE mes_equipment_fault ADD COLUMN repair_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_fault' AND column_name = 'repair_person'
    ) THEN
      ALTER TABLE mes_equipment_fault ADD COLUMN repair_person VARCHAR(64) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_production_report'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'step_name'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN step_name VARCHAR(128) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'workstation_name'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN workstation_name VARCHAR(128) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'start_time'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN start_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'end_time'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN end_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'good_qty'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN good_qty INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'scrap_qty'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN scrap_qty INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'rework_qty'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN rework_qty INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'working_hours'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN working_hours DOUBLE NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'machine_hours'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN machine_hours DOUBLE NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'remark'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN remark VARCHAR(1000) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'mes_production_report' AND column_name = 'update_time'
    ) THEN
      ALTER TABLE mes_production_report ADD COLUMN update_time DATETIME NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_supplier'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_supplier' AND column_name = 'email'
    ) THEN
      ALTER TABLE erp_supplier ADD COLUMN email VARCHAR(128) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_supplier' AND column_name = 'rating'
    ) THEN
      ALTER TABLE erp_supplier ADD COLUMN rating VARCHAR(8) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_warehouse'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_warehouse' AND column_name = 'location'
    ) THEN
      ALTER TABLE erp_warehouse ADD COLUMN location VARCHAR(255) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_finance'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_finance' AND column_name = 'transaction_type'
    ) THEN
      ALTER TABLE erp_finance ADD COLUMN transaction_type INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_finance' AND column_name = 'tax_rate'
    ) THEN
      ALTER TABLE erp_finance ADD COLUMN tax_rate DECIMAL(5,4) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_finance' AND column_name = 'transaction_date'
    ) THEN
      ALTER TABLE erp_finance ADD COLUMN transaction_date DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_finance' AND column_name = 'description'
    ) THEN
      ALTER TABLE erp_finance ADD COLUMN description VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_finance' AND column_name = 'created_by'
    ) THEN
      ALTER TABLE erp_finance ADD COLUMN created_by VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_finance' AND column_name = 'updated_by'
    ) THEN
      ALTER TABLE erp_finance ADD COLUMN updated_by VARCHAR(64) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_supply_chain'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_supply_chain' AND column_name = 'material_name'
    ) THEN
      ALTER TABLE erp_supply_chain ADD COLUMN material_name VARCHAR(128) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_supply_chain' AND column_name = 'unit'
    ) THEN
      ALTER TABLE erp_supply_chain ADD COLUMN unit VARCHAR(16) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_supply_chain' AND column_name = 'unit_price'
    ) THEN
      ALTER TABLE erp_supply_chain ADD COLUMN unit_price DECIMAL(18,6) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_supply_chain' AND column_name = 'total_amount'
    ) THEN
      ALTER TABLE erp_supply_chain ADD COLUMN total_amount DECIMAL(18,2) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_supply_chain' AND column_name = 'created_by'
    ) THEN
      ALTER TABLE erp_supply_chain ADD COLUMN created_by VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_supply_chain' AND column_name = 'updated_by'
    ) THEN
      ALTER TABLE erp_supply_chain ADD COLUMN updated_by VARCHAR(64) NULL;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_production'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'product_code'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN product_code VARCHAR(32) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'product_name'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN product_name VARCHAR(128) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'production_quantity'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN production_quantity INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'completed_quantity'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN completed_quantity INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'production_status'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN production_status INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'workshop'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN workshop VARCHAR(32) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'production_line'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN production_line VARCHAR(32) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'plan_start_time'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN plan_start_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'plan_end_time'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN plan_end_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'actual_start_time'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN actual_start_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'actual_end_time'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN actual_end_time DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'created_by'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN created_by VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'updated_by'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN updated_by VARCHAR(64) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'erp_production' AND column_name = 'is_deleted'
    ) THEN
      ALTER TABLE erp_production ADD COLUMN is_deleted INT NOT NULL DEFAULT 0;
    END IF;
  END IF;
END//
DELIMITER ;

CALL fix_missing_schema_level2b();
DROP PROCEDURE fix_missing_schema_level2b;

USE srm_db;

DELIMITER //
DROP PROCEDURE IF EXISTS fix_missing_schema_srm_level2b//
CREATE PROCEDURE fix_missing_schema_srm_level2b()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_material'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material' AND column_name = 'name'
    ) THEN
      ALTER TABLE srm_material ADD COLUMN name VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material' AND column_name = 'specification'
    ) THEN
      ALTER TABLE srm_material ADD COLUMN specification VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material' AND column_name = 'category_name'
    ) THEN
      ALTER TABLE srm_material ADD COLUMN category_name VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material' AND column_name = 'price'
    ) THEN
      ALTER TABLE srm_material ADD COLUMN price DECIMAL(18,2) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material' AND column_name = 'created_time'
    ) THEN
      ALTER TABLE srm_material ADD COLUMN created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material' AND column_name = 'updated_time'
    ) THEN
      ALTER TABLE srm_material ADD COLUMN updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_contract'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_contract' AND column_name = 'title'
    ) THEN
      ALTER TABLE srm_contract ADD COLUMN title VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_contract' AND column_name = 'total_amount'
    ) THEN
      ALTER TABLE srm_contract ADD COLUMN total_amount DECIMAL(18,2) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_contract' AND column_name = 'signed_by'
    ) THEN
      ALTER TABLE srm_contract ADD COLUMN signed_by VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_contract' AND column_name = 'signed_date'
    ) THEN
      ALTER TABLE srm_contract ADD COLUMN signed_date DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_contract' AND column_name = 'created_time'
    ) THEN
      ALTER TABLE srm_contract ADD COLUMN created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_contract' AND column_name = 'updated_time'
    ) THEN
      ALTER TABLE srm_contract ADD COLUMN updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_reconciliation'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_reconciliation' AND column_name = 'reconciliation_period'
    ) THEN
      ALTER TABLE srm_reconciliation ADD COLUMN reconciliation_period VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_reconciliation' AND column_name = 'start_date'
    ) THEN
      ALTER TABLE srm_reconciliation ADD COLUMN start_date DATE NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_reconciliation' AND column_name = 'end_date'
    ) THEN
      ALTER TABLE srm_reconciliation ADD COLUMN end_date DATE NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_reconciliation' AND column_name = 'confirm_status'
    ) THEN
      ALTER TABLE srm_reconciliation ADD COLUMN confirm_status VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_reconciliation' AND column_name = 'remark'
    ) THEN
      ALTER TABLE srm_reconciliation ADD COLUMN remark TEXT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_reconciliation' AND column_name = 'created_time'
    ) THEN
      ALTER TABLE srm_reconciliation ADD COLUMN created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_reconciliation' AND column_name = 'updated_time'
    ) THEN
      ALTER TABLE srm_reconciliation ADD COLUMN updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'order_id'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN order_id BIGINT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'order_no'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN order_no VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'objection_date'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN objection_date DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'material_id'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN material_id BIGINT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'material_name'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN material_name VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'objection_type'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN objection_type VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'quantity'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN quantity INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'loss_amount'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN loss_amount DECIMAL(18,2) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'processing_result'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN processing_result TEXT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'remark'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN remark TEXT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'created_time'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection' AND column_name = 'updated_time'
    ) THEN
      ALTER TABLE srm_quality_objection ADD COLUMN updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast' AND column_name = 'order_id'
    ) THEN
      ALTER TABLE srm_material_forecast ADD COLUMN order_id BIGINT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast' AND column_name = 'order_no'
    ) THEN
      ALTER TABLE srm_material_forecast ADD COLUMN order_no VARCHAR(255) NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast' AND column_name = 'expected_arrival_date'
    ) THEN
      ALTER TABLE srm_material_forecast ADD COLUMN expected_arrival_date DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast' AND column_name = 'actual_arrival_date'
    ) THEN
      ALTER TABLE srm_material_forecast ADD COLUMN actual_arrival_date DATETIME NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast' AND column_name = 'total_quantity'
    ) THEN
      ALTER TABLE srm_material_forecast ADD COLUMN total_quantity INT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast' AND column_name = 'logistics_info'
    ) THEN
      ALTER TABLE srm_material_forecast ADD COLUMN logistics_info TEXT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast' AND column_name = 'remark'
    ) THEN
      ALTER TABLE srm_material_forecast ADD COLUMN remark TEXT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast' AND column_name = 'created_time'
    ) THEN
      ALTER TABLE srm_material_forecast ADD COLUMN created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast' AND column_name = 'updated_time'
    ) THEN
      ALTER TABLE srm_material_forecast ADD COLUMN updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
  END IF;
END//
DELIMITER ;

CALL fix_missing_schema_srm_level2b();
DROP PROCEDURE fix_missing_schema_srm_level2b;
