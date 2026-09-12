package com.hxcoe.erp.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.dto.erp.IntegrationVoucherDTO;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.FinanceEntity;
import com.hxcoe.erp.entity.FixedAssetEntity;
import com.hxcoe.erp.entity.FixedAssetEamMappingEntity;
import com.hxcoe.erp.entity.WriteOffEntity;
import com.hxcoe.erp.entity.CostCalculationEntity;
import com.hxcoe.erp.entity.CostAccountingRecordEntity;
import com.hxcoe.erp.model.GeneralLedgerDTO;
import com.hxcoe.erp.repository.CostAccountingRecordRepository;
import com.hxcoe.erp.repository.FinanceRepository;
import com.hxcoe.erp.repository.FixedAssetEamMappingRepository;
import com.hxcoe.erp.client.EamAssetClient;
import com.hxcoe.erp.support.ResultDataExtractor;
import com.hxcoe.erp.service.FinanceService;
import com.hxcoe.erp.service.FixedAssetService;
import com.hxcoe.common.result.Result;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务模块控制器
 */
@RestController
@RequestMapping("/api/v1/erp/finance")
@Validated
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private CostAccountingRecordRepository costAccountingRecordRepository;

    @Autowired
    private FinanceRepository financeRepository;

    @Autowired
    private FixedAssetEamMappingRepository fixedAssetEamMappingRepository;

    @Autowired
    private EamAssetClient eamAssetClient;

    /**
     * 创建财务凭证 (用于外部系统集成)
     */
    @PostMapping("/vouchers/integration")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createVoucher(@RequestBody IntegrationVoucherDTO voucher) {
        FinanceEntity entity = new FinanceEntity();
        entity.setFinanceNo(voucher.getVoucherCode());
        entity.setTransactionDate(voucher.getVoucherDate() != null ? voucher.getVoucherDate() : LocalDateTime.now());
        entity.setDescription(voucher.getDescription());
        entity.setAmount(voucher.getAmount() != null ? voucher.getAmount() : BigDecimal.ZERO);
        entity.setCurrency(voucher.getCurrency() != null && !voucher.getCurrency().isBlank() ? voucher.getCurrency() : "CNY");
        
        String type = voucher.getVoucherType() != null ? voucher.getVoucherType().trim().toUpperCase() : "";
        if (type.contains("IN")) {
            entity.setTransactionType(1);
        } else if (type.contains("OUT")) {
            entity.setTransactionType(2);
        } else {
            entity.setTransactionType(0);
        }
        
        entity.setStatus(1);
        entity.setCreatedBy(voucher.getSourceSystem());
        entity.setUpdatedBy(voucher.getSourceSystem());
        entity.setIsDeleted(0);
        financeService.createFinance(entity);
        
        return success("凭证创建成功", null);
    }

    /**
     * 创建财务记录

     *
     * @param financeEntity 财务实体
     * @return 创建结果
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FinanceEntity> createFinance(@RequestBody @Valid FinanceEntity financeEntity) {
        FinanceEntity result = financeService.createFinance(financeEntity);
        return success("记录查询成功", result);
    }

    /**
     * 根据ID查询财务记录
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<FinanceEntity> getFinanceById(@PathVariable Long id) {
        FinanceEntity result = financeService.getFinanceById(id);
        if (result != null) {
            return success("记录查询成功", result);
        }
        return notFound("财务记录不存在");
    }

    /**
     * 根据财务单号查询财务记录
     *
     * @param financeNo 财务单号
     * @return 查询结果
     */
    @GetMapping("/no/{financeNo}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<FinanceEntity> getFinanceByNo(@PathVariable String financeNo) {
        FinanceEntity result = financeService.getFinanceByNo(financeNo);
        if (result != null) {
            return success("记录查询成功", result);
        }
        return notFound("财务记录不存在");
    }

    /**
     * 更新财务记录
     *
     * @param financeEntity 财务实体
     * @return 更新结果
     */
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<FinanceEntity> updateFinance(@RequestBody @Valid FinanceEntity financeEntity) {
        FinanceEntity result = financeService.updateFinance(financeEntity);
        return success("记录查询成功", result);
    }

    /**
     * 删除财务记录
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteFinance(@PathVariable Long id) {
        boolean result = financeService.deleteFinance(id);
        if (result) {
            return success("记录删除成功", null);
        }
        return notFound("财务记录不存在");
    }

    /**
     * 分页查询财务记录
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param transactionType 交易类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResult<FinanceEntity>> getFinanceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        Integer tx = null;
        if (transactionType != null && !transactionType.isBlank()) {
            String s = transactionType.trim().toLowerCase();
            if ("receivable".equals(s)) tx = 1;
            else if ("payable".equals(s)) tx = 2;
            else {
                try { tx = Integer.parseInt(s); } catch (Exception ignored) {}
            }
        }
        PageResult<FinanceEntity> result = financeService.getFinanceList(page, size, tx, status, startDate, endDate);
        return success("记录查询成功", result);
    }

    @PostMapping("/receivable/{id}/collection-notice")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Map<String, Object>> sendReceivableCollectionNotice(@PathVariable Long id) {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("finance_id", id);
        data.put("notice_id", "CN" + System.currentTimeMillis());
        data.put("sent_at", LocalDateTime.now());
        return success("催款通知已发送", data);
    }

    @GetMapping("/receivable/reports/aging")
    public ApiResponse<Map<String, Object>> receivableAgingReport() {
        List<FinanceEntity> rows = financeRepository.findAll((root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("isDeleted"), 0));
            ps.add(cb.equal(root.get("transactionType"), 1));
            return cb.and(ps.toArray(new Predicate[0]));
        });

        Map<String, Map<String, Object>> bucket = new HashMap<>();
        bucket.put("0-30", new HashMap<>(Map.of("age_group", "0-30天", "count", 0L, "amount", BigDecimal.ZERO)));
        bucket.put("31-60", new HashMap<>(Map.of("age_group", "31-60天", "count", 0L, "amount", BigDecimal.ZERO)));
        bucket.put("61-90", new HashMap<>(Map.of("age_group", "61-90天", "count", 0L, "amount", BigDecimal.ZERO)));
        bucket.put("91-180", new HashMap<>(Map.of("age_group", "91-180天", "count", 0L, "amount", BigDecimal.ZERO)));
        bucket.put("180+", new HashMap<>(Map.of("age_group", "180天以上", "count", 0L, "amount", BigDecimal.ZERO)));

        List<Map<String, Object>> details = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (FinanceEntity e : rows) {
            long ageDays = e.getTransactionDate() != null ? ChronoUnit.DAYS.between(e.getTransactionDate(), now) : 0;
            BigDecimal amt = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;
            String key;
            if (ageDays <= 30) key = "0-30";
            else if (ageDays <= 60) key = "31-60";
            else if (ageDays <= 90) key = "61-90";
            else if (ageDays <= 180) key = "91-180";
            else key = "180+";

            Map<String, Object> b = bucket.get(key);
            b.put("count", ((Long) b.get("count")) + 1);
            b.put("amount", ((BigDecimal) b.get("amount")).add(amt));

            Map<String, Object> d = new HashMap<>();
            d.put("finance_no", e.getFinanceNo());
            d.put("amount", amt);
            d.put("transaction_date", e.getTransactionDate());
            d.put("age_days", ageDays);
            d.put("customer_name", e.getCreatedBy() != null && !e.getCreatedBy().isBlank() ? e.getCreatedBy() : "默认客户");
            d.put("status", e.getStatus());
            details.add(d);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> b : bucket.values()) {
            total = total.add((BigDecimal) b.get("amount"));
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        for (String k : List.of("0-30", "31-60", "61-90", "91-180", "180+")) {
            Map<String, Object> b = bucket.get(k);
            BigDecimal amount = (BigDecimal) b.get("amount");
            long count = (Long) b.get("count");
            Map<String, Object> out = new HashMap<>();
            out.put("age_group", b.get("age_group"));
            out.put("count", count);
            out.put("amount", amount);
            out.put("percentage", total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, BigDecimal.ROUND_HALF_UP));
            distribution.add(out);
        }

        details.sort(Comparator.comparingLong(m -> -((Number) m.get("age_days")).longValue()));
        Map<String, Object> data = new HashMap<>();
        data.put("distribution", distribution);
        data.put("details", details);
        return success("操作成功", data);
    }

    @GetMapping("/receivable/reports/forecast")
    public ApiResponse<Map<String, Object>> receivableForecastReport() {
        List<FinanceEntity> rows = financeRepository.findAll((root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("isDeleted"), 0));
            ps.add(cb.equal(root.get("transactionType"), 1));
            return cb.and(ps.toArray(new Predicate[0]));
        });

        LocalDateTime now = LocalDateTime.now();
        Map<String, BigDecimal> byWeek = new HashMap<>();
        for (FinanceEntity e : rows) {
            LocalDateTime base = e.getTransactionDate() != null ? e.getTransactionDate() : now;
            LocalDateTime due = base.plusDays(30);
            long days = ChronoUnit.DAYS.between(now, due);
            if (days < 0 || days > 30) continue;
            int bucketIdx = (int) (days / 7);
            String label = "第" + (bucketIdx + 1) + "周";
            byWeek.put(label, byWeek.getOrDefault(label, BigDecimal.ZERO).add(e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO));
        }
        List<Map<String, Object>> points = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String label = "第" + i + "周";
            Map<String, Object> p = new HashMap<>();
            p.put("label", label);
            p.put("amount", byWeek.getOrDefault(label, BigDecimal.ZERO));
            points.add(p);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("points", points);
        return success("操作成功", data);
    }

    @GetMapping("/receivable/reports/credit")
    public ApiResponse<List<Map<String, Object>>> receivableCreditReport() {
        List<FinanceEntity> rows = financeRepository.findAll((root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("isDeleted"), 0));
            ps.add(cb.equal(root.get("transactionType"), 1));
            return cb.and(ps.toArray(new Predicate[0]));
        });

        LocalDateTime now = LocalDateTime.now();
        Map<String, Map<String, Object>> perCustomer = new HashMap<>();
        for (FinanceEntity e : rows) {
            String customer = e.getCreatedBy() != null && !e.getCreatedBy().isBlank() ? e.getCreatedBy() : "默认客户";
            Map<String, Object> c = perCustomer.computeIfAbsent(customer, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("customer_name", k);
                m.put("overdue_count", 0L);
                m.put("overdue_amount", BigDecimal.ZERO);
                return m;
            });

            long ageDays = e.getTransactionDate() != null ? ChronoUnit.DAYS.between(e.getTransactionDate(), now) : 0;
            if (ageDays > 30) {
                c.put("overdue_count", ((Long) c.get("overdue_count")) + 1);
                c.put("overdue_amount", ((BigDecimal) c.get("overdue_amount")).add(e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO));
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> c : perCustomer.values()) {
            BigDecimal overdue = (BigDecimal) c.get("overdue_amount");
            long count = (Long) c.get("overdue_count");
            int score = 100;
            if (overdue.compareTo(BigDecimal.ZERO) > 0) {
                score -= Math.min(60, overdue.divide(BigDecimal.valueOf(10000), 0, BigDecimal.ROUND_HALF_UP).intValue() * 5);
            }
            score -= (int) Math.min(30, count * 3);
            score = Math.max(0, score);
            String level;
            String levelType;
            if (score >= 90) { level = "优秀"; levelType = "success"; }
            else if (score >= 80) { level = "良好"; levelType = "primary"; }
            else if (score >= 70) { level = "一般"; levelType = "warning"; }
            else { level = "较差"; levelType = "danger"; }

            Map<String, Object> row = new HashMap<>(c);
            row.put("credit_score", score);
            row.put("credit_level", level);
            row.put("credit_level_type", levelType);
            out.add(row);
        }

        out.sort((a, b) -> Integer.compare(((Number) b.get("credit_score")).intValue(), ((Number) a.get("credit_score")).intValue()));
        return success("操作成功", out);
    }

    @GetMapping("/payable/reports/plan")
    public ApiResponse<Map<String, Object>> payablePlanReport() {
        List<FinanceEntity> rows = financeRepository.findAll((root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("isDeleted"), 0));
            ps.add(cb.equal(root.get("transactionType"), 2));
            return cb.and(ps.toArray(new Predicate[0]));
        });

        LocalDateTime now = LocalDateTime.now();
        Map<String, BigDecimal> byWeek = new HashMap<>();
        for (FinanceEntity e : rows) {
            LocalDateTime base = e.getTransactionDate() != null ? e.getTransactionDate() : now;
            LocalDateTime due = base.plusDays(30);
            long days = ChronoUnit.DAYS.between(now, due);
            if (days < 0 || days > 30) continue;
            int bucketIdx = (int) (days / 7);
            String label = "第" + (bucketIdx + 1) + "周";
            byWeek.put(label, byWeek.getOrDefault(label, BigDecimal.ZERO).add(e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO));
        }
        List<Map<String, Object>> points = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String label = "第" + i + "周";
            Map<String, Object> p = new HashMap<>();
            p.put("label", label);
            p.put("amount", byWeek.getOrDefault(label, BigDecimal.ZERO));
            points.add(p);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("points", points);
        return success("操作成功", data);
    }

    @GetMapping("/payable/reports/reconciliation")
    public ApiResponse<List<Map<String, Object>>> payableReconciliationReport() {
        List<FinanceEntity> rows = financeRepository.findAll((root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("isDeleted"), 0));
            ps.add(cb.equal(root.get("transactionType"), 2));
            return cb.and(ps.toArray(new Predicate[0]));
        });

        Map<String, Map<String, Object>> perSupplier = new HashMap<>();
        for (FinanceEntity e : rows) {
            String supplier = e.getCreatedBy() != null && !e.getCreatedBy().isBlank() ? e.getCreatedBy() : "默认供应商";
            Map<String, Object> s = perSupplier.computeIfAbsent(supplier, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("supplier_name", k);
                m.put("count", 0L);
                m.put("amount", BigDecimal.ZERO);
                return m;
            });
            s.put("count", ((Long) s.get("count")) + 1);
            s.put("amount", ((BigDecimal) s.get("amount")).add(e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO));
        }
        List<Map<String, Object>> out = new ArrayList<>(perSupplier.values());
        out.sort((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")));
        return success("操作成功", out);
    }

    @GetMapping("/payable/reports/aging")
    public ApiResponse<Map<String, Object>> payableAgingReport() {
        List<FinanceEntity> rows = financeRepository.findAll((root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("isDeleted"), 0));
            ps.add(cb.equal(root.get("transactionType"), 2));
            return cb.and(ps.toArray(new Predicate[0]));
        });

        Map<String, Map<String, Object>> bucket = new HashMap<>();
        bucket.put("0-30", new HashMap<>(Map.of("age_group", "0-30天", "count", 0L, "amount", BigDecimal.ZERO)));
        bucket.put("31-60", new HashMap<>(Map.of("age_group", "31-60天", "count", 0L, "amount", BigDecimal.ZERO)));
        bucket.put("61-90", new HashMap<>(Map.of("age_group", "61-90天", "count", 0L, "amount", BigDecimal.ZERO)));
        bucket.put("91-180", new HashMap<>(Map.of("age_group", "91-180天", "count", 0L, "amount", BigDecimal.ZERO)));
        bucket.put("180+", new HashMap<>(Map.of("age_group", "180天以上", "count", 0L, "amount", BigDecimal.ZERO)));

        LocalDateTime now = LocalDateTime.now();
        for (FinanceEntity e : rows) {
            long ageDays = e.getTransactionDate() != null ? ChronoUnit.DAYS.between(e.getTransactionDate(), now) : 0;
            BigDecimal amt = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;
            String key;
            if (ageDays <= 30) key = "0-30";
            else if (ageDays <= 60) key = "31-60";
            else if (ageDays <= 90) key = "61-90";
            else if (ageDays <= 180) key = "91-180";
            else key = "180+";

            Map<String, Object> b = bucket.get(key);
            b.put("count", ((Long) b.get("count")) + 1);
            b.put("amount", ((BigDecimal) b.get("amount")).add(amt));
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> b : bucket.values()) {
            total = total.add((BigDecimal) b.get("amount"));
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        for (String k : List.of("0-30", "31-60", "61-90", "91-180", "180+")) {
            Map<String, Object> b = bucket.get(k);
            BigDecimal amount = (BigDecimal) b.get("amount");
            long count = (Long) b.get("count");
            Map<String, Object> out = new HashMap<>();
            out.put("age_group", b.get("age_group"));
            out.put("count", count);
            out.put("amount", amount);
            out.put("percentage", total.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, BigDecimal.ROUND_HALF_UP));
            distribution.add(out);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("distribution", distribution);
        return success("操作成功", data);
    }

    /**
     * 计算指定日期范围内的总收入
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总收入
     */
    @GetMapping("/income")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<BigDecimal> calculateTotalIncome(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        BigDecimal result = financeService.calculateTotalIncome(startDate, endDate);
        return success("操作成功", result);
    }

    /**
     * 计算指定日期范围内的总支出
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 总支出
     */
    @GetMapping("/expense")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<BigDecimal> calculateTotalExpense(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        BigDecimal result = financeService.calculateTotalExpense(startDate, endDate);
        return success("操作成功", result);
    }

    /**
     * 测试财务模块接口
     *
     * @return 测试结果
     */
    @GetMapping("/test")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> testFinance() {
        String result = financeService.testFinance();
        return success("操作成功", result);
    }

    // 核销相关接口
    /**
     * 创建核销记录
     *
     * @param writeOffEntity 核销实体
     * @return 创建结果
     */
    @PostMapping("/write-off")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WriteOffEntity> createWriteOff(@RequestBody @Valid WriteOffEntity writeOffEntity) {
        WriteOffEntity result = financeService.createWriteOff(writeOffEntity);
        return success("记录查询成功", result);
    }

    /**
     * 获取核销记录列表
     *
     * @param page 页码
     * @param size 每页条数
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping("/write-off/list")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResult<WriteOffEntity>> getWriteOffList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        PageResult<WriteOffEntity> result = financeService.getWriteOffList(page, size, status);
        return success("记录查询成功", result);
    }

    /**
     * 根据ID获取核销记录
     *
     * @param id 核销ID
     * @return 查询结果
     */
    @GetMapping("/write-off/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<WriteOffEntity> getWriteOffById(@PathVariable Long id) {
        WriteOffEntity result = financeService.getWriteOffById(id);
        if (result == null) {
            return notFound("记录不存在");
        }
        return success("记录查询成功", result);
    }

    // 成本核算相关接口
    /**
     * 执行成本核算
     *
     * @param costCalculationEntity 成本核算实体
     * @return 核算结果
     */
    @PostMapping("/cost-calculation")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CostCalculationEntity> calculateCost(@RequestBody @Valid CostCalculationEntity costCalculationEntity) {
        CostCalculationEntity result = financeService.calculateCost(costCalculationEntity);
        return success("操作成功", result);
    }

    /**
     * 获取成本核算结果
     *
     * @param calculationId 核算ID
     * @return 核算结果
     */
    @GetMapping("/cost-results/{calculationId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CostCalculationEntity> getCostResults(@PathVariable Long calculationId) {
        CostCalculationEntity result = financeService.getCostResults(calculationId);
        if (result == null) {
            return notFound("记录不存在");
        }
        return success("操作成功", result);
    }

    @GetMapping("/cost-results")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CostCalculationEntity> getCostResultsByParam(@RequestParam(required = false) Long calculationId) {
        if (calculationId == null) {
            return success("操作成功", null);
        }
        CostCalculationEntity result = financeService.getCostResults(calculationId);
        if (result == null) {
            return notFound("记录不存在");
        }
        return success("操作成功", result);
    }

    @GetMapping("/cost-accounting")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResult<CostAccountingRecordEntity>> getCostAccountingList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String costType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate
    ) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1));
        Specification<CostAccountingRecordEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (productCode != null && !productCode.isBlank()) {
                predicates.add(cb.like(root.get("productCode"), "%" + productCode.trim() + "%"));
            }
            if (productName != null && !productName.isBlank()) {
                predicates.add(cb.like(root.get("productName"), "%" + productName.trim() + "%"));
            }
            if (costType != null && !costType.isBlank()) {
                predicates.add(cb.equal(root.get("costType"), costType.trim()));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("costDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("costDate"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CostAccountingRecordEntity> pageData = costAccountingRecordRepository.findAll(spec, pageable);
        return success("操作成功", PageResult.build(pageData.getTotalElements(), size, page, pageData.getContent()));
    }

    @PostMapping("/cost-accounting")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CostAccountingRecordEntity> createCostAccounting(@RequestBody CostAccountingRecordEntity entity) {
        if (entity.getMaterialCost() == null) entity.setMaterialCost(BigDecimal.ZERO);
        if (entity.getLaborCost() == null) entity.setLaborCost(BigDecimal.ZERO);
        if (entity.getOverheadCost() == null) entity.setOverheadCost(BigDecimal.ZERO);
        if (entity.getTotalCost() == null) {
            entity.setTotalCost(entity.getMaterialCost().add(entity.getLaborCost()).add(entity.getOverheadCost()));
        }
        if (entity.getUnitCost() == null) entity.setUnitCost(BigDecimal.ZERO);
        if (entity.getCostDate() == null) entity.setCostDate(LocalDateTime.now());
        if (entity.getCreatedTime() == null) entity.setCreatedTime(LocalDateTime.now());
        if (entity.getUpdatedTime() == null) entity.setUpdatedTime(LocalDateTime.now());
        if (entity.getIsDeleted() == null) entity.setIsDeleted(0);
        CostAccountingRecordEntity saved = costAccountingRecordRepository.save(entity);
        return success("操作成功", saved);
    }

    @PutMapping("/cost-accounting/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CostAccountingRecordEntity> updateCostAccounting(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        CostAccountingRecordEntity existing = costAccountingRecordRepository.findById(id).orElse(null);
        if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) {
            return notFound("记录不存在");
        }
        if (body.get("product_code") != null) existing.setProductCode(String.valueOf(body.get("product_code")));
        if (body.get("product_name") != null) existing.setProductName(String.valueOf(body.get("product_name")));
        if (body.get("cost_type") != null) existing.setCostType(String.valueOf(body.get("cost_type")));
        if (body.get("material_cost") != null) existing.setMaterialCost(new BigDecimal(String.valueOf(body.get("material_cost"))));
        if (body.get("labor_cost") != null) existing.setLaborCost(new BigDecimal(String.valueOf(body.get("labor_cost"))));
        if (body.get("overhead_cost") != null) existing.setOverheadCost(new BigDecimal(String.valueOf(body.get("overhead_cost"))));
        if (body.get("total_cost") != null) existing.setTotalCost(new BigDecimal(String.valueOf(body.get("total_cost"))));
        if (body.get("unit_cost") != null) existing.setUnitCost(new BigDecimal(String.valueOf(body.get("unit_cost"))));
        if (body.get("remark") != null) existing.setRemark(String.valueOf(body.get("remark")));
        existing.setUpdatedTime(LocalDateTime.now());
        CostAccountingRecordEntity saved = costAccountingRecordRepository.save(existing);
        return success("操作成功", saved);
    }

    @DeleteMapping("/cost-accounting/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteCostAccounting(@PathVariable Long id) {
        CostAccountingRecordEntity existing = costAccountingRecordRepository.findById(id).orElse(null);
        if (existing == null) {
            return success("删除成功", null);
        }
        existing.setIsDeleted(1);
        existing.setUpdatedTime(LocalDateTime.now());
        costAccountingRecordRepository.save(existing);
        return success("删除成功", null);
    }

    // 固定资产管理相关接口
    @Autowired
    private FixedAssetService fixedAssetService;

    /**
     * 获取固定资产列表
     *
     * @param page 页码
     * @param size 每页条数
     * @param assetName 资产名称
     * @param assetCategory 资产类别
     * @param status 状态
     * @param usingDepartment 使用部门
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/fixed-assets")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResult<FixedAssetEntity>> getFixedAssets(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) String assetCategory,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String usingDepartment,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        PageResult<FixedAssetEntity> result = fixedAssetService.getFixedAssetList(
                page, size, assetName, assetCategory, status, usingDepartment, startDate, endDate);
        return success("操作成功", result);
    }

    /**
     * 创建固定资产
     *
     * @param fixedAssetEntity 固定资产实体
     * @return 创建结果
     */
    @PostMapping("/fixed-assets")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FixedAssetEntity> createFixedAsset(@RequestBody @Valid FixedAssetEntity fixedAssetEntity) {
        FixedAssetEntity result = fixedAssetService.createFixedAsset(fixedAssetEntity);
        return success("操作成功", result);
    }

    /**
     * 根据ID获取固定资产
     *
     * @param id 固定资产ID
     * @return 查询结果
     */
    @GetMapping("/fixed-assets/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<FixedAssetEntity> getFixedAssetById(@PathVariable Long id) {
        FixedAssetEntity result = fixedAssetService.getFixedAssetById(id);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("固定资产不存在");
    }

    /**
     * 更新固定资产
     *
     * @param id 固定资产ID
     * @param fixedAssetEntity 固定资产实体
     * @return 更新结果
     */
    @PutMapping("/fixed-assets/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<FixedAssetEntity> updateFixedAsset(@PathVariable Long id, @RequestBody @Valid FixedAssetEntity fixedAssetEntity) {
        fixedAssetEntity.setId(id);
        FixedAssetEntity result = fixedAssetService.updateFixedAsset(fixedAssetEntity);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("固定资产不存在");
    }

    /**
     * 删除固定资产
     *
     * @param id 固定资产ID
     * @return 删除结果
     */
    @DeleteMapping("/fixed-assets/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteFixedAsset(@PathVariable Long id) {
        boolean result = fixedAssetService.deleteFixedAsset(id);
        if (result) {
            return success("操作成功", null);
        }
        return notFound("固定资产不存在");
    }

    /**
     * 固定资产折旧
     *
     * @param id 固定资产ID
     * @param depreciationDate 折旧日期
     * @return 折旧结果
     */
    @PostMapping("/fixed-assets/{id}/depreciation")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<FixedAssetEntity> calculateDepreciation(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime depreciationDate) {
        FixedAssetEntity result = fixedAssetService.calculateDepreciation(id, depreciationDate);
        if (result != null) {
            return success("操作成功", result);
        }
        return badRequest("固定资产不存在或状态不可折旧");
    }

    /**
     * 批量计提固定资产折旧
     *
     * @param assetIds 固定资产ID列表
     * @param depreciationDate 折旧日期
     * @return 折旧结果
     */
    @PostMapping("/fixed-assets/depreciation")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<FixedAssetEntity>> batchCalculateDepreciation(
            @RequestBody List<Long> assetIds,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime depreciationDate) {
        List<FixedAssetEntity> result = fixedAssetService.batchCalculateDepreciation(assetIds, depreciationDate);
        return success("操作成功", result);
    }

    /**
     * 自动计提固定资产折旧
     *
     * @param depreciationDate 折旧日期
     * @return 折旧结果
     */
    @PostMapping("/fixed-assets/depreciation/auto")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<FixedAssetEntity>> autoCalculateDepreciation(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime depreciationDate) {
        List<FixedAssetEntity> result = fixedAssetService.autoCalculateDepreciation(depreciationDate);
        return success("操作成功", result);
    }

    /**
     * 固定资产处置
     *
     * @param id 固定资产ID
     * @param disposalDate 处置日期
     * @param disposalReason 处置原因
     * @param disposalAmount 处置金额
     * @return 处置结果
     */
    @PostMapping("/fixed-assets/{id}/dispose")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<FixedAssetEntity> disposeFixedAsset(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime disposalDate,
            @RequestParam String disposalReason,
            @RequestParam BigDecimal disposalAmount) {
        FixedAssetEntity result = fixedAssetService.disposeFixedAsset(id, disposalDate, disposalReason, disposalAmount);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("固定资产不存在");
    }

    /**
     * 固定资产盘点
     *
     * @param inventoryInfo 盘点信息
     * @return 盘点结果
     */
    @PostMapping("/fixed-assets/inventory")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<FixedAssetEntity>> inventoryFixedAssets(
            @RequestBody Map<String, Object> inventoryInfo) {
        Object assetIdsObj = inventoryInfo.get("assetIds");
        List<Long> assetIds = new ArrayList<>();
        if (assetIdsObj instanceof List) {
            for (Object id : (List<?>) assetIdsObj) {
                if (id instanceof Number) {
                    assetIds.add(((Number) id).longValue());
                }
            }
        }
        
        String dateStr = (String) inventoryInfo.get("inventoryDate");
        LocalDateTime inventoryDate = dateStr != null ? LocalDateTime.parse(dateStr) : LocalDateTime.now();
        String inventoryPerson = (String) inventoryInfo.get("inventoryPerson");
        
        List<FixedAssetEntity> result = fixedAssetService.inventoryFixedAssets(assetIds, inventoryDate, inventoryPerson);
        return success("操作成功", result);
    }

    /**
     * 获取固定资产类别列表
     *
     * @return 资产类别列表
     */
    @GetMapping("/fixed-assets/categories")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<String>> getAssetCategories() {
        List<String> result = fixedAssetService.getAssetCategories();
        return success("操作成功", result);
    }

    /**
     * 统计固定资产数量
     *
     * @param status 状态
     * @return 统计结果
     */
    @GetMapping("/fixed-assets/count")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Long> countFixedAssets(@RequestParam(required = false) Integer status) {
        long result = fixedAssetService.countFixedAssets(status);
        return success("操作成功", result);
    }

    @PostMapping("/fixed-assets/{id}/eam/link")
    public ApiResponse<Map<String, Object>> linkFixedAssetEam(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object v = body != null ? body.get("eam_asset_id") : null;
        Long eamAssetId = null;
        if (v instanceof Number n) eamAssetId = n.longValue();
        else if (v != null) {
            try { eamAssetId = Long.parseLong(String.valueOf(v)); } catch (Exception ignored) {}
        }
        if (eamAssetId == null) return badRequest("eam_asset_id不能为空");

        FixedAssetEntity asset = fixedAssetService.getFixedAssetById(id);
        if (asset == null) return notFound("固定资产不存在");

        FixedAssetEamMappingEntity mapping = fixedAssetEamMappingRepository.findByFixedAssetId(id).orElse(null);
        if (mapping == null) {
            mapping = new FixedAssetEamMappingEntity();
            mapping.setFixedAssetId(id);
        }
        mapping.setEamAssetId(eamAssetId);
        fixedAssetEamMappingRepository.save(mapping);

        Map<String, Object> data = new HashMap<>();
        data.put("fixed_asset_id", id);
        data.put("eam_asset_id", eamAssetId);
        return success("操作成功", data);
    }

    @GetMapping("/fixed-assets/{id}/eam")
    public ApiResponse<Map<String, Object>> getFixedAssetEam(@PathVariable Long id) {
        FixedAssetEamMappingEntity mapping = fixedAssetEamMappingRepository.findByFixedAssetId(id).orElse(null);
        Map<String, Object> data = new HashMap<>();
        data.put("mapping", mapping);
        if (mapping != null && mapping.getEamAssetId() != null) {
            Result<Object> res = eamAssetClient.getById(mapping.getEamAssetId());
            data.put("eam_asset", res != null ? res.getData() : null);
        } else {
            data.put("eam_asset", null);
        }
        return success("操作成功", data);
    }

    @PostMapping("/fixed-assets/eam/sync")
    public ApiResponse<Map<String, Object>> syncFixedAssetsFromEam() {
        Result<Object> res = eamAssetClient.list();
        List<Map<String, Object>> rows = ResultDataExtractor.asListOfMap(res != null ? res.getData() : null);
        int created = 0;
        int linked = 0;
        for (Map<String, Object> row : rows) {
            Object idVal = row.get("id");
            Long eamId = null;
            if (idVal instanceof Number n) eamId = n.longValue();
            else if (idVal != null) {
                try { eamId = Long.parseLong(String.valueOf(idVal)); } catch (Exception ignored) {}
            }
            if (eamId == null) continue;

            String code = row.get("code") != null ? String.valueOf(row.get("code")) : null;
            if (code == null || code.isBlank()) code = "EAM-" + eamId;

            FixedAssetEntity existing = fixedAssetService.getFixedAssetByNo(code);
            if (existing == null) {
                FixedAssetEntity fa = new FixedAssetEntity();
                fa.setAssetNo(code);
                fa.setAssetName(row.get("name") != null ? String.valueOf(row.get("name")) : code);
                fa.setAssetCategory(row.get("categoryName") != null ? String.valueOf(row.get("categoryName")) : "EAM");
                fa.setSpecification(row.get("model") != null ? String.valueOf(row.get("model")) : null);
                Object purchaseDate = row.get("purchaseDate");
                if (purchaseDate != null) {
                    try {
                        java.time.LocalDate d = java.time.LocalDate.parse(String.valueOf(purchaseDate));
                        fa.setPurchaseDate(d.atStartOfDay());
                    } catch (Exception ignored) {
                        fa.setPurchaseDate(LocalDateTime.now());
                    }
                } else {
                    fa.setPurchaseDate(LocalDateTime.now());
                }
                Object ov = row.get("originalValue");
                java.math.BigDecimal originalValue = java.math.BigDecimal.ZERO;
                if (ov instanceof Number n) originalValue = java.math.BigDecimal.valueOf(n.doubleValue());
                else if (ov != null) {
                    try { originalValue = new java.math.BigDecimal(String.valueOf(ov)); } catch (Exception ignored) {}
                }
                fa.setOriginalValue(originalValue);
                fa.setExpectedUsageYears(5);
                fa.setExpectedResidualRate(new java.math.BigDecimal("0.05"));
                fa.setResidualValue(originalValue.multiply(new java.math.BigDecimal("0.05")));
                fa.setMonthlyDepreciation(fixedAssetService.calculateMonthlyDepreciation(originalValue, fa.getResidualValue(), fa.getExpectedUsageYears()));
                fa.setAccumulatedDepreciation(java.math.BigDecimal.ZERO);
                fa.setNetValue(originalValue);
                String st = row.get("status") != null ? String.valueOf(row.get("status")) : "";
                int status = 1;
                if ("stopped".equalsIgnoreCase(st)) status = 2;
                else if ("scrapped".equalsIgnoreCase(st)) status = 3;
                fa.setStatus(status);
                fa.setLocation(row.get("location") != null ? String.valueOf(row.get("location")) : null);
                fa.setIsDeleted(0);
                fa.setCreatedTime(LocalDateTime.now());
                fa.setUpdatedTime(LocalDateTime.now());
                FixedAssetEntity saved = fixedAssetService.createFixedAsset(fa);
                existing = saved;
                created++;
            }

            FixedAssetEamMappingEntity mapping = fixedAssetEamMappingRepository.findByFixedAssetId(existing.getId()).orElse(null);
            if (mapping == null) {
                mapping = new FixedAssetEamMappingEntity();
                mapping.setFixedAssetId(existing.getId());
                mapping.setEamAssetId(eamId);
                fixedAssetEamMappingRepository.save(mapping);
                linked++;
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("created_fixed_assets", created);
        data.put("linked_fixed_assets", linked);
        data.put("eam_assets", rows.size());
        return success("操作成功", data);
    }

    /**
     * 获取总账数据
     *
     * @param page 页码
     * @param size 每页条数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/general-ledger")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResult<GeneralLedgerDTO>> getGeneralLedger(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        PageResult<GeneralLedgerDTO> result = financeService.getGeneralLedger(page, size, startDate, endDate);
        return success("操作成功", result);
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.error(400, message);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
