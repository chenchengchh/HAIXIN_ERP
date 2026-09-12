-- =====================================================================
-- 38-init-erp-index-optimization.sql
-- ERP 模块数据库索引优化
-- 目标：为高频业务查询场景补充索引，降低全表扫描
-- 原则：所有索引均使用 IF NOT EXISTS 思路（通过存储过程判空再创建）
-- =====================================================================

USE erp_db;

-- ---------------------------------------------------------------------
-- 工具存储过程：安全添加索引（存在则跳过）
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS erp_add_index_if_missing;
DELIMITER $$
CREATE PROCEDURE erp_add_index_if_missing(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_ddl   TEXT
)
BEGIN
    DECLARE idx_count INT DEFAULT 0;
    SELECT COUNT(*) INTO idx_count
      FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = 'erp_db'
       AND TABLE_NAME   = p_table
       AND INDEX_NAME   = p_index;
    IF idx_count = 0 THEN
        SET @s = p_ddl;
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- ---------------------------------------------------------------------
-- 1. 销售订单镜像（订单中心 / 状态过滤 / 客户维度）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_sales_order_mirror','idx_erp_som_status',
    'CREATE INDEX idx_erp_som_status ON erp_sales_order_mirror(status)');
CALL erp_add_index_if_missing('erp_sales_order_mirror','idx_erp_som_customer',
    'CREATE INDEX idx_erp_som_customer ON erp_sales_order_mirror(customer_id)');
CALL erp_add_index_if_missing('erp_sales_order_mirror','idx_erp_som_order_date',
    'CREATE INDEX idx_erp_som_order_date ON erp_sales_order_mirror(order_date)');

-- ---------------------------------------------------------------------
-- 2. 财务流水（状态/类型/日期三维查询）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_finance','idx_erp_finance_status_type',
    'CREATE INDEX idx_erp_finance_status_type ON erp_finance(status, transaction_type)');
CALL erp_add_index_if_missing('erp_finance','idx_erp_finance_txn_date',
    'CREATE INDEX idx_erp_finance_txn_date ON erp_finance(transaction_date)');
CALL erp_add_index_if_missing('erp_finance','idx_erp_finance_is_deleted',
    'CREATE INDEX idx_erp_finance_is_deleted ON erp_finance(is_deleted, status)');

-- ---------------------------------------------------------------------
-- 3. 生产订单（状态/车间/计划时间）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_production','idx_erp_prod_status',
    'CREATE INDEX idx_erp_prod_status ON erp_production(production_status)');
CALL erp_add_index_if_missing('erp_production','idx_erp_prod_workshop_status',
    'CREATE INDEX idx_erp_prod_workshop_status ON erp_production(workshop, production_status)');
CALL erp_add_index_if_missing('erp_production','idx_erp_prod_plan_start',
    'CREATE INDEX idx_erp_prod_plan_start ON erp_production(plan_start_time)');
CALL erp_add_index_if_missing('erp_production','idx_erp_prod_product_code',
    'CREATE INDEX idx_erp_prod_product_code ON erp_production(product_code)');

-- ---------------------------------------------------------------------
-- 4. 供应链单据（业务类型/状态/物料）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_supply_chain','idx_erp_sc_type_status',
    'CREATE INDEX idx_erp_sc_type_status ON erp_supply_chain(business_type, status)');
CALL erp_add_index_if_missing('erp_supply_chain','idx_erp_sc_material',
    'CREATE INDEX idx_erp_sc_material ON erp_supply_chain(material_code)');
CALL erp_add_index_if_missing('erp_supply_chain','idx_erp_sc_warehouse',
    'CREATE INDEX idx_erp_sc_warehouse ON erp_supply_chain(warehouse_code)');

-- ---------------------------------------------------------------------
-- 5. 凭证（状态/类型/日期）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_voucher','idx_erp_voucher_status',
    'CREATE INDEX idx_erp_voucher_status ON erp_voucher(status)');
CALL erp_add_index_if_missing('erp_voucher','idx_erp_voucher_type_date',
    'CREATE INDEX idx_erp_voucher_type_date ON erp_voucher(voucher_type, voucher_date)');
CALL erp_add_index_if_missing('erp_voucher','idx_erp_voucher_date',
    'CREATE INDEX idx_erp_voucher_date ON erp_voucher(voucher_date)');

-- ---------------------------------------------------------------------
-- 6. 凭证分录（凭证反查 / 总账分组汇总覆盖索引）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_voucher_item','idx_erp_vi_voucher',
    'CREATE INDEX idx_erp_vi_voucher ON erp_voucher_item(voucher_id)');
CALL erp_add_index_if_missing('erp_voucher_item','idx_erp_vi_account',
    'CREATE INDEX idx_erp_vi_account ON erp_voucher_item(account_id)');
-- 总账报表 GROUP BY 覆盖索引：避免回表
CALL erp_add_index_if_missing('erp_voucher_item','idx_erp_vi_account_cover',
    'CREATE INDEX idx_erp_vi_account_cover ON erp_voucher_item(account_id, voucher_id, debit_amount, credit_amount)');

-- ---------------------------------------------------------------------
-- 7. 集成任务（Outbox 重试扫描：status+updated_time 高频）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_integration_task','idx_erp_it_status_retry',
    'CREATE INDEX idx_erp_it_status_retry ON erp_integration_task(status, retry_count, updated_time)');
