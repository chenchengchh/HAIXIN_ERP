package com.hxcoe.erp.service.impl;

import com.hxcoe.erp.entity.FinanceEntity;
import com.hxcoe.erp.entity.WriteOffEntity;
import com.hxcoe.erp.entity.CostCalculationEntity;
import com.hxcoe.erp.entity.AccountEntity;
import com.hxcoe.erp.model.GeneralLedgerDTO;
import com.hxcoe.erp.repository.FinanceRepository;
import com.hxcoe.erp.repository.WriteOffRepository;
import com.hxcoe.erp.repository.CostCalculationRepository;
import com.hxcoe.erp.repository.AccountRepository;
import com.hxcoe.erp.repository.VoucherItemRepository;
import com.hxcoe.erp.service.FinanceService;
import com.hxcoe.common.result.PageResult;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务服务实现类
 */
@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private FinanceRepository financeRepository;

    @Autowired
    private WriteOffRepository writeOffRepository;

    @Autowired
    private CostCalculationRepository costCalculationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VoucherItemRepository voucherItemRepository;

    @Override
    public FinanceEntity createFinance(FinanceEntity financeEntity) {
        // 设置默认值
        if (financeEntity.getIsDeleted() == null) {
            financeEntity.setIsDeleted(0);
        }
        if (financeEntity.getCreatedTime() == null) {
            financeEntity.setCreatedTime(LocalDateTime.now());
        }
        if (financeEntity.getUpdatedTime() == null) {
            financeEntity.setUpdatedTime(LocalDateTime.now());
        }
        return financeRepository.save(financeEntity);
    }

    @Override
    public FinanceEntity getFinanceById(Long id) {
        return financeRepository.findById(id).orElse(null);
    }

    @Override
    public FinanceEntity getFinanceByNo(String financeNo) {
        return financeRepository.findByFinanceNo(financeNo);
    }

    @Override
    public FinanceEntity updateFinance(FinanceEntity financeEntity) {
        // 更新时间
        financeEntity.setUpdatedTime(LocalDateTime.now());
        return financeRepository.save(financeEntity);
    }

    @Override
    public boolean deleteFinance(Long id) {
        FinanceEntity financeEntity = financeRepository.findById(id).orElse(null);
        if (financeEntity != null) {
            // 逻辑删除
            financeEntity.setIsDeleted(1);
            financeEntity.setUpdatedTime(LocalDateTime.now());
            financeRepository.save(financeEntity);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<FinanceEntity> getFinanceList(Integer page, Integer size, Integer transactionType, Integer status, LocalDateTime startDate, LocalDateTime endDate) {
        // 构建分页请求
        Pageable pageable = PageRequest.of(page - 1, size);

        // 构建查询条件
        Specification<FinanceEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 过滤逻辑删除
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));

            // 交易类型条件
            if (transactionType != null) {
                predicates.add(criteriaBuilder.equal(root.get("transactionType"), transactionType));
            }

            // 状态条件
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 日期范围条件
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("transactionDate"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<FinanceEntity> financePage = financeRepository.findAll(spec, pageable);

        // 构建分页结果
        return PageResult.build(
                financePage.getTotalElements(),
                size,
                page,
                financePage.getContent()
        );
    }

    @Override
    public BigDecimal calculateTotalIncome(LocalDateTime startDate, LocalDateTime endDate) {
        // SQL 直接聚合，避免全表加载到内存
        BigDecimal total = financeRepository.sumAmountByTypeAndDateRange(1, startDate, endDate);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public BigDecimal calculateTotalExpense(LocalDateTime startDate, LocalDateTime endDate) {
        // SQL 直接聚合，避免全表加载到内存
        BigDecimal total = financeRepository.sumAmountByTypeAndDateRange(2, startDate, endDate);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public String testFinance() {
        return "财务模块测试成功";
    }

    @Override
    public WriteOffEntity createWriteOff(WriteOffEntity writeOffEntity) {
        if (writeOffEntity.getIsDeleted() == null) {
            writeOffEntity.setIsDeleted(0);
        }
        if (writeOffEntity.getCreatedTime() == null) {
            writeOffEntity.setCreatedTime(LocalDateTime.now());
        }
        if (writeOffEntity.getUpdatedTime() == null) {
            writeOffEntity.setUpdatedTime(LocalDateTime.now());
        }
        return writeOffRepository.save(writeOffEntity);
    }

    @Override
    public PageResult<WriteOffEntity> getWriteOffList(Integer page, Integer size, String status) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<WriteOffEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));
            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<WriteOffEntity> writeOffPage = writeOffRepository.findAll(spec, pageable);
        return PageResult.build(
                writeOffPage.getTotalElements(),
                size,
                page,
                writeOffPage.getContent()
        );
    }

    @Override
    public WriteOffEntity getWriteOffById(Long id) {
        return writeOffRepository.findById(id).orElse(null);
    }

    @Override
    public CostCalculationEntity calculateCost(CostCalculationEntity costCalculationEntity) {
        if (costCalculationEntity.getIsDeleted() == null) {
            costCalculationEntity.setIsDeleted(0);
        }
        if (costCalculationEntity.getCreatedTime() == null) {
            costCalculationEntity.setCreatedTime(LocalDateTime.now());
        }
        if (costCalculationEntity.getUpdatedTime() == null) {
            costCalculationEntity.setUpdatedTime(LocalDateTime.now());
        }
        return costCalculationRepository.save(costCalculationEntity);
    }

    @Override
    public CostCalculationEntity getCostResults(Long calculationId) {
        return costCalculationRepository.findById(calculationId).orElse(null);
    }

    @Override
    public PageResult<GeneralLedgerDTO> getGeneralLedger(Integer page, Integer size, LocalDateTime startDate, LocalDateTime endDate) {
        // 分页查询会计科目
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AccountEntity> accountPage = accountRepository.findAll(pageable);

        // 一次性 GROUP BY 汇总所有科目的借贷总额，避免 N+1 查询
        List<Object[]> sums = voucherItemRepository.sumByAccountGrouped(startDate, endDate);
        Map<Long, BigDecimal[]> sumMap = new HashMap<>();
        for (Object[] row : sums) {
            if (row == null || row.length < 3) {
                continue;
            }
            Long accountId = row[0] instanceof Number n ? n.longValue() : null;
            if (accountId == null) {
                continue;
            }
            BigDecimal debit = row[1] instanceof BigDecimal b ? b : new BigDecimal(String.valueOf(row[1]));
            BigDecimal credit = row[2] instanceof BigDecimal b ? b : new BigDecimal(String.valueOf(row[2]));
            sumMap.put(accountId, new BigDecimal[]{debit, credit});
        }

        List<GeneralLedgerDTO> ledgerList = new ArrayList<>();
        for (AccountEntity account : accountPage.getContent()) {
            GeneralLedgerDTO dto = new GeneralLedgerDTO();
            dto.setAccountCode(account.getAccountCode());
            dto.setAccountName(account.getAccountName());
            dto.setAccountType(account.getAccountType());
            dto.setStatus(account.getStatus() == 1 ? "active" : "inactive");

            BigDecimal[] sum = sumMap.getOrDefault(account.getId(), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            BigDecimal debitTotal = sum[0];
            BigDecimal creditTotal = sum[1];

            dto.setDebitAmount(debitTotal);
            dto.setCreditAmount(creditTotal);

            // 假设期初余额为0（实际项目中应从期初余额汇总表获取）
            dto.setBeginningBalance(BigDecimal.ZERO);

            // 计算期末余额：借方科目 = 期初 + 借 - 贷；贷方科目 = 期初 + 贷 - 借
            if ("debit".equalsIgnoreCase(account.getBalanceDirection())) {
                dto.setEndingBalance(dto.getBeginningBalance().add(debitTotal).subtract(creditTotal));
            } else {
                dto.setEndingBalance(dto.getBeginningBalance().add(creditTotal).subtract(debitTotal));
            }

            ledgerList.add(dto);
        }

        return PageResult.build(
                accountPage.getTotalElements(),
                size,
                page,
                ledgerList
        );
    }
}
