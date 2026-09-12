package com.hxcoe.erp.controller;

import com.hxcoe.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ERP 仪表盘控制器
 * 提供经营统计、收支趋势、订单状态分布、销售趋势、库存周转等聚合数据
 */
@RestController
@RequestMapping("/api/v1/erp/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取仪表盘核心统计数据
     * 通过一次 SQL 聚合所有 COUNT/SUM，避免多次数据库往返
     * @return 统计卡片数据（生产单数、凭证数、物料数、供应商数、凭证金额合计等）
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        logger.info("获取仪表盘统计数据");
        Map<String, Object> stats = new HashMap<>();
        // 主统计：合并为一次条件聚合查询，替代原来 10 次独立 COUNT/SUM
        String combinedSql =
                "SELECT " +
                "  (SELECT COUNT(*) FROM erp_production WHERE is_deleted = 0) AS productionCount, " +
                "  (SELECT COUNT(*) FROM erp_production WHERE is_deleted = 0 AND production_status = 1) AS productionInProgress, " +
                "  (SELECT COUNT(*) FROM erp_voucher WHERE is_deleted = 0) AS voucherCount, " +
                "  (SELECT COUNT(*) FROM erp_material WHERE is_deleted = 0) AS materialCount, " +
                "  (SELECT COUNT(*) FROM erp_supplier WHERE is_deleted = 0) AS supplierCount, " +
                "  (SELECT COUNT(*) FROM erp_warehouse WHERE is_deleted = 0) AS warehouseCount, " +
                "  (SELECT COUNT(*) FROM erp_supply_chain WHERE is_deleted = 0) AS supplyChainCount, " +
                "  (SELECT IFNULL(SUM(debit_total),0) FROM erp_voucher WHERE is_deleted = 0) AS totalDebit, " +
                "  (SELECT IFNULL(SUM(credit_total),0) FROM erp_voucher WHERE is_deleted = 0) AS totalCredit, " +
                "  (SELECT IFNULL(SUM(total_amount),0) FROM erp_supply_chain WHERE is_deleted = 0) AS totalSalesAmount";
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(combinedSql);
            if (row != null) {
                stats.putAll(row);
            }
        } catch (Exception e) {
            logger.warn("仪表盘统计聚合查询失败 error={}", e.getMessage());
            // 失败兜底：返回零值集合，避免 500
            stats.put("productionCount", 0L);
            stats.put("productionInProgress", 0L);
            stats.put("voucherCount", 0L);
            stats.put("materialCount", 0L);
            stats.put("supplierCount", 0L);
            stats.put("warehouseCount", 0L);
            stats.put("supplyChainCount", 0L);
            stats.put("totalDebit", 0);
            stats.put("totalCredit", 0);
            stats.put("totalSalesAmount", 0);
        }
        return Result.success(stats);
    }

    /**
     * 获取收支趋势（按月聚合凭证借贷金额）
     * @param range 时间范围（month/quarter/year，默认 month 取近6个月）
     * @return 收支趋势数据
     */
    @GetMapping("/income-expense")
    public Result<List<Map<String, Object>>> getIncomeExpenseTrend(@RequestParam(defaultValue = "month") String range) {
        logger.info("获取收支趋势, range={}", range);
        int months = switch (range) {
            case "quarter" -> 3;
            case "year" -> 12;
            default -> 6;
        };
        String sql = "SELECT DATE_FORMAT(voucher_date, '%Y-%m') AS period, " +
                "IFNULL(SUM(credit_total),0) AS income, IFNULL(SUM(debit_total),0) AS expense " +
                "FROM erp_voucher WHERE is_deleted = 0 AND voucher_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                "GROUP BY DATE_FORMAT(voucher_date, '%Y-%m') ORDER BY period";
        List<Map<String, Object>> trend = jdbcTemplate.queryForList(sql, months);
        return Result.success(trend);
    }

    /**
     * 获取生产订单状态分布
     * @return 状态分布数据
     */
    @GetMapping("/order-status")
    public Result<List<Map<String, Object>>> getOrderStatusDistribution() {
        logger.info("获取订单状态分布");
        String sql = "SELECT production_status AS status, COUNT(*) AS count " +
                "FROM erp_production WHERE is_deleted = 0 GROUP BY production_status ORDER BY production_status";
        List<Map<String, Object>> distribution = jdbcTemplate.queryForList(sql);
        return Result.success(distribution);
    }

    /**
     * 获取销售趋势（按月聚合供应链业务金额与数量，近12个月）
     * @return 销售趋势数据
     */
    @GetMapping("/sales-trend")
    public Result<List<Map<String, Object>>> getSalesTrend() {
        logger.info("获取销售趋势");
        String sql = "SELECT DATE_FORMAT(created_time, '%Y-%m') AS period, " +
                "IFNULL(SUM(total_amount),0) AS amount, IFNULL(SUM(quantity),0) AS quantity, COUNT(*) AS orderCount " +
                "FROM erp_supply_chain WHERE is_deleted = 0 AND created_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                "GROUP BY DATE_FORMAT(created_time, '%Y-%m') ORDER BY period";
        List<Map<String, Object>> trend = jdbcTemplate.queryForList(sql);
        return Result.success(trend);
    }

    /**
     * 获取库存周转数据（按物料统计库存上下限与安全库存水平）
     * @return 库存周转数据
     */
    @GetMapping("/inventory-turnover")
    public Result<Map<String, Object>> getInventoryTurnover() {
        logger.info("获取库存周转率");
        Map<String, Object> result = new HashMap<>();
        // 物料相关聚合一次完成；供应链单据量独立一次（不同表）
        String materialSql =
                "SELECT " +
                "  COUNT(*) AS materialCount, " +
                "  SUM(CASE WHEN safety_stock IS NOT NULL AND min_stock IS NOT NULL AND min_stock < safety_stock THEN 1 ELSE 0 END) AS belowSafetyStock, " +
                "  IFNULL(SUM(unit_price * IFNULL(safety_stock,0)),0) AS totalStockValue " +
                "FROM erp_material WHERE is_deleted = 0";
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(materialSql);
            if (row != null) {
                result.putAll(row);
            }
        } catch (Exception e) {
            logger.warn("库存物料聚合查询失败 error={}", e.getMessage());
            result.put("materialCount", 0L);
            result.put("belowSafetyStock", 0L);
            result.put("totalStockValue", 0);
        }
        // 近12个月出入库业务量作为周转参考
        result.put("yearInboundOutbound", queryForLong(
                "SELECT COUNT(*) FROM erp_supply_chain WHERE is_deleted = 0 AND created_time >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)"));
        return Result.success(result);
    }

    /**
     * 执行 COUNT/SUM 类查询并返回 long 值
     * @param sql SQL 语句
     * @return 数值结果
     */
    private long queryForLong(String sql) {
        try {
            Long value = jdbcTemplate.queryForObject(sql, Long.class);
            return value == null ? 0L : value;
        } catch (Exception e) {
            logger.warn("仪表盘统计查询失败 sql={} error={}", sql, e.getMessage());
            return 0L;
        }
    }
}
