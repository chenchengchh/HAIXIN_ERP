CREATE DATABASE IF NOT EXISTS plm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE plm_db;

DELIMITER //
DROP PROCEDURE IF EXISTS normalize_plm_tables//
CREATE PROCEDURE normalize_plm_tables()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'task'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_task'
    ) THEN
      CREATE TABLE plm_task LIKE task;
      INSERT INTO plm_task SELECT * FROM task;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'project'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_project'
    ) THEN
      CREATE TABLE plm_project LIKE project;
      INSERT INTO plm_project SELECT * FROM project;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'bom'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_bom'
    ) THEN
      CREATE TABLE plm_bom LIKE bom;
      INSERT INTO plm_bom SELECT * FROM bom;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'product'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_product'
    ) THEN
      CREATE TABLE plm_product LIKE product;
      INSERT INTO plm_product SELECT * FROM product;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'gantt_link'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_gantt_link'
    ) THEN
      CREATE TABLE plm_gantt_link LIKE gantt_link;
      INSERT INTO plm_gantt_link SELECT * FROM gantt_link;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'quality_issue'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_quality_issue'
    ) THEN
      CREATE TABLE plm_quality_issue LIKE quality_issue;
      INSERT INTO plm_quality_issue SELECT * FROM quality_issue;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'trial_plan'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_trial_plan'
    ) THEN
      CREATE TABLE plm_trial_plan LIKE trial_plan;
      INSERT INTO plm_trial_plan SELECT * FROM trial_plan;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'trial_report'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_trial_report'
    ) THEN
      CREATE TABLE plm_trial_report LIKE trial_report;
      INSERT INTO plm_trial_report SELECT * FROM trial_report;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'change_request'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_change_request'
    ) THEN
      CREATE TABLE plm_change_request LIKE change_request;
      INSERT INTO plm_change_request SELECT * FROM change_request;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'process_file'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_process_file'
    ) THEN
      CREATE TABLE plm_process_file LIKE process_file;
      INSERT INTO plm_process_file SELECT * FROM process_file;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'process_route'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_process_route'
    ) THEN
      CREATE TABLE plm_process_route LIKE process_route;
      INSERT INTO plm_process_route SELECT * FROM process_route;
    END IF;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'resource_load'
  ) THEN
    IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = DATABASE() AND table_name = 'plm_resource_load'
    ) THEN
      CREATE TABLE plm_resource_load LIKE resource_load;
      INSERT INTO plm_resource_load SELECT * FROM resource_load;
    END IF;
  END IF;
END//
DELIMITER ;

CALL normalize_plm_tables();
DROP PROCEDURE normalize_plm_tables;
