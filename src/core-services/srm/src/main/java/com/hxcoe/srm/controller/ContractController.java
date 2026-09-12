package com.hxcoe.srm.controller;

import com.hxcoe.srm.entity.ContractEntity;
import com.hxcoe.srm.service.ContractService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srm")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @PostMapping("/contracts")
    public Result<ContractEntity> createContract(@RequestBody ContractEntity contract) {
        ContractEntity result = contractService.createContract(contract);
        return Result.success("合同创建成功", result);
    }

    @GetMapping("/contracts")
    public Result<PageResult<ContractEntity>> getContracts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ContractEntity> result = contractService.getContracts(pageable);
        PageResult<ContractEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("合同列表查询成功", pageResult);
    }

    @GetMapping("/contracts/{id}")
    public Result<ContractEntity> getContractById(@PathVariable Long id) {
        Optional<ContractEntity> result = contractService.getContractById(id);
        return result.map(contractEntity -> Result.success("合同查询成功", contractEntity)).orElseGet(() -> Result.fail("合同不存在"));
    }

    @PutMapping("/contracts/{id}")
    public Result<ContractEntity> updateContract(@PathVariable Long id, @RequestBody ContractEntity contract) {
        ContractEntity result = contractService.updateContract(id, contract);
        if (result != null) {
            return Result.success("合同更新成功", result);
        } else {
            return Result.fail("合同不存在");
        }
    }

    @DeleteMapping("/contracts/{id}")
    public Result<String> deleteContract(@PathVariable Long id) {
        contractService.deleteContract(id);
        return Result.success("合同删除成功");
    }

    @PutMapping("/contracts/{id}/activate")
    public Result<ContractEntity> activateContract(@PathVariable Long id) {
        ContractEntity result = contractService.activateContract(id);
        if (result != null) {
            return Result.success("合同激活成功", result);
        } else {
            return Result.fail("合同不存在");
        }
    }

    @PutMapping("/contracts/{id}/terminate")
    public Result<ContractEntity> terminateContract(@PathVariable Long id) {
        ContractEntity result = contractService.terminateContract(id);
        if (result != null) {
            return Result.success("合同终止成功", result);
        } else {
            return Result.fail("合同不存在");
        }
    }
}
