-- =====================================================================
-- 39-init-crm-index-optimization.sql
-- CRM 模块数据库索引优化
-- 目标：为高频业务查询场景补充索引，降低全表扫描
-- 原则：所有索引均使用 IF NOT EXISTS 思路（通过存储过程判空再创建）
-- =====================================================================

USE crm_db;

-- ---------------------------------------------------------------------
-- 工具存储过程：安全添加索引（存在则跳过）
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS crm_add_index_if_missing;
DELIMITER $$
CREATE PROCEDURE crm_add_index_if_missing(
    IN p_table VARCHAR(64),
    IN p_index VARCHAR(64),
    IN p_ddl   TEXT
)
BEGIN
    DECLARE idx_count INT DEFAULT 0;
    SELECT COUNT(*) INTO idx_count
      FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = 'crm_db'
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
-- 1. 客户表（状态/级别/类型/创建时间/删除标记）
-- ---------------------------------------------------------------------
CALL crm_add_index_if_missing('crm_customer','idx_crm_cust_status',
    'CREATE INDEX idx_crm_cust_status ON crm_customer(status)');
CALL crm_add_index_if_missing('crm_customer','idx_crm_cust_level',
    'CREATE INDEX idx_crm_cust_level ON crm_customer(level)');
CALL crm_add_index_if_missing('crm_customer','idx_crm_cust_type',
    'CREATE INDEX idx_crm_cust_type ON crm_customer(customer_type)');
CALL crm_add_index_if_missing('crm_customer','idx_crm_cust_create_time',
    'CREATE INDEX idx_crm_cust_create_time ON crm_customer(create_time)');
CALL crm_add_index_if_missing('crm_customer','idx_crm_cust_deleted_status',
    'CREATE INDEX idx_crm_cust_deleted_status ON crm_customer(is_deleted, status)');
CALL crm_add_index_if_missing('crm_customer','idx_crm_cust_owner',
    'CREATE INDEX idx_crm_cust_owner ON crm_customer(owner_name)');

-- ---------------------------------------------------------------------
-- 2. 销售订单表（客户/状态/日期/删除标记）
-- ---------------------------------------------------------------------
CALL crm_add_index_if_missing('crm_sales_order','idx_crm_so_customer',
    'CREATE INDEX idx_crm_so_customer ON crm_sales_order(customer_id)');
CALL crm_add_index_if_missing('crm_sales_order','idx_crm_so_status',
    'CREATE INDEX idx_crm_so_status ON crm_sales_order(status)');
CALL crm_add_index_if_missing('crm_sales_order','idx_crm_so_order_date',
    'CREATE INDEX idx_crm_so_order_date ON crm_sales_order(order_date)');
CALL crm_add_index_if_missing('crm_sales_order','idx_crm_so_create_time',
    'CREATE INDEX idx_crm_so_create_time ON crm_sales_order(create_time)');
CALL crm_add_index_if_missing('crm_sales_order','idx_crm_so_deleted_status',
    'CREATE INDEX idx_crm_so_deleted_status ON crm_sales_order(is_deleted, status)');
-- 复合索引：客户+状态（常用查询组合）
CALL crm_add_index_if_missing('crm_sales_order','idx_crm_so_cust_status',
    'CREATE INDEX idx_crm_so_cust_status ON crm_sales_order(customer_id, status)');

-- ---------------------------------------------------------------------
-- 3. 合同表（客户/状态/日期/删除标记）
-- ---------------------------------------------------------------------
CALL crm_add_index_if_missing('crm_contract','idx_crm_contract_customer',
    'CREATE INDEX idx_crm_contract_customer ON crm_contract(customer_id)');
CALL crm_add_index_if_missing('crm_contract','idx_crm_contract_status',
    'CREATE INDEX idx_crm_contract_status ON crm_contract(status)');
CALL crm_add_index_if_missing('crm_contract','idx_crm_contract_sign_date',
    'CREATE INDEX idx_crm_contract_sign_date ON crm_contract(sign_date)');
CALL crm_add_index_if_missing('crm_contract','idx_crm_contract_deleted_status',
    'CREATE INDEX idx_crm_contract_deleted_status ON crm_contract(is_deleted, status)');

-- ---------------------------------------------------------------------
-- 4. 商机表（客户/阶段/负责人/删除标记）
-- ---------------------------------------------------------------------
CALL crm_add_index_if_missing('crm_opportunity','idx_crm_opp_customer',
    'CREATE INDEX idx_crm_opp_customer ON crm_opportunity(customer_id)');
CALL crm_add_index_if_missing('crm_opportunity','idx_crm_opp_stage',
    'CREATE INDEX idx_crm_opp_stage ON crm_opportunity(stage)');
