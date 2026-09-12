package com.hxcoe.srm.service;

import com.hxcoe.srm.entity.ContractEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ContractService {
    ContractEntity createContract(ContractEntity contract);
    Page<ContractEntity> getContracts(Pageable pageable);
    Optional<ContractEntity> getContractById(Long id);
    ContractEntity updateContract(Long id, ContractEntity contract);
    void deleteContract(Long id);
    ContractEntity activateContract(Long id);
    ContractEntity terminateContract(Long id);
}