CALL erp_add_index_if_missing('erp_integration_task','idx_erp_it_idempotency',
    'CREATE INDEX idx_erp_it_idempotency ON erp_integration_task(idempotency_key)');
CALL erp_add_index_if_missing('erp_integration_task','idx_erp_it_action_type',
    'CREATE INDEX idx_erp_it_action_type ON erp_integration_task(action_type, status)');

-- ---------------------------------------------------------------------
-- 8. 能源成本（期间/能源类型/区域）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_energy_cost','idx_erp_energy_period',
    'CREATE INDEX idx_erp_energy_period ON erp_energy_cost(period)');
CALL erp_add_index_if_missing('erp_energy_cost','idx_erp_energy_type_area',
    'CREATE INDEX idx_erp_energy_type_area ON erp_energy_cost(energy_type, area)');

-- ---------------------------------------------------------------------
-- 9. 物料（类型/状态/删除标记）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_material','idx_erp_material_type',
    'CREATE INDEX idx_erp_material_type ON erp_material(material_type)');
CALL erp_add_index_if_missing('erp_material','idx_erp_material_status',
    'CREATE INDEX idx_erp_material_status ON erp_material(status, is_deleted)');
CALL erp_add_index_if_missing('erp_material','idx_erp_material_name',
    'CREATE INDEX idx_erp_material_name ON erp_material(material_name)');

-- ---------------------------------------------------------------------
-- 10. 会计科目（类型/层级/状态）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_account','idx_erp_account_type',
    'CREATE INDEX idx_erp_account_type ON erp_account(account_type, status)');
CALL erp_add_index_if_missing('erp_account','idx_erp_account_parent',
    'CREATE INDEX idx_erp_account_parent ON erp_account(parent_id)');

-- ---------------------------------------------------------------------
-- 11. 组织架构（类型/层级/状态）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_organization','idx_erp_org_type',
    'CREATE INDEX idx_erp_org_type ON erp_organization(organization_type, status)');
CALL erp_add_index_if_missing('erp_organization','idx_erp_org_parent',
    'CREATE INDEX idx_erp_org_parent ON erp_organization(parent_id)');

-- ---------------------------------------------------------------------
-- 12. 仓库（状态/类型）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_warehouse','idx_erp_wh_status',
    'CREATE INDEX idx_erp_wh_status ON erp_warehouse(status, warehouse_type)');

-- ---------------------------------------------------------------------
-- 13. 库位（仓库+状态）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_location','idx_erp_loc_wh_status',
    'CREATE INDEX idx_erp_loc_wh_status ON erp_location(warehouse_code, status)');

-- ---------------------------------------------------------------------
-- 14. 固定资产（状态/类别/部门）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_fixed_assets','idx_erp_fa_status',
    'CREATE INDEX idx_erp_fa_status ON erp_fixed_assets(status)');
CALL erp_add_index_if_missing('erp_fixed_assets','idx_erp_fa_category',
    'CREATE INDEX idx_erp_fa_category ON erp_fixed_assets(asset_category)');
CALL erp_add_index_if_missing('erp_fixed_assets','idx_erp_fa_department',
    'CREATE INDEX idx_erp_fa_department ON erp_fixed_assets(using_department)');

-- ---------------------------------------------------------------------
-- 15. 核销单（状态/类型/原单）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_write_off','idx_erp_wo_status',
    'CREATE INDEX idx_erp_wo_status ON erp_write_off(status)');
CALL erp_add_index_if_missing('erp_write_off','idx_erp_wo_original',
    'CREATE INDEX idx_erp_wo_original ON erp_write_off(original_doc_type, original_doc_id)');
CALL erp_add_index_if_missing('erp_write_off','idx_erp_wo_date',
    'CREATE INDEX idx_erp_wo_date ON erp_write_off(write_off_date)');

-- ---------------------------------------------------------------------
-- 16. 成本核算记录（产品/日期/类型）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_cost_accounting_record','idx_erp_car_product',
    'CREATE INDEX idx_erp_car_product ON erp_cost_accounting_record(product_code)');
CALL erp_add_index_if_missing('erp_cost_accounting_record','idx_erp_car_cost_date',
    'CREATE INDEX idx_erp_car_cost_date ON erp_cost_accounting_record(cost_date)');
CALL erp_add_index_if_missing('erp_cost_accounting_record','idx_erp_car_cost_type',
    'CREATE INDEX idx_erp_car_cost_type ON erp_cost_accounting_record(cost_type)');

-- ---------------------------------------------------------------------
-- 17. 调拨单 / 采购退货单（状态）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_transfer_order','idx_erp_transfer_status',
    'CREATE INDEX idx_erp_transfer_status ON erp_transfer_order(status)');
CALL erp_add_index_if_missing('erp_purchase_return_order','idx_erp_pr_status',
    'CREATE INDEX idx_erp_pr_status ON erp_purchase_return_order(status)');
CALL erp_add_index_if_missing('erp_purchase_return_order','idx_erp_pr_supplier',
    'CREATE INDEX idx_erp_pr_supplier ON erp_purchase_return_order(supplier_code)');

-- ---------------------------------------------------------------------
-- 18. 出库发货事实表（创建时间维度）
-- ---------------------------------------------------------------------
CALL erp_add_index_if_missing('erp_outbound_shipment_fact','idx_erp_osf_created',
    'CREATE INDEX idx_erp_osf_created ON erp_outbound_shipment_fact(created_time)');

-- ---------------------------------------------------------------------
-- 清理工具存储过程
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS erp_add_index_if_missing;
