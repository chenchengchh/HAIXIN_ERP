-- 主数据映射表：用于跨域/历史系统引用对齐，避免用 name 等非唯一字段关联

-- SRM：供应商映射（Owner=SRM）
CREATE DATABASE IF NOT EXISTS `srm_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `srm_db`;

CREATE TABLE IF NOT EXISTS `srm_supplier_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint(20) NOT NULL,
  `supplier_code` varchar(64) NOT NULL,
  `legacy_system` varchar(32) NOT NULL,
  `legacy_id` varchar(64) DEFAULT NULL,
  `legacy_code` varchar(64) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_srm_supplier_mapping_legacy_id` (`legacy_system`, `legacy_id`),
  UNIQUE KEY `uk_srm_supplier_mapping_legacy_code` (`legacy_system`, `legacy_code`),
  KEY `idx_srm_supplier_mapping_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商跨域/历史编码映射表';

-- ERP：物料映射（Owner=ERP，当前落在共享库 hxcoe003）
CREATE DATABASE IF NOT EXISTS `hxcoe003` DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
USE `hxcoe003`;

CREATE TABLE IF NOT EXISTS `erp_material_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `material_id` bigint(20) DEFAULT NULL,
  `material_code` varchar(64) NOT NULL,
  `legacy_system` varchar(32) NOT NULL,
  `legacy_id` varchar(64) DEFAULT NULL,
  `legacy_code` varchar(64) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_erp_material_mapping_legacy_id` (`legacy_system`, `legacy_id`),
  UNIQUE KEY `uk_erp_material_mapping_legacy_code` (`legacy_system`, `legacy_code`),
  KEY `idx_erp_material_mapping_material_code` (`material_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='物料跨域/历史编码映射表';

-- WMS：仓库/库位映射（Owner=WMS）
CREATE DATABASE IF NOT EXISTS `wms_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `wms_db`;

CREATE TABLE IF NOT EXISTS `wms_warehouse_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `warehouse_id` bigint(20) DEFAULT NULL,
  `warehouse_code` varchar(64) NOT NULL,
  `legacy_system` varchar(32) NOT NULL,
  `legacy_id` varchar(64) DEFAULT NULL,
  `legacy_code` varchar(64) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wms_wh_mapping_legacy_id` (`legacy_system`, `legacy_id`),
  UNIQUE KEY `uk_wms_wh_mapping_legacy_code` (`legacy_system`, `legacy_code`),
  KEY `idx_wms_wh_mapping_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库跨域/历史编码映射表';

CREATE TABLE IF NOT EXISTS `wms_location_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `location_id` bigint(20) DEFAULT NULL,
  `location_code` varchar(64) NOT NULL,
  `warehouse_code` varchar(64) DEFAULT NULL,
  `legacy_system` varchar(32) NOT NULL,
  `legacy_id` varchar(64) DEFAULT NULL,
  `legacy_code` varchar(64) DEFAULT NULL,
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wms_loc_mapping_legacy_id` (`legacy_system`, `legacy_id`),
  UNIQUE KEY `uk_wms_loc_mapping_legacy_code` (`legacy_system`, `legacy_code`),
  KEY `idx_wms_loc_mapping_location_code` (`location_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库位跨域/历史编码映射表';

