USE mysql;

CREATE USER IF NOT EXISTS 'scm_app'@'%' IDENTIFIED BY 'root';
CREATE USER IF NOT EXISTS 'srm_app'@'%' IDENTIFIED BY 'root';

GRANT SELECT ON `srm_db`.`srm_purchase_order` TO 'scm_app'@'%';
GRANT SELECT ON `srm_db`.`srm_purchase_order_item` TO 'scm_app'@'%';

GRANT SELECT ON `scm_db`.`scm_supplier` TO 'srm_app'@'%';
GRANT SELECT ON `erp_db`.`erp_supplier` TO 'srm_app'@'%';

FLUSH PRIVILEGES;