CALL crm_add_index_if_missing('crm_opportunity','idx_crm_opp_owner',
    'CREATE INDEX idx_crm_opp_owner ON crm_opportunity(owner_id)');
CALL crm_add_index_if_missing('crm_opportunity','idx_crm_opp_deleted',
    'CREATE INDEX idx_crm_opp_deleted ON crm_opportunity(is_deleted)');

-- ---------------------------------------------------------------------
-- 5. 客户联系人表（客户ID外键索引）
-- ---------------------------------------------------------------------
CALL crm_add_index_if_missing('crm_customer_contact','idx_crm_contact_customer',
    'CREATE INDEX idx_crm_contact_customer ON crm_customer_contact(customer_id)');
CALL crm_add_index_if_missing('crm_customer_contact','idx_crm_contact_deleted',
    'CREATE INDEX idx_crm_contact_deleted ON crm_customer_contact(is_deleted)');

-- ---------------------------------------------------------------------
-- 6. 客户跟进记录表（客户ID+时间索引）
-- ---------------------------------------------------------------------
CALL crm_add_index_if_missing('crm_customer_follow_up','idx_crm_follow_customer',
    'CREATE INDEX idx_crm_follow_customer ON crm_customer_follow_up(customer_id)');
CALL crm_add_index_if_missing('crm_customer_follow_up','idx_crm_follow_time',
    'CREATE INDEX idx_crm_follow_time ON crm_customer_follow_up(follow_up_time)');
-- 复合索引：客户+时间（360视图常用查询）
CALL crm_add_index_if_missing('crm_customer_follow_up','idx_crm_follow_cust_time',
    'CREATE INDEX idx_crm_follow_cust_time ON crm_customer_follow_up(customer_id, follow_up_time DESC)');

-- ---------------------------------------------------------------------
-- 7. 客户交易记录表（客户ID+日期索引）
-- ---------------------------------------------------------------------
CALL crm_add_index_if_missing('crm_customer_transaction','idx_crm_trans_customer',
    'CREATE INDEX idx_crm_trans_customer ON crm_customer_transaction(customer_id)');
CALL crm_add_index_if_missing('crm_customer_transaction','idx_crm_trans_date',
    'CREATE INDEX idx_crm_trans_date ON crm_customer_transaction(deal_date)');
-- 复合索引：客户+日期（360视图常用查询）
CALL crm_add_index_if_missing('crm_customer_transaction','idx_crm_trans_cust_date',
    'CREATE INDEX idx_crm_trans_cust_date ON crm_customer_transaction(customer_id, deal_date DESC)');

-- ---------------------------------------------------------------------
-- 8. 客户标签关联表（客户ID+标签ID复合索引）
-- ---------------------------------------------------------------------
CALL crm_add_index_if_missing('crm_customer_tag_relation','idx_crm_tag_rel_customer',
    'CREATE INDEX idx_crm_tag_rel_customer ON crm_customer_tag_relation(customer_id)');
CALL crm_add_index_if_missing('crm_customer_tag_relation','idx_crm_tag_rel_tag',
    'CREATE INDEX idx_crm_tag_rel_tag ON crm_customer_tag_relation(tag_id)');
-- 复合索引：客户+标签（联合查询）
CALL crm_add_index_if_missing('crm_customer_tag_relation','idx_crm_tag_rel_cust_tag',
    'CREATE INDEX idx_crm_tag_rel_cust_tag ON crm_customer_tag_relation(customer_id, tag_id)');

-- ---------------------------------------------------------------------
-- 9. 服务工单表（客户/状态/类型）
-- ---------------------------------------------------------------------
CALL crm_add_index_if_missing('crm_service_ticket','idx_crm_ticket_customer',
    'CREATE INDEX idx_crm_ticket_customer ON crm_service_ticket(customer_id)');
CALL crm_add_index_if_missing('crm_service_ticket','idx_crm_ticket_status',
    'CREATE INDEX idx_crm_ticket_status ON crm_service_ticket(status)');
CALL crm_add_index_if_missing('crm_service_ticket','idx_crm_ticket_type',
    'CREATE INDEX idx_crm_ticket_type ON crm_service_ticket(ticket_type)');

-- ---------------------------------------------------------------------
-- 10. 销售订单明细表（订单ID外键索引）
-- ---------------------------------------------------------------------
CALL crm_add_index_if_missing('crm_sales_order_item','idx_crm_soi_order',
    'CREATE INDEX idx_crm_soi_order ON crm_sales_order_item(sales_order_id)');

-- 清理存储过程
DROP PROCEDURE IF EXISTS crm_add_index_if_missing;
