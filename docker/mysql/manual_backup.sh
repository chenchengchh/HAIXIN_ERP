#!/bin/bash

# MySQL手动备份脚本
# 支持全量备份和特定数据库备份

# 配置参数
DB_HOST="localhost"
DB_PORT="3306"
DB_USER="root"
DB_PASSWORD="root"

# 备份目录
BACKUP_BASE_DIR="/backups"
MANUAL_BACKUP_DIR="${BACKUP_BASE_DIR}/manual"
LOGS_BACKUP_DIR="${BACKUP_BASE_DIR}/logs"

# 日期格式
DATE=$(date +"%Y%m%d_%H%M%S")

# 日志文件
LOG_FILE="${LOGS_BACKUP_DIR}/manual_backup_${DATE}.log"

# 显示使用帮助
show_help() {
    echo "MySQL手动备份脚本使用说明："
    echo ""
    echo "用法：bash manual_backup.sh [选项]"
    echo ""
    echo "选项："
    echo "  --all-databases    备份所有数据库（全量备份）"
    echo "  --database DB_NAME 备份指定数据库"
    echo "  --help             显示此帮助信息"
    echo ""
    echo "示例："
    echo "  1. 备份所有数据库："
    echo "     bash manual_backup.sh --all-databases"
    echo ""
    echo "  2. 备份指定数据库："
    echo "     bash manual_backup.sh --database mydb"
    echo ""
}

# 日志函数
log() {
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] $1" >> "${LOG_FILE}"
    echo "$1"
}

# 创建备份目录
check_backup_dirs() {
    log "检查备份目录..."
    mkdir -p "${MANUAL_BACKUP_DIR}" "${LOGS_BACKUP_DIR}"
    log "备份目录检查完成"
}

# 执行全量备份
backup_all_databases() {
    log "开始执行全量备份..."
    BACKUP_FILE="${MANUAL_BACKUP_DIR}/manual_full_backup_${DATE}.sql.gz"
    
    # 使用mysqldump执行全量备份
    mysqldump -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" \
        --single-transaction \
        --routines \
        --triggers \
        --all-databases \
        --master-data=2 \
        | gzip > "${BACKUP_FILE}"
    
    if [ $? -eq 0 ]; then
        log "全量备份完成: ${BACKUP_FILE}"
        log "备份文件大小: $(du -h "${BACKUP_FILE}" | cut -f1)"
    else
        log "全量备份失败"
        exit 1
    fi
}

# 执行特定数据库备份
backup_specific_database() {
    local DB_NAME=$1
    log "开始备份数据库: ${DB_NAME}..."
    BACKUP_FILE="${MANUAL_BACKUP_DIR}/manual_backup_${DB_NAME}_${DATE}.sql.gz"
    
    # 使用mysqldump执行特定数据库备份
    mysqldump -h"${DB_HOST}" -P"${DB_PORT}" -u"${DB_USER}" -p"${DB_PASSWORD}" \
        --single-transaction \
        --routines \
        --triggers \
        "${DB_NAME}" \
        | gzip > "${BACKUP_FILE}"
    
    if [ $? -eq 0 ]; then
        log "数据库 ${DB_NAME} 备份完成: ${BACKUP_FILE}"
        log "备份文件大小: $(du -h "${BACKUP_FILE}" | cut -f1)"
    else
        log "数据库 ${DB_NAME} 备份失败"
        exit 1
    fi
}

# 主函数
main() {
    log "=== MySQL手动备份开始 ==="
    
    # 检查备份目录
    check_backup_dirs
    
    # 解析命令行参数
    if [ $# -eq 0 ]; then
        show_help
        exit 0
    fi
    
    while [ $# -gt 0 ]; do
        case "$1" in
            --all-databases)
                backup_all_databases
                shift
                ;;
            --database)
                if [ -n "$2" ]; then
                    backup_specific_database "$2"
                    shift 2
                else
                    echo "错误：--database选项需要指定数据库名称"
                    show_help
                    exit 1
                fi
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                echo "错误：未知选项 '$1'"
                show_help
                exit 1
                ;;
        esac
    done
    
    log "=== MySQL手动备份结束 ==="
}

# 执行主函数
main "$@"
