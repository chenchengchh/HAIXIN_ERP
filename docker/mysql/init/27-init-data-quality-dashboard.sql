USE system_db;

CREATE OR REPLACE VIEW vw_data_quality_metric_all AS
SELECT 'srm' AS source, metric_date, metric_key, metric_value, detail_json, created_time
FROM srm_db.srm_data_quality_metric
UNION ALL
SELECT 'erp' AS source, metric_date, metric_key, metric_value, detail_json, created_time
FROM erp_db.erp_data_quality_metric
UNION ALL
SELECT 'wms' AS source, metric_date, metric_key, metric_value, detail_json, created_time
FROM wms_db.wms_data_quality_metric
UNION ALL
SELECT 'crm' AS source, metric_date, metric_key, metric_value, detail_json, created_time
FROM crm_db.crm_data_quality_metric
UNION ALL
SELECT 'eam' AS source, metric_date, metric_key, metric_value, detail_json, created_time
FROM eam_db.eam_data_quality_metric
UNION ALL
SELECT 'hr' AS source, metric_date, metric_key, metric_value, detail_json, created_time
FROM hr_db.hr_data_quality_metric;

CREATE OR REPLACE VIEW vw_data_clean_task_all AS
SELECT 'srm' AS source, id, entity_type, conflict_type, business_key, detail_json, status, resolution, created_time, updated_time
FROM srm_db.srm_data_clean_task
UNION ALL
SELECT 'erp' AS source, id, entity_type, conflict_type, business_key, detail_json, status, resolution, created_time, updated_time
FROM erp_db.erp_data_clean_task
UNION ALL
SELECT 'wms' AS source, id, entity_type, conflict_type, business_key, detail_json, status, resolution, created_time, updated_time
FROM wms_db.wms_data_clean_task
UNION ALL
SELECT 'crm' AS source, id, entity_type, conflict_type, business_key, detail_json, status, resolution, created_time, updated_time
FROM crm_db.crm_data_clean_task
UNION ALL
SELECT 'eam' AS source, id, entity_type, conflict_type, business_key, detail_json, status, resolution, created_time, updated_time
FROM eam_db.eam_data_clean_task
UNION ALL
SELECT 'hr' AS source, id, entity_type, conflict_type, business_key, detail_json, status, resolution, created_time, updated_time
FROM hr_db.hr_data_clean_task;
