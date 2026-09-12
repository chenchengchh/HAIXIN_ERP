package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.Contract;
import com.hxcoe.crm.repository.ContractRepository;
import com.hxcoe.crm.service.ContractService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 合同服务实现类
 */
@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    /**
     * 创建合同，自动生成合同编号（HT + 时间戳），默认状态为 DRAFT
     * @param contract 合同数据
     * @return 创建后的合同
     */
    @Override
    public Contract createContract(Contract contract) {
        if (contract.getContractNo() == null || contract.getContractNo().isEmpty()) {
            contract.setContractNo("HT" + System.currentTimeMillis());
        }
        if (contract.getStatus() == null || contract.getStatus().isEmpty()) {
            contract.setStatus("DRAFT");
        }
        return contractRepository.save(contract);
    }

    /**
     * 查询合同详情
     * @param id 合同ID
     * @return 合同详情
     */
    @Override
    public Contract getContractDetail(Long id) {
        return contractRepository.findById(id).orElse(null);
    }

    /**
     * 更新合同信息
     * @param id 合同ID
     * @param contract 合同数据
     * @return 更新后的合同，合同不存在时返回 null
     */
    @Override
    public Contract updateContract(Long id, Contract contract) {
        Contract existingContract = contractRepository.findById(id).orElse(null);
        if (existingContract != null) {
            // 更新合同信息
            existingContract.setContractName(contract.getContractName());
            existingContract.setCustomerId(contract.getCustomerId());
            existingContract.setCustomerName(contract.getCustomerName());
            existingContract.setAmount(contract.getAmount());
            existingContract.setStatus(contract.getStatus());
            existingContract.setStartDate(contract.getStartDate());
            existingContract.setEndDate(contract.getEndDate());
            existingContract.setSignDate(contract.getSignDate());
            existingContract.setOwnerName(contract.getOwnerName());
            existingContract.setRemark(contract.getRemark());

            return contractRepository.save(existingContract);
        }
        return null;
    }

    /**
     * 签订合同，将状态置为 SIGNED 并记录签订时间
     * @param id 合同ID
     * @return 签订后的合同，合同不存在时返回 null
     */
    @Override
    public Contract signContract(Long id) {
        Contract existingContract = contractRepository.findById(id).orElse(null);
        if (existingContract != null) {
            existingContract.setStatus("SIGNED");
            existingContract.setSignedAt(LocalDateTime.now());
            if (existingContract.getSignDate() == null) {
                existingContract.setSignDate(LocalDate.now());
            }
            return contractRepository.save(existingContract);
        }
        return null;
    }

    /**
     * 分页查询合同列表，支持状态和客户ID筛选
     * @param pageable 分页参数
     * @param status 状态筛选（可选）
     * @param customerId 客户ID筛选（可选）
     * @return 合同分页数据
     */
    @Override
    public Page<Contract> getContractList(Pageable pageable, String status, Long customerId) {
        Specification<Contract> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customerId"), customerId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return contractRepository.findAll(spec, pageable);
    }

    /**
     * 查询即将到期合同（结束日期在未来指定天数内且状态为 SIGNED/ACTIVE）
     * @param days 未来天数
     * @return 即将到期合同列表
     */
    @Override
    public List<Contract> getExpiringContracts(Integer days) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        return contractRepository.findByEndDateBetweenAndStatusIn(today, deadline, Arrays.asList("SIGNED", "ACTIVE"));
    }
}
