package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.ContractEntity;
import com.hxcoe.srm.repository.ContractRepository;
import com.hxcoe.srm.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Transactional
    @Override
    public ContractEntity createContract(ContractEntity contract) {
        contract.setStatus("DRAFT");
        return contractRepository.save(contract);
    }

    @Override
    public Page<ContractEntity> getContracts(Pageable pageable) {
        return contractRepository.findAll(pageable);
    }

    @Override
    public Optional<ContractEntity> getContractById(Long id) {
        return contractRepository.findById(id);
    }

    @Transactional
    @Override
    public ContractEntity updateContract(Long id, ContractEntity contract) {
        return contractRepository.findById(id).map(existing -> {
            existing.setTitle(contract.getTitle());
            existing.setTotalAmount(contract.getTotalAmount());
            existing.setStartDate(contract.getStartDate());
            existing.setEndDate(contract.getEndDate());
            // Update other fields as needed
            return contractRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteContract(Long id) {
        contractRepository.deleteById(id);
    }

    @Override
    public ContractEntity activateContract(Long id) {
        return contractRepository.findById(id).map(contract -> {
            contract.setStatus("ACTIVE");
            return contractRepository.save(contract);
        }).orElse(null);
    }

    @Override
    public ContractEntity terminateContract(Long id) {
        return contractRepository.findById(id).map(contract -> {
            contract.setStatus("TERMINATED");
            return contractRepository.save(contract);
        }).orElse(null);
    }
}
