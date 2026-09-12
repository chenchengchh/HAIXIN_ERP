USE hxcoe003;

DELIMITER //
DROP PROCEDURE IF EXISTS fix_missing_schema_level2//
CREATE PROCEDURE fix_missing_schema_level2()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'oa_document_category'
  ) THEN
    CREATE TABLE oa_document_category (
      id BIGINT NOT NULL AUTO_INCREMENT,
      name VARCHAR(100) NOT NULL,
      description VARCHAR(500) NULL,
      parent_id BIGINT NULL,
      parent_name VARCHAR(100) NULL,
      level INT NOT NULL DEFAULT 1,
      sort INT NULL,
      status INT NOT NULL DEFAULT 1,
      creator_id BIGINT NOT NULL DEFAULT 0,
      create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updater_id BIGINT NULL,
      update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      KEY idx_oa_document_category_parent_id (parent_id),
      KEY idx_oa_document_category_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  ELSE
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_document_category' AND column_name = 'creator_id'
    ) THEN
      ALTER TABLE oa_document_category ADD COLUMN creator_id BIGINT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_document_category' AND column_name = 'create_time'
    ) THEN
      ALTER TABLE oa_document_category ADD COLUMN create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_document_category' AND column_name = 'updater_id'
    ) THEN
      ALTER TABLE oa_document_category ADD COLUMN updater_id BIGINT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_document_category' AND column_name = 'update_time'
    ) THEN
      ALTER TABLE oa_document_category ADD COLUMN update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process' AND column_name = 'process_type'
    ) THEN
      ALTER TABLE oa_approval_process ADD COLUMN process_type VARCHAR(50) NOT NULL DEFAULT 'default';
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process' AND column_name = 'process_definition'
    ) THEN
      ALTER TABLE oa_approval_process ADD COLUMN process_definition TEXT NOT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process' AND column_name = 'form_config'
    ) THEN
      ALTER TABLE oa_approval_process ADD COLUMN form_config TEXT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process' AND column_name = 'erp_order_id'
    ) THEN
      ALTER TABLE oa_approval_process ADD COLUMN erp_order_id BIGINT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process' AND column_name = 'scm_supplier_id'
    ) THEN
      ALTER TABLE oa_approval_process ADD COLUMN scm_supplier_id BIGINT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process' AND column_name = 'mes_workshop_id'
    ) THEN
      ALTER TABLE oa_approval_process ADD COLUMN mes_workshop_id BIGINT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process' AND column_name = 'creator_id'
    ) THEN
      ALTER TABLE oa_approval_process ADD COLUMN creator_id BIGINT NOT NULL DEFAULT 0;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process' AND column_name = 'create_time'
    ) THEN
      ALTER TABLE oa_approval_process ADD COLUMN create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process' AND column_name = 'updater_id'
    ) THEN
      ALTER TABLE oa_approval_process ADD COLUMN updater_id BIGINT NULL;
    END IF;
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_approval_process' AND column_name = 'update_time'
    ) THEN
      ALTER TABLE oa_approval_process ADD COLUMN update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'crm_customer_tag'
  ) THEN
    CREATE TABLE crm_customer_tag (
      id BIGINT NOT NULL AUTO_INCREMENT,
      tag_name VARCHAR(64) NOT NULL,
      tag_type VARCHAR(32) NOT NULL,
      tag_category VARCHAR(32) NOT NULL,
      color VARCHAR(16) NULL,
      sort_order INT NULL,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'crm_customer_tag_relation'
  ) THEN
    CREATE TABLE crm_customer_tag_relation (
      id BIGINT NOT NULL AUTO_INCREMENT,
      customer_id BIGINT NOT NULL,
      tag_id BIGINT NOT NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      KEY idx_crm_customer_tag_relation_customer_id (customer_id),
      KEY idx_crm_customer_tag_relation_tag_id (tag_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'crm_customer_follow_up'
  ) THEN
    CREATE TABLE crm_customer_follow_up (
      id BIGINT NOT NULL AUTO_INCREMENT,
      customer_id BIGINT NOT NULL,
      follow_up_user_id BIGINT NOT NULL,
      follow_up_type VARCHAR(32) NOT NULL,
      content VARCHAR(1024) NOT NULL,
      next_plan VARCHAR(512) NULL,
      follow_up_time DATETIME NOT NULL,
      next_time DATETIME NULL,
      PRIMARY KEY (id),
      KEY idx_crm_customer_follow_up_customer_id (customer_id),
      KEY idx_crm_customer_follow_up_user_id (follow_up_user_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_inspection_standard'
  ) THEN
    CREATE TABLE qms_inspection_standard (
      id BIGINT NOT NULL AUTO_INCREMENT,
      standard_no VARCHAR(64) NOT NULL,
      material_code VARCHAR(64) NULL,
      material_name VARCHAR(128) NULL,
      version VARCHAR(32) NULL,
      aql_level VARCHAR(32) NULL,
      status VARCHAR(16) NULL,
      inspection_items_json LONGTEXT NULL,
      creator VARCHAR(64) NULL,
      created_time DATETIME NULL,
      updated_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_inspection_standard_no (standard_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_nc_review'
  ) THEN
    CREATE TABLE qms_nc_review (
      id BIGINT NOT NULL AUTO_INCREMENT,
      review_no VARCHAR(64) NOT NULL,
      registration_id BIGINT NULL,
      registration_no VARCHAR(64) NULL,
      review_result VARCHAR(16) NULL,
      review_comment VARCHAR(2000) NULL,
      reviewer VARCHAR(64) NULL,
      review_time DATETIME NULL,
      status VARCHAR(16) NULL,
      created_time DATETIME NULL,
      updated_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_nc_review_no (review_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_nc_disposal'
  ) THEN
    CREATE TABLE qms_nc_disposal (
      id BIGINT NOT NULL AUTO_INCREMENT,
      disposal_no VARCHAR(64) NOT NULL,
      registration_id BIGINT NULL,
      registration_no VARCHAR(64) NULL,
      disposal_method VARCHAR(32) NULL,
      disposal_result VARCHAR(16) NULL,
      disposal_comment VARCHAR(2000) NULL,
      disposer VARCHAR(64) NULL,
      disposal_time DATETIME NULL,
      status VARCHAR(16) NULL,
      created_time DATETIME NULL,
      updated_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_nc_disposal_no (disposal_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_nc_tracking'
  ) THEN
    CREATE TABLE qms_nc_tracking (
      id BIGINT NOT NULL AUTO_INCREMENT,
      tracking_no VARCHAR(64) NOT NULL,
      registration_id BIGINT NULL,
      registration_no VARCHAR(64) NULL,
      tracking_action VARCHAR(2000) NULL,
      tracking_result VARCHAR(16) NULL,
      tracker VARCHAR(64) NULL,
      tracking_time DATETIME NULL,
      status VARCHAR(16) NULL,
      created_time DATETIME NULL,
      updated_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_nc_tracking_no (tracking_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_nc_registration'
  ) THEN
    CREATE TABLE qms_nc_registration (
      id BIGINT NOT NULL AUTO_INCREMENT,
      registration_no VARCHAR(64) NOT NULL,
      material_code VARCHAR(64) NULL,
      material_name VARCHAR(128) NULL,
      batch_no VARCHAR(64) NULL,
      quantity DECIMAL(18,2) NULL,
      defect_type VARCHAR(32) NULL,
      defect_level VARCHAR(16) NULL,
      defect_description VARCHAR(1000) NULL,
      discovery_department VARCHAR(64) NULL,
      discovery_person VARCHAR(64) NULL,
      discovery_time DATETIME NULL,
      discovery_location VARCHAR(128) NULL,
      status VARCHAR(16) NULL,
      registrant VARCHAR(64) NULL,
      registration_time DATETIME NULL,
      review_status VARCHAR(32) NULL,
      disposal_status VARCHAR(32) NULL,
      tracking_status VARCHAR(32) NULL,
      created_time DATETIME NULL,
      updated_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_nc_registration_no (registration_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_report'
  ) THEN
    CREATE TABLE qms_anomaly_report (
      id BIGINT NOT NULL AUTO_INCREMENT,
      report_no VARCHAR(64) NOT NULL,
      title VARCHAR(200) NULL,
      anomaly_type VARCHAR(64) NULL,
      severity VARCHAR(16) NULL,
      occurrence_time DATETIME NULL,
      occurrence_location VARCHAR(128) NULL,
      description VARCHAR(2000) NULL,
      reporter VARCHAR(64) NULL,
      report_time DATETIME NULL,
      status VARCHAR(16) NULL,
      related_products_json LONGTEXT NULL,
      impact_assessment VARCHAR(2000) NULL,
      created_time DATETIME NULL,
      updated_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_anomaly_report_no (report_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_analysis'
  ) THEN
    CREATE TABLE qms_anomaly_analysis (
      id BIGINT NOT NULL AUTO_INCREMENT,
      analysis_no VARCHAR(64) NOT NULL,
      report_id BIGINT NULL,
      report_no VARCHAR(64) NULL,
      root_cause VARCHAR(2000) NULL,
      analysis_method VARCHAR(64) NULL,
      analysis_result VARCHAR(2000) NULL,
      analyst VARCHAR(64) NULL,
      analysis_time DATETIME NULL,
      status VARCHAR(16) NULL,
      created_time DATETIME NULL,
      updated_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_anomaly_analysis_no (analysis_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_anomaly_disposal'
  ) THEN
    CREATE TABLE qms_anomaly_disposal (
      id BIGINT NOT NULL AUTO_INCREMENT,
      disposal_no VARCHAR(64) NOT NULL,
      report_id BIGINT NULL,
      report_no VARCHAR(64) NULL,
      disposal_measure VARCHAR(2000) NULL,
      disposer VARCHAR(64) NULL,
      disposal_time DATETIME NULL,
      status VARCHAR(16) NULL,
      created_time DATETIME NULL,
      updated_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_anomaly_disposal_no (disposal_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_data_collection'
  ) THEN
    CREATE TABLE qms_data_collection (
      id BIGINT NOT NULL AUTO_INCREMENT,
      collection_no VARCHAR(64) NOT NULL,
      collection_name VARCHAR(128) NULL,
      collection_date DATE NULL,
      data_type VARCHAR(64) NULL,
      source VARCHAR(128) NULL,
      data_items_json LONGTEXT NULL,
      collector VARCHAR(64) NULL,
      status VARCHAR(16) NULL,
      created_time DATETIME NULL,
      updated_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_data_collection_no (collection_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_capa'
  ) THEN
    CREATE TABLE qms_capa (
      id BIGINT NOT NULL AUTO_INCREMENT,
      report_id BIGINT NULL,
      report_no VARCHAR(64) NULL,
      preventive_measure VARCHAR(2000) NULL,
      corrective_measure VARCHAR(2000) NULL,
      implementation_team_json LONGTEXT NULL,
      implementation_deadline DATE NULL,
      actual_completion_date DATE NULL,
      implementation_status VARCHAR(16) NULL,
      verify_status VARCHAR(16) NULL,
      verify_result VARCHAR(2000) NULL,
      verify_date DATE NULL,
      reviewer VARCHAR(64) NULL,
      review_date DATE NULL,
      review_result VARCHAR(16) NULL,
      created_time DATETIME NULL,
      updated_time DATETIME NULL,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_batch'
  ) THEN
    CREATE TABLE mes_batch (
      id BIGINT NOT NULL AUTO_INCREMENT,
      batch_no VARCHAR(64) NOT NULL,
      work_order_no VARCHAR(64) NULL,
      material_id VARCHAR(64) NULL,
      material_name VARCHAR(128) NULL,
      qty INT NULL,
      status VARCHAR(32) NULL,
      create_time DATETIME NULL,
      update_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_mes_batch_no (batch_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_flow_record'
  ) THEN
    CREATE TABLE mes_flow_record (
      id BIGINT NOT NULL AUTO_INCREMENT,
      sn_code VARCHAR(64) NOT NULL,
      batch_no VARCHAR(64) NULL,
      from_step_id VARCHAR(64) NULL,
      from_step_name VARCHAR(128) NULL,
      to_step_id VARCHAR(64) NULL,
      to_step_name VARCHAR(128) NULL,
      from_station_id VARCHAR(64) NULL,
      from_station_name VARCHAR(128) NULL,
      to_station_id VARCHAR(64) NULL,
      to_station_name VARCHAR(128) NULL,
      operator_id VARCHAR(64) NULL,
      operator_name VARCHAR(64) NULL,
      timestamp DATETIME NULL,
      status VARCHAR(32) NULL,
      remarks VARCHAR(255) NULL,
      PRIMARY KEY (id),
      KEY idx_mes_flow_record_sn_code (sn_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_data'
  ) THEN
    CREATE TABLE mes_equipment_data (
      id BIGINT NOT NULL AUTO_INCREMENT,
      equipment_code VARCHAR(64) NULL,
      equipment_name VARCHAR(128) NULL,
      data_type VARCHAR(64) NULL,
      data_value VARCHAR(255) NULL,
      collect_time DATETIME NULL,
      status VARCHAR(32) NULL,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_equipment_fault'
  ) THEN
    CREATE TABLE mes_equipment_fault (
      id BIGINT NOT NULL AUTO_INCREMENT,
      fault_code VARCHAR(64) NULL,
      equipment_code VARCHAR(64) NULL,
      equipment_name VARCHAR(128) NULL,
      fault_desc VARCHAR(500) NULL,
      fault_time DATETIME NULL,
      status VARCHAR(32) NULL,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_process_assignment'
  ) THEN
    CREATE TABLE mes_process_assignment (
      id BIGINT NOT NULL AUTO_INCREMENT,
      work_order_no VARCHAR(64) NULL,
      process_code VARCHAR(64) NULL,
      process_name VARCHAR(128) NULL,
      station_code VARCHAR(64) NULL,
      station_name VARCHAR(128) NULL,
      operator_id VARCHAR(64) NULL,
      operator_name VARCHAR(64) NULL,
      status VARCHAR(32) NULL,
      create_time DATETIME NULL,
      update_time DATETIME NULL,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_production_report'
  ) THEN
    CREATE TABLE mes_production_report (
      id BIGINT NOT NULL AUTO_INCREMENT,
      report_no VARCHAR(64) NULL,
      work_order_no VARCHAR(64) NULL,
      operator_name VARCHAR(64) NULL,
      status VARCHAR(32) NULL,
      create_time DATETIME NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_mes_production_report_no (report_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_manual_reporting'
  ) THEN
    CREATE TABLE mes_manual_reporting (
      id BIGINT NOT NULL AUTO_INCREMENT,
      report_no VARCHAR(64) NULL,
      work_order_no VARCHAR(64) NULL,
      operator_name VARCHAR(64) NULL,
      quantity DECIMAL(18,2) NULL,
      report_time DATETIME NULL,
      status VARCHAR(32) NULL,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_production_execution'
  ) THEN
    CREATE TABLE mes_production_execution (
      id BIGINT NOT NULL AUTO_INCREMENT,
      execution_no VARCHAR(64) NULL,
      work_order_no VARCHAR(64) NULL,
      status VARCHAR(32) NULL,
      start_time DATETIME NULL,
      end_time DATETIME NULL,
      create_time DATETIME NULL,
      update_time DATETIME NULL,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_quality_inspection'
  ) THEN
    CREATE TABLE mes_quality_inspection (
      id BIGINT NOT NULL AUTO_INCREMENT,
      inspection_no VARCHAR(64) NULL,
      work_order_no VARCHAR(64) NULL,
      status VARCHAR(32) NULL,
      inspector VARCHAR(64) NULL,
      inspection_time DATETIME NULL,
      create_time DATETIME NULL,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_organization'
  ) THEN
    CREATE TABLE erp_organization (
      id BIGINT NOT NULL AUTO_INCREMENT,
      organization_code VARCHAR(32) NOT NULL,
      organization_name VARCHAR(128) NOT NULL,
      organization_type VARCHAR(16) NOT NULL,
      parent_id BIGINT NULL,
      level INT NOT NULL DEFAULT 1,
      status INT NOT NULL DEFAULT 1,
      remark VARCHAR(255) NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      created_by VARCHAR(64) NULL,
      updated_by VARCHAR(64) NULL,
      is_deleted INT NOT NULL DEFAULT 0,
      PRIMARY KEY (id),
      UNIQUE KEY uk_erp_organization_code (organization_code),
      KEY idx_erp_organization_parent_id (parent_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_account'
  ) THEN
    CREATE TABLE erp_account (
      id BIGINT NOT NULL AUTO_INCREMENT,
      account_code VARCHAR(32) NOT NULL,
      account_name VARCHAR(64) NOT NULL,
      account_type VARCHAR(16) NOT NULL,
      balance_direction VARCHAR(8) NOT NULL,
      parent_id BIGINT NULL,
      level INT NOT NULL DEFAULT 1,
      is_leaf INT NOT NULL DEFAULT 1,
      status INT NOT NULL DEFAULT 1,
      remark VARCHAR(255) NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      created_by VARCHAR(64) NULL,
      updated_by VARCHAR(64) NULL,
      is_deleted INT NOT NULL DEFAULT 0,
      PRIMARY KEY (id),
      UNIQUE KEY uk_erp_account_code (account_code),
      KEY idx_erp_account_parent_id (parent_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_supplier'
  ) THEN
    CREATE TABLE erp_supplier (
      id BIGINT NOT NULL AUTO_INCREMENT,
      supplier_code VARCHAR(32) NOT NULL,
      supplier_name VARCHAR(128) NOT NULL,
      supplier_type VARCHAR(32) NULL,
      contact_person VARCHAR(64) NULL,
      contact_phone VARCHAR(32) NULL,
      contact_email VARCHAR(128) NULL,
      address VARCHAR(255) NULL,
      status INT NOT NULL DEFAULT 1,
      remark VARCHAR(255) NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      is_deleted INT NOT NULL DEFAULT 0,
      PRIMARY KEY (id),
      UNIQUE KEY uk_erp_supplier_code (supplier_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_warehouse'
  ) THEN
    CREATE TABLE erp_warehouse (
      id BIGINT NOT NULL AUTO_INCREMENT,
      warehouse_code VARCHAR(32) NOT NULL,
      warehouse_name VARCHAR(128) NOT NULL,
      warehouse_type VARCHAR(32) NULL,
      address VARCHAR(255) NULL,
      manager VARCHAR(64) NULL,
      phone VARCHAR(32) NULL,
      status INT NOT NULL DEFAULT 1,
      remark VARCHAR(255) NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      is_deleted INT NOT NULL DEFAULT 0,
      PRIMARY KEY (id),
      UNIQUE KEY uk_erp_warehouse_code (warehouse_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_finance'
  ) THEN
    CREATE TABLE erp_finance (
      id BIGINT NOT NULL AUTO_INCREMENT,
      finance_no VARCHAR(32) NULL,
      finance_type VARCHAR(32) NULL,
      business_type INT NULL,
      amount DECIMAL(18,2) NULL,
      currency VARCHAR(10) NULL,
      status VARCHAR(16) NULL,
      remark VARCHAR(255) NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      is_deleted INT NOT NULL DEFAULT 0,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_production'
  ) THEN
    CREATE TABLE erp_production (
      id BIGINT NOT NULL AUTO_INCREMENT,
      production_no VARCHAR(32) NULL,
      production_type VARCHAR(32) NULL,
      material_code VARCHAR(64) NULL,
      material_name VARCHAR(128) NULL,
      plan_quantity DECIMAL(18,2) NULL,
      actual_quantity DECIMAL(18,2) NULL,
      start_date DATETIME NULL,
      end_date DATETIME NULL,
      status VARCHAR(16) NULL,
      remark VARCHAR(255) NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      is_deleted INT NOT NULL DEFAULT 0,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_supply_chain'
  ) THEN
    CREATE TABLE erp_supply_chain (
      id BIGINT NOT NULL AUTO_INCREMENT,
      sc_no VARCHAR(32) NULL,
      business_type INT NULL,
      material_code VARCHAR(64) NULL,
      warehouse_code VARCHAR(64) NULL,
      quantity DECIMAL(18,2) NULL,
      status INT NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      is_deleted INT NOT NULL DEFAULT 0,
      PRIMARY KEY (id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_voucher_item'
  ) THEN
    CREATE TABLE erp_voucher_item (
      id BIGINT NOT NULL AUTO_INCREMENT,
      voucher_id BIGINT NOT NULL,
      account_code VARCHAR(32) NOT NULL,
      account_name VARCHAR(64) NULL,
      summary VARCHAR(255) NULL,
      debit_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
      credit_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      KEY idx_erp_voucher_item_voucher_id (voucher_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;
END//
DELIMITER ;

CALL fix_missing_schema_level2();
DROP PROCEDURE fix_missing_schema_level2;

USE srm_db;

DELIMITER //
DROP PROCEDURE IF EXISTS fix_missing_schema_srm_level2//
CREATE PROCEDURE fix_missing_schema_srm_level2()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_purchase_request'
  ) THEN
    CREATE TABLE srm_purchase_request (
      id BIGINT NOT NULL AUTO_INCREMENT,
      request_code VARCHAR(255) NOT NULL,
      applicant VARCHAR(255) NULL,
      department VARCHAR(255) NULL,
      apply_date DATETIME NULL,
      status VARCHAR(255) NULL,
      purchase_type VARCHAR(255) NULL,
      description VARCHAR(255) NULL,
      expected_delivery_date DATETIME NULL,
      material_code VARCHAR(255) NULL,
      material_name VARCHAR(255) NULL,
      quantity DECIMAL(18,2) NULL,
      unit VARCHAR(255) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_srm_purchase_request_code (request_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_inquiry'
  ) THEN
    CREATE TABLE srm_inquiry (
      id BIGINT NOT NULL AUTO_INCREMENT,
      inquiry_no VARCHAR(255) NOT NULL,
      title VARCHAR(255) NULL,
      type VARCHAR(255) NULL,
      status VARCHAR(255) NULL,
      start_time DATETIME NULL,
      deadline DATETIME NULL,
      remarks VARCHAR(255) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_srm_inquiry_no (inquiry_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_inquiry_item'
  ) THEN
    CREATE TABLE srm_inquiry_item (
      id BIGINT NOT NULL AUTO_INCREMENT,
      inquiry_id BIGINT NULL,
      material_code VARCHAR(255) NULL,
      material_name VARCHAR(255) NULL,
      quantity DECIMAL(18,2) NULL,
      unit VARCHAR(255) NULL,
      remarks VARCHAR(255) NULL,
      PRIMARY KEY (id),
      KEY idx_srm_inquiry_item_inquiry_id (inquiry_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_quotation'
  ) THEN
    CREATE TABLE srm_quotation (
      id BIGINT NOT NULL AUTO_INCREMENT,
      quotation_no VARCHAR(255) NOT NULL,
      inquiry_id BIGINT NULL,
      inquiry_no VARCHAR(255) NULL,
      supplier_id BIGINT NULL,
      supplier_name VARCHAR(255) NULL,
      status VARCHAR(255) NULL,
      total_amount DECIMAL(18,2) NULL,
      currency VARCHAR(32) NULL,
      remarks VARCHAR(255) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_srm_quotation_no (quotation_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_quotation_item'
  ) THEN
    CREATE TABLE srm_quotation_item (
      id BIGINT NOT NULL AUTO_INCREMENT,
      quotation_id BIGINT NULL,
      material_code VARCHAR(255) NULL,
      material_name VARCHAR(255) NULL,
      quantity DECIMAL(18,2) NULL,
      unit_price DECIMAL(18,2) NULL,
      amount DECIMAL(18,2) NULL,
      remarks VARCHAR(255) NULL,
      PRIMARY KEY (id),
      KEY idx_srm_quotation_item_quotation_id (quotation_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_contract'
  ) THEN
    CREATE TABLE srm_contract (
      id BIGINT NOT NULL AUTO_INCREMENT,
      contract_no VARCHAR(255) NOT NULL,
      contract_name VARCHAR(255) NULL,
      supplier_id BIGINT NULL,
      supplier_name VARCHAR(255) NULL,
      status VARCHAR(255) NULL,
      start_date DATETIME NULL,
      end_date DATETIME NULL,
      amount DECIMAL(18,2) NULL,
      remarks VARCHAR(255) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_srm_contract_no (contract_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_delivery_note'
  ) THEN
    CREATE TABLE srm_delivery_note (
      id BIGINT NOT NULL AUTO_INCREMENT,
      delivery_no VARCHAR(255) NOT NULL,
      purchase_order_id BIGINT NULL,
      purchase_order_no VARCHAR(255) NULL,
      supplier_id BIGINT NULL,
      supplier_name VARCHAR(255) NULL,
      status VARCHAR(255) NULL,
      delivery_time DATETIME NULL,
      remarks VARCHAR(255) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_srm_delivery_note_no (delivery_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_delivery_note_item'
  ) THEN
    CREATE TABLE srm_delivery_note_item (
      id BIGINT NOT NULL AUTO_INCREMENT,
      delivery_note_id BIGINT NULL,
      material_code VARCHAR(255) NULL,
      material_name VARCHAR(255) NULL,
      quantity DECIMAL(18,2) NULL,
      unit VARCHAR(255) NULL,
      remarks VARCHAR(255) NULL,
      PRIMARY KEY (id),
      KEY idx_srm_delivery_note_item_delivery_note_id (delivery_note_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_reconciliation'
  ) THEN
    CREATE TABLE srm_reconciliation (
      id BIGINT NOT NULL AUTO_INCREMENT,
      reconciliation_no VARCHAR(255) NOT NULL,
      supplier_id BIGINT NULL,
      supplier_name VARCHAR(255) NULL,
      status VARCHAR(255) NULL,
      total_amount DECIMAL(18,2) NULL,
      remarks VARCHAR(255) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_srm_reconciliation_no (reconciliation_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_quality_objection'
  ) THEN
    CREATE TABLE srm_quality_objection (
      id BIGINT NOT NULL AUTO_INCREMENT,
      objection_no VARCHAR(255) NOT NULL,
      supplier_id BIGINT NULL,
      supplier_name VARCHAR(255) NULL,
      material_code VARCHAR(255) NULL,
      material_name VARCHAR(255) NULL,
      description VARCHAR(2000) NULL,
      status VARCHAR(255) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_srm_quality_objection_no (objection_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_material_category'
  ) THEN
    CREATE TABLE srm_material_category (
      id BIGINT NOT NULL AUTO_INCREMENT,
      category_code VARCHAR(255) NOT NULL,
      category_name VARCHAR(255) NOT NULL,
      parent_id BIGINT NULL,
      status VARCHAR(32) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_srm_material_category_code (category_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_material'
  ) THEN
    CREATE TABLE srm_material (
      id BIGINT NOT NULL AUTO_INCREMENT,
      material_code VARCHAR(255) NOT NULL,
      material_name VARCHAR(255) NOT NULL,
      category_id BIGINT NULL,
      unit VARCHAR(64) NULL,
      status VARCHAR(32) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_srm_material_code (material_code)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast'
  ) THEN
    CREATE TABLE srm_material_forecast (
      id BIGINT NOT NULL AUTO_INCREMENT,
      forecast_no VARCHAR(255) NOT NULL,
      period VARCHAR(32) NULL,
      supplier_id BIGINT NULL,
      supplier_name VARCHAR(255) NULL,
      status VARCHAR(255) NULL,
      remarks VARCHAR(255) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_srm_material_forecast_no (forecast_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'srm_material_forecast_item'
  ) THEN
    CREATE TABLE srm_material_forecast_item (
      id BIGINT NOT NULL AUTO_INCREMENT,
      forecast_id BIGINT NULL,
      material_code VARCHAR(255) NULL,
      material_name VARCHAR(255) NULL,
      quantity DECIMAL(18,2) NULL,
      unit VARCHAR(255) NULL,
      remarks VARCHAR(255) NULL,
      PRIMARY KEY (id),
      KEY idx_srm_material_forecast_item_forecast_id (forecast_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;
END//
DELIMITER ;

CALL fix_missing_schema_srm_level2();
DROP PROCEDURE fix_missing_schema_srm_level2;

DELIMITER //
DROP TRIGGER IF EXISTS trg_srm_material_write_gate_ins//
CREATE TRIGGER trg_srm_material_write_gate_ins
BEFORE INSERT ON srm_material
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'srm_material is read-only (owner=ERP)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_srm_material_write_gate_upd//
CREATE TRIGGER trg_srm_material_write_gate_upd
BEFORE UPDATE ON srm_material
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'srm_material is read-only (owner=ERP)';
  END IF;
END//

DROP TRIGGER IF EXISTS trg_srm_material_write_gate_del//
CREATE TRIGGER trg_srm_material_write_gate_del
BEFORE DELETE ON srm_material
FOR EACH ROW
BEGIN
  IF USER() NOT LIKE 'srm_sync@%' AND USER() NOT LIKE 'root@%' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'srm_material is read-only (owner=ERP)';
  END IF;
END//

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
