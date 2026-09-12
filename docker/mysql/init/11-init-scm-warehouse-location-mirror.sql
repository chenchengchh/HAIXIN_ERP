USE scm_db;

CREATE TABLE IF NOT EXISTS `scm_warehouse` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `warehouse_code` varchar(64) NOT NULL,
  `warehouse_name` varchar(128) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `manager` varchar(64) DEFAULT NULL,
  `contact` varchar(64) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scm_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SCM 仓库镜像（Owner=WMS）';

CREATE TABLE IF NOT EXISTS `scm_location` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `location_code` varchar(64) NOT NULL,
  `location_name` varchar(128) DEFAULT NULL,
  `warehouse_code` varchar(64) DEFAULT NULL,
  `zone_code` varchar(64) DEFAULT NULL,
  `location_type_code` varchar(64) DEFAULT NULL,
  `status` varchar(8) DEFAULT NULL,
  `remark` varchar(512) DEFAULT NULL,
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scm_location_code` (`location_code`),
  KEY `idx_scm_location_warehouse` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SCM 库位镜像（Owner=WMS）';

