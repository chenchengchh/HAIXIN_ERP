CREATE TABLE IF NOT EXISTS bom_substitute_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type INT NOT NULL COMMENT '规则类型：1-优先级策略 2-比例策略 3-生效条件',
    target_category_id BIGINT COMMENT '适用分类ID，null=全局',
    strategy_config VARCHAR(1000) COMMENT '策略配置JSON',
    priority INT DEFAULT 99 COMMENT '规则优先级（数字越小越优先）',
    effective_date DATETIME COMMENT '生效日期',
    expire_date DATETIME COMMENT '失效日期',
    status INT NOT NULL DEFAULT 1 COMMENT '0-禁用 1-启用',
    created_by VARCHAR(50),
    created_time DATETIME,
    updated_by VARCHAR(50),
    updated_time DATETIME,
    remark VARCHAR(500)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='替代规则配置';

INSERT INTO bom_substitute_rule (rule_name, rule_type, target_category_id, strategy_config, priority, effective_date, expire_date, status, created_by, created_time, remark) VALUES
('全局低成本优先策略', 1, NULL, '{"preferLowCost":true,"maxAlternatives":3}', 1, '2026-07-01 00:00:00', NULL, 1, 'seed', '2026-07-01 10:00:00', '替代料选择时优先使用单价较低的物料，最多保留3个替代选项'),
('电气件比例约束', 2, 3, '{"maxRatio":2.0,"allowCrossType":false}', 2, '2026-07-01 00:00:00', NULL, 1, 'seed', '2026-07-01 10:00:00', '电气件分类的替代比例不超过1:2，禁止跨子分类替代'),
('原材料库存生效条件', 3, 1, '{"minStockQty":100,"checkWarehouse":true}', 3, '2026-07-01 00:00:00', '2026-12-31 23:59:59', 1, 'seed', '2026-07-01 10:00:00', '原材料替代仅在库存>=100时生效，需校验仓库库存'),
('标准件自动替代策略', 1, 2, '{"autoReplace":true,"matchBySpec":true}', 4, '2026-07-01 00:00:00', NULL, 0, 'seed', '2026-07-01 10:00:00', '标准件支持按规格自动匹配替代（当前禁用）');
