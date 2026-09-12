USE hxcoe003;

DELIMITER //
DROP PROCEDURE IF EXISTS fix_missing_schema//
CREATE PROCEDURE fix_missing_schema()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'hr_department'
  ) THEN
    CREATE TABLE hr_department (
      id BIGINT NOT NULL AUTO_INCREMENT,
      name VARCHAR(50) NOT NULL,
      department_code VARCHAR(20) NOT NULL,
      parent_id BIGINT NULL,
      manager_id BIGINT NULL,
      description VARCHAR(200) NULL,
      status VARCHAR(20) NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      created_by VARCHAR(50) NULL,
      updated_by VARCHAR(50) NULL,
      remark VARCHAR(500) NULL,
      version INT NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_hr_department_code (department_code),
      KEY idx_hr_department_parent_id (parent_id),
      KEY idx_hr_department_manager_id (manager_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'hr_employee'
  ) THEN
    CREATE TABLE hr_employee (
      id BIGINT NOT NULL AUTO_INCREMENT,
      employee_code VARCHAR(20) NOT NULL,
      name VARCHAR(50) NOT NULL,
      gender VARCHAR(10) NULL,
      id_card VARCHAR(18) NULL,
      phone VARCHAR(20) NULL,
      email VARCHAR(100) NULL,
      department_id BIGINT NULL,
      position_id BIGINT NULL,
      hire_date DATE NULL,
      leave_date DATE NULL,
      status VARCHAR(20) NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      created_by VARCHAR(50) NULL,
      updated_by VARCHAR(50) NULL,
      remark VARCHAR(500) NULL,
      version INT NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_hr_employee_code (employee_code),
      UNIQUE KEY uk_hr_employee_id_card (id_card),
      KEY idx_hr_employee_department_id (department_id),
      KEY idx_hr_employee_position_id (position_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'oa_document'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_document' AND column_name = 'type'
    ) THEN
      ALTER TABLE oa_document ADD COLUMN type VARCHAR(20) NULL;
    END IF;

    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_document' AND column_name = 'current_version'
    ) THEN
      ALTER TABLE oa_document ADD COLUMN current_version VARCHAR(20) NULL;
    END IF;

    IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_document' AND column_name = 'status'
    ) THEN
      ALTER TABLE oa_document MODIFY COLUMN status INT NOT NULL DEFAULT 0;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'oa_meeting_room'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_meeting_room' AND column_name = 'description'
    ) THEN
      ALTER TABLE oa_meeting_room ADD COLUMN description VARCHAR(500) NULL;
    END IF;

    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_meeting_room' AND column_name = 'creator_id'
    ) THEN
      ALTER TABLE oa_meeting_room ADD COLUMN creator_id BIGINT NULL;
    END IF;

    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_meeting_room' AND column_name = 'creator_name'
    ) THEN
      ALTER TABLE oa_meeting_room ADD COLUMN creator_name VARCHAR(50) NULL;
    END IF;

    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_meeting_room' AND column_name = 'updater_id'
    ) THEN
      ALTER TABLE oa_meeting_room ADD COLUMN updater_id BIGINT NULL;
    END IF;

    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_meeting_room' AND column_name = 'create_time'
    ) THEN
      ALTER TABLE oa_meeting_room ADD COLUMN create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;

    IF NOT EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema = DATABASE() AND table_name = 'oa_meeting_room' AND column_name = 'update_time'
    ) THEN
      ALTER TABLE oa_meeting_room ADD COLUMN update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'crm_lead'
  ) THEN
    CREATE TABLE crm_lead (
      id BIGINT NOT NULL AUTO_INCREMENT,
      lead_no VARCHAR(64) NULL,
      lead_name VARCHAR(255) NULL,
      company_name VARCHAR(255) NULL,
      contact_name VARCHAR(255) NULL,
      phone VARCHAR(64) NULL,
      email VARCHAR(255) NULL,
      source VARCHAR(255) NULL,
      industry VARCHAR(255) NULL,
      intent VARCHAR(255) NULL,
      rating VARCHAR(64) NULL,
      status VARCHAR(64) NULL,
      owner_id BIGINT NULL,
      owner_name VARCHAR(255) NULL,
      convert_time DATETIME NULL,
      create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      create_by BIGINT NULL,
      update_by BIGINT NULL,
      is_deleted INT NULL DEFAULT 0,
      PRIMARY KEY (id),
      UNIQUE KEY uk_crm_lead_no (lead_no),
      KEY idx_crm_lead_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'crm_customer'
  ) THEN
    CREATE TABLE crm_customer (
      id BIGINT NOT NULL AUTO_INCREMENT,
      customer_no VARCHAR(32) NOT NULL,
      customer_name VARCHAR(128) NOT NULL,
      customer_type VARCHAR(32) NOT NULL,
      industry VARCHAR(64) NULL,
      scale VARCHAR(32) NULL,
      level VARCHAR(8) NULL,
      status VARCHAR(32) NOT NULL,
      source VARCHAR(32) NULL,
      tags VARCHAR(255) NULL,
      region VARCHAR(64) NULL,
      address VARCHAR(255) NULL,
      website VARCHAR(128) NULL,
      owner_id BIGINT NULL,
      owner_name VARCHAR(64) NULL,
      created_by VARCHAR(64) NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_by VARCHAR(64) NULL,
      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      is_deleted TINYINT(1) NOT NULL DEFAULT 0,
      PRIMARY KEY (id),
      UNIQUE KEY uk_crm_customer_no (customer_no),
      KEY idx_crm_customer_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'crm_sales_order'
  ) THEN
    CREATE TABLE crm_sales_order (
      id BIGINT NOT NULL AUTO_INCREMENT,
      order_no VARCHAR(32) NOT NULL,
      customer_id BIGINT NOT NULL,
      customer_name VARCHAR(128) NULL,
      opportunity_id BIGINT NULL,
      total_amount DECIMAL(18,2) NULL,
      discount_amount DECIMAL(18,2) NULL,
      final_amount DECIMAL(18,2) NULL,
      currency VARCHAR(10) NULL,
      order_date DATE NULL,
      delivery_date DATE NULL,
      payment_terms VARCHAR(256) NULL,
      delivery_address VARCHAR(512) NULL,
      sales_person_id BIGINT NULL,
      status VARCHAR(32) NULL,
      approval_status VARCHAR(32) NULL,
      remark VARCHAR(1000) NULL,
      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      update_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_crm_sales_order_no (order_no),
      KEY idx_crm_sales_order_customer_id (customer_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'erp_voucher'
  ) THEN
    CREATE TABLE erp_voucher (
      id BIGINT NOT NULL AUTO_INCREMENT,
      voucher_no VARCHAR(32) NOT NULL,
      voucher_date DATETIME NOT NULL,
      voucher_type VARCHAR(16) NOT NULL,
      summary VARCHAR(255) NULL,
      status VARCHAR(16) NOT NULL,
      debit_total DECIMAL(18,2) NOT NULL DEFAULT 0,
      credit_total DECIMAL(18,2) NOT NULL DEFAULT 0,
      attachment_count INT NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      created_by VARCHAR(64) NULL,
      updated_by VARCHAR(64) NULL,
      is_deleted INT NOT NULL DEFAULT 0,
      PRIMARY KEY (id),
      UNIQUE KEY uk_erp_voucher_no (voucher_no),
      KEY idx_erp_voucher_date (voucher_date)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'mes_work_order'
  ) THEN
    CREATE TABLE mes_work_order (
      id BIGINT NOT NULL AUTO_INCREMENT,
      work_order_no VARCHAR(64) NULL,
      source_id VARCHAR(64) NULL,
      product_code VARCHAR(64) NULL,
      product_name VARCHAR(128) NULL,
      plan_quantity DECIMAL(18,2) NULL,
      actual_quantity DECIMAL(18,2) NULL,
      resource_name VARCHAR(128) NULL,
      start_time DATETIME NULL,
      end_time DATETIME NULL,
      status VARCHAR(32) NULL,
      create_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_mes_work_order_no (work_order_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_inspection_task'
  ) THEN
    CREATE TABLE qms_inspection_task (
      id BIGINT NOT NULL AUTO_INCREMENT,
      task_no VARCHAR(64) NOT NULL,
      plan_id BIGINT NULL,
      plan_name VARCHAR(128) NULL,
      material_code VARCHAR(64) NULL,
      material_name VARCHAR(128) NULL,
      batch_no VARCHAR(64) NULL,
      quantity DECIMAL(18,2) NULL,
      task_type VARCHAR(32) NULL,
      assignee VARCHAR(64) NULL,
      assign_time DATETIME NULL,
      due_time DATETIME NULL,
      status VARCHAR(16) NULL,
      inspection_result VARCHAR(16) NULL,
      task_name VARCHAR(128) NULL,
      plan_no VARCHAR(64) NULL,
      description VARCHAR(500) NULL,
      actual_finish_time DATETIME NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_inspection_task_no (task_no),
      KEY idx_qms_inspection_task_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_inspection_result'
  ) THEN
    CREATE TABLE qms_inspection_result (
      id BIGINT NOT NULL AUTO_INCREMENT,
      result_no VARCHAR(64) NOT NULL,
      task_id BIGINT NULL,
      task_no VARCHAR(64) NULL,
      material_code VARCHAR(64) NULL,
      material_name VARCHAR(128) NULL,
      batch_no VARCHAR(64) NULL,
      inspector VARCHAR(64) NULL,
      inspection_time DATETIME NULL,
      inspection_items_json LONGTEXT NULL,
      inspection_result VARCHAR(16) NULL,
      audit_status VARCHAR(16) NULL,
      auditor VARCHAR(64) NULL,
      audit_time DATETIME NULL,
      audit_remark VARCHAR(500) NULL,
      inspection_quantity DECIMAL(18,2) NULL,
      qualified_quantity DECIMAL(18,2) NULL,
      unqualified_quantity DECIMAL(18,2) NULL,
      description VARCHAR(500) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_inspection_result_no (result_no),
      KEY idx_qms_inspection_result_task_id (task_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'qms_quality_report'
  ) THEN
    CREATE TABLE qms_quality_report (
      id BIGINT NOT NULL AUTO_INCREMENT,
      report_no VARCHAR(64) NOT NULL,
      report_name VARCHAR(128) NULL,
      report_type VARCHAR(16) NULL,
      period VARCHAR(64) NULL,
      content_json LONGTEXT NULL,
      creator VARCHAR(64) NULL,
      create_time DATETIME NULL,
      status VARCHAR(16) NULL,
      created_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      PRIMARY KEY (id),
      UNIQUE KEY uk_qms_quality_report_no (report_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'customers'
  ) THEN
    CREATE TABLE customers (
      id BIGINT NOT NULL AUTO_INCREMENT,
      customer_code VARCHAR(30) NULL,
      name VARCHAR(50) NOT NULL,
      phone VARCHAR(11) NULL,
      email VARCHAR(100) NULL,
      gender VARCHAR(10) NULL,
      birth_date DATETIME NULL,
      source VARCHAR(50) NULL,
      level VARCHAR(20) NULL,
      status VARCHAR(20) NULL,
      remark VARCHAR(500) NULL,
      created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      created_by VARCHAR(50) NULL,
      updated_by VARCHAR(50) NULL,
      PRIMARY KEY (id),
      UNIQUE KEY uk_customers_customer_code (customer_code),
      KEY idx_customers_phone (phone)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'scrm_customer'
  ) THEN
    CREATE TABLE scrm_customer LIKE customers;
    INSERT INTO scrm_customer SELECT * FROM customers;
  END IF;
END//
DELIMITER ;

CALL fix_missing_schema();
DROP PROCEDURE fix_missing_schema;
