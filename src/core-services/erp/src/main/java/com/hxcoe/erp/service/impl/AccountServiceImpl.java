package com.hxcoe.erp.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.AccountEntity;
import com.hxcoe.erp.repository.AccountRepository;
import com.hxcoe.erp.service.AccountService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 会计科目服务实现类
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public PageResult<AccountEntity> getAccountList(Integer page, Integer size, String accountName, String accountCode, String accountType) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Specification<AccountEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 逻辑删除过滤
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));

            if (accountName != null && !accountName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("accountName"), "%" + accountName + "%"));
            }

            if (accountCode != null && !accountCode.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("accountCode"), accountCode));
            }

            if (accountType != null && !accountType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("accountType"), accountType));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<AccountEntity> accountPage = accountRepository.findAll(spec, pageable);

        return PageResult.build(
                accountPage.getTotalElements(),
                size,
                page,
                accountPage.getContent()
        );
    }

    @Override
    public AccountEntity createAccount(AccountEntity accountEntity) {
        // 设置默认值
        accountEntity.setIsDeleted(0);
        if (accountEntity.getCreatedTime() == null) {
            accountEntity.setCreatedTime(LocalDateTime.now());
        }
        if (accountEntity.getUpdatedTime() == null) {
            accountEntity.setUpdatedTime(LocalDateTime.now());
        }
        if (accountEntity.getStatus() == null) {
            accountEntity.setStatus(1); // 默认启用
        }
        return accountRepository.save(accountEntity);
    }

    @Override

    public AccountEntity updateAccount(AccountEntity accountEntity) {
        if (accountEntity.getId() == null) {
            return null;
        }
        AccountEntity existing = accountRepository.findById(accountEntity.getId()).orElse(null);
        if (existing == null) {
            return null;
        }
        
        // 更新非空字段
        if (accountEntity.getAccountName() != null) existing.setAccountName(accountEntity.getAccountName());
        if (accountEntity.getAccountType() != null) existing.setAccountType(accountEntity.getAccountType());
        if (accountEntity.getBalanceDirection() != null) existing.setBalanceDirection(accountEntity.getBalanceDirection());
        if (accountEntity.getLevel() != null) existing.setLevel(accountEntity.getLevel());
        if (accountEntity.getIsLeaf() != null) existing.setIsLeaf(accountEntity.getIsLeaf());
        if (accountEntity.getStatus() != null) existing.setStatus(accountEntity.getStatus());
        if (accountEntity.getRemark() != null) existing.setRemark(accountEntity.getRemark());
        
        existing.setUpdatedTime(LocalDateTime.now());
        return accountRepository.save(existing);
    }

    @Override
    public boolean deleteAccount(Long id) {
        if (id == null) return false;
        AccountEntity account = accountRepository.findById(id).orElse(null);
        if (account != null) {
            account.setIsDeleted(1);
            account.setUpdatedTime(LocalDateTime.now());
            accountRepository.save(account);
            return true;
        }
        return false;
    }

    @Override
    public AccountEntity getAccountById(Long id) {
        if (id == null) return null;
        return accountRepository.findById(id).orElse(null);
    }

    @Override
    public AccountEntity getAccountByCode(String accountCode) {
        return accountRepository.findByAccountCode(accountCode).orElse(null);
    }
}
