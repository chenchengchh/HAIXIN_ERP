#!/bin/bash

# MySQL自动备份脚本
# 支持全量备份和增量备份

# 配置参数
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_PASSWORD="root"

# 备份目录
BACKUP_BASE_DIR="/backups"
FULL_BACKUP_DIR="${BACKUP_BASE_DIR}/full"
INCREMENTAL_BACKUP_DIR="${BACKUP_BASE_DIR}/incremental"
LOGS_BACKUP_DIR="${BACKUP_BASE_DIR}/logs"
MANUAL_BACKUP_DIR="${BACKUP_BASE_DIR}/manual"

# 日期格式
DATE=$(date +"%Y%m%d_%H%M%S")
WEEK_DAY=$(date +"%u") # 1-7，周一为1

# 日志文件
LOG_FILE="${LOGS_BACKUP_DIR}/backup_${DATE}.log"

# 备份保留策略
FULL_BACKUP_RETENTION=30  # 全量备份保留30天
INCREMENTAL_BACKUP_RETENTION=7  # 增量备份保留7天
BINLOG_RETENTION=14  # 二进制日志保留14天

# 创建日志目录
mkdir -p "${LOGS_BACKUP_DIR}"

# 日志函数
log() {
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] $1" >> "${LOG_FILE}"
}

# 检查备份目录
check_backup_dirs() {
    log "检查备份目录..."
    mkdir -p "${FULL_BACKUP_DIR}" "${INCREMENTAL_BACKUP_DIR}" "${LOGS_BACKUP_DIR}" "${MANUAL_BACKUP_DIR}"
    log "备份目录检查完成"
}

# 执行全量备份
perform_full_backup() {
    log "开始执行全量备份..."
    BACKUP_FILE="${FULL_BACKUP_DIR}/full_backup_${DATE}.sql.gz"
    
    # 使用mysqldump执行全量备份
    mysqldump -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" \
        --single-transaction \
        --routines \
        --triggers \
        --all-databases \
        --master-data=2 \
        --flush-logs \
        | gzip > "${BACKUP_FILE}"
    
    if [ $? -eq 0 ]; then
        log "全量备份完成: ${BACKUP_FILE}"
        # 更新增量备份基准文件
        echo "${DATE}" > "${BACKUP_BASE_DIR}/last_full_backup.txt"
    else
        log "全量备份失败"
        exit 1
    fi
}

# 执行增量备份
perform_incremental_backup() {
    log "开始执行增量备份..."
    
    # 检查是否存在全量备份
    if [ ! -f "${BACKUP_BASE_DIR}/last_full_backup.txt" ]; then
        log "未找到全量备份，跳过增量备份"
        return
    fi
    
    # 刷新二进制日志，生成新的二进制日志文件
    mysql -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" -e "FLUSH LOGS;" 2>/dev/null
    
    if [ $? -eq 0 ]; then
        log "二进制日志刷新成功"
    else
        log "二进制日志刷新失败"
        return
    fi
}

# 清理过期备份
cleanup_old_backups() {
    log "开始清理过期备份..."
    
    # 清理过期全量备份
    find "${FULL_BACKUP_DIR}" -name "full_backup_*.sql.gz" -mtime +"${FULL_BACKUP_RETENTION}" -delete
    log "已清理 ${FULL_BACKUP_RETENTION} 天前的全量备份"
    
    # 清理过期增量备份
    find "${INCREMENTAL_BACKUP_DIR}" -name "*.sql.gz" -mtime +"${INCREMENTAL_BACKUP_RETENTION}" -delete
    log "已清理 ${INCREMENTAL_BACKUP_RETENTION} 天前的增量备份"
    
    # 清理过期日志
    find "${LOGS_BACKUP_DIR}" -name "backup_*.log" -mtime +30 -delete
    log "已清理30天前的备份日志"
}

# 主函数
main() {
    log "=== MySQL自动备份开始 ==="
    
    # 检查备份目录
    check_backup_dirs
    
    # 每周一执行全量备份，其他时间执行增量备份
    if [ "${WEEK_DAY}" -eq 1 ]; then
        perform_full_backup
    else
        perform_incremental_backup
    fi
    
    # 清理过期备份
    cleanup_old_backups
    
    log "=== MySQL自动备份结束 ==="
}

# 执行主函数
main
