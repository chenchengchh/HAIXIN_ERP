package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.Contract;
import com.hxcoe.crm.service.ContractService;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 合同管理控制器
 */
@RestController
@RequestMapping("/api/v1/crm/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    /**
     * 创建合同
     * @param contract 合同数据
     * @return 创建结果
     */
    @PostMapping
    public Result<Contract> createContract(@RequestBody Contract contract) {
        Contract createdContract = contractService.createContract(contract);
        return Result.success("成功", createdContract);
    }

    /**
     * 分页查询合同列表
     * @param page 页码，默认1
     * @param size 每页大小，默认10
     * @param status 状态筛选（可选）
     * @param customerId 客户ID筛选（可选）
     * @return 合同分页数据
     */
    @GetMapping("/list")
    public Result<PageResult<Contract>> getContractList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "id"));
        Page<Contract> contractPage = contractService.getContractList(pageable, status, customerId);
        return Result.success("成功", PageResult.build(
                contractPage.getTotalElements(),
                contractPage.getSize(),
                contractPage.getNumber() + 1,
                contractPage.getContent()
        ));
    }

    /**
     * 查询合同详情
     * @param id 合同ID
     * @return 合同详情
     */
    @GetMapping("/{id}")
    public Result<Contract> getContractDetail(@PathVariable Long id) {
        Contract contract = contractService.getContractDetail(id);
        return contract != null ? Result.success("成功", contract) : Result.fail("未找到");
    }

    /**
     * 更新合同
     * @param id 合同ID
     * @param contract 合同数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<Contract> updateContract(@PathVariable Long id, @RequestBody Contract contract) {
        Contract updatedContract = contractService.updateContract(id, contract);
        return updatedContract != null ? Result.success("成功", updatedContract) : Result.fail("未找到");
    }

    /**
     * 签订合同（状态置为 SIGNED，记录签订时间）
     * @param id 合同ID
     * @return 签订结果
     */
    @PostMapping("/{id}/sign")
    public Result<Contract> signContract(@PathVariable Long id) {
        Contract signedContract = contractService.signContract(id);
        return signedContract != null ? Result.success("成功", signedContract) : Result.fail("未找到");
    }

    /**
     * 查询即将到期合同（结束日期在未来指定天数内且状态为 SIGNED/ACTIVE）
     * @param days 未来天数，默认30天
     * @return 即将到期合同列表
     */
    @GetMapping("/expiring")
    public Result<List<Contract>> getExpiringContracts(@RequestParam(defaultValue = "30") Integer days) {
        List<Contract> contracts = contractService.getExpiringContracts(days);
        return Result.success(contracts);
    }
}
