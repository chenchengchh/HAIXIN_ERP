-- ========================================
-- Integration DB 初始化脚本
-- 说明：为后续统一承载跨域 Inbox/Outbox/Mapping/Snapshot 预留独立库与账号
-- ========================================
CREATE DATABASE IF NOT EXISTS `integration_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mysql;

CREATE USER IF NOT EXISTS 'integration_app'@'%' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON `integration_db`.* TO 'integration_app'@'%';

FLUSH PRIVILEGES;
