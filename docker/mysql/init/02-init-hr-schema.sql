USE hr_db;

CREATE TABLE IF NOT EXISTS hr_department (
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

CREATE TABLE IF NOT EXISTS hr_employee (
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

CREATE TABLE IF NOT EXISTS hr_position (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  position_code VARCHAR(20) NOT NULL,
  level VARCHAR(20) NULL,
  description VARCHAR(200) NULL,
  status VARCHAR(20) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_by VARCHAR(50) NULL,
  remark VARCHAR(500) NULL,
  version INT NULL,
  department_id BIGINT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_hr_position_code (position_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_resume (
  id BIGINT NOT NULL AUTO_INCREMENT,
  candidate_name VARCHAR(50) NOT NULL,
  gender VARCHAR(10) NULL,
  phone VARCHAR(20) NULL,
  email VARCHAR(100) NULL,
  position_id BIGINT NULL,
  resume_url VARCHAR(200) NULL,
  status VARCHAR(20) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_by VARCHAR(50) NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_hr_resume_position_id (position_id),
  CONSTRAINT fk_hr_resume_position FOREIGN KEY (position_id) REFERENCES hr_position (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_salary_structure (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  basic_salary DECIMAL(10,2) NULL,
  bonus DECIMAL(10,2) NULL,
  allowance DECIMAL(10,2) NULL,
  deduction DECIMAL(10,2) NULL,
  effective_date DATE NULL,
  status VARCHAR(20) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_by VARCHAR(50) NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_hr_salary_structure_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_payroll_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  employee_id BIGINT NULL,
  month VARCHAR(7) NULL,
  basic_salary DECIMAL(10,2) NULL,
  performance_salary DECIMAL(10,2) NULL,
  bonus DECIMAL(10,2) NULL,
  allowance DECIMAL(10,2) NULL,
  deduction DECIMAL(10,2) NULL,
  actual_salary DECIMAL(10,2) NULL,
  status INT NULL,
  remark VARCHAR(500) NULL,
  created_by VARCHAR(50) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) NULL,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hr_payroll_employee_id (employee_id),
  KEY idx_hr_payroll_month (month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_transfer_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  employee_id BIGINT NULL,
  old_department_id BIGINT NULL,
  new_department_id BIGINT NULL,
  old_position_id BIGINT NULL,
  new_position_id BIGINT NULL,
  reason VARCHAR(500) NULL,
  transfer_date DATETIME NULL,
  status INT NULL,
  remark VARCHAR(500) NULL,
  created_by VARCHAR(50) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) NULL,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hr_transfer_employee_id (employee_id),
  KEY idx_hr_transfer_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_salary_adjustment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  employee_id BIGINT NULL,
  old_salary DECIMAL(10,2) NULL,
  new_salary DECIMAL(10,2) NULL,
  reason VARCHAR(500) NULL,
  adjustment_date DATETIME NULL,
  status INT NULL,
  remark VARCHAR(500) NULL,
  created_by VARCHAR(50) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) NULL,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hr_salary_adjust_employee_id (employee_id),
  KEY idx_hr_salary_adjust_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_resignation_request (
  id BIGINT NOT NULL AUTO_INCREMENT,
  employee_id BIGINT NULL,
  resignation_type INT NULL,
  reason VARCHAR(500) NULL,
  apply_date DATETIME NULL,
  expected_resign_date DATETIME NULL,
  actual_resign_date DATETIME NULL,
  status INT NULL,
  remark VARCHAR(500) NULL,
  created_by VARCHAR(50) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(50) NULL,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_hr_resign_employee_id (employee_id),
  KEY idx_hr_resign_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_recruitment_demand (
  id BIGINT NOT NULL AUTO_INCREMENT,
  position_name VARCHAR(50) NOT NULL,
  department_id BIGINT NULL,
  demand_number INT NULL,
  required_skills VARCHAR(500) NULL,
  expected_salary VARCHAR(20) NULL,
  status VARCHAR(20) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_by VARCHAR(50) NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_hr_recruitment_department_id (department_id),
  KEY idx_hr_recruitment_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_interview (
  id BIGINT NOT NULL AUTO_INCREMENT,
  resume_id BIGINT NULL,
  interviewer_id BIGINT NULL,
  interview_time DATETIME NULL,
  interview_type VARCHAR(20) NULL,
  interview_result VARCHAR(20) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_by VARCHAR(50) NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_hr_interview_resume_id (resume_id),
  KEY idx_hr_interview_interviewer_id (interviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_leave_request (
  id BIGINT NOT NULL AUTO_INCREMENT,
  employee_id BIGINT NULL,
  leave_type VARCHAR(20) NULL,
  start_date DATE NULL,
  end_date DATE NULL,
  duration DOUBLE NULL,
  reason VARCHAR(500) NULL,
  status VARCHAR(20) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_by VARCHAR(50) NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_hr_leave_employee_id (employee_id),
  KEY idx_hr_leave_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_attendance_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  employee_id BIGINT NULL,
  attendance_date DATE NULL,
  check_in_time DATETIME NULL,
  check_out_time DATETIME NULL,
  status VARCHAR(20) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_by VARCHAR(50) NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_hr_attendance_employee_id (employee_id),
  KEY idx_hr_attendance_date (attendance_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_attendance_exception (
  id BIGINT NOT NULL AUTO_INCREMENT,
  employee_id BIGINT NULL,
  exception_date DATE NULL,
  exception_type VARCHAR(20) NULL,
  description VARCHAR(500) NULL,
  status VARCHAR(20) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_by VARCHAR(50) NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_hr_att_ex_employee_id (employee_id),
  KEY idx_hr_att_ex_date (exception_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_performance_objective (
  id BIGINT NOT NULL AUTO_INCREMENT,
  employee_id BIGINT NULL,
  objective_content VARCHAR(500) NULL,
  target_value VARCHAR(200) NULL,
  weight DOUBLE NULL,
  start_date DATE NULL,
  end_date DATE NULL,
  status VARCHAR(20) NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_by VARCHAR(50) NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_hr_perf_obj_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hr_performance_appraisal (
  id BIGINT NOT NULL AUTO_INCREMENT,
  employee_id BIGINT NULL,
  appraisal_period VARCHAR(20) NULL,
  objective_score DOUBLE NULL,
  competency_score DOUBLE NULL,
  total_score DOUBLE NULL,
  appraisal_status VARCHAR(20) NULL,
  appraiser_id BIGINT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(50) NULL,
  updated_by VARCHAR(50) NULL,
  remark VARCHAR(500) NULL,
  PRIMARY KEY (id),
  KEY idx_hr_perf_app_employee_id (employee_id),
  KEY idx_hr_perf_app_appraiser_id (appraiser_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
