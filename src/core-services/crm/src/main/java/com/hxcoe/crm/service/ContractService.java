package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 合同服务接口
 */
public interface ContractService {

    /**
     * 创建合同
     * @param contract 合同数据
     * @return 创建后的合同
     */
    Contract createContract(Contract contract);

    /**
     * 查询合同详情
     * @param id 合同ID
     * @return 合同详情
     */
    Contract getContractDetail(Long id);

    /**
     * 分页查询合同列表
     * @param pageable 分页参数
     * @param status 状态筛选（可选）
     * @param customerId 客户ID筛选（可选）
     * @return 合同分页数据
     */
    Page<Contract> getContractList(Pageable pageable, String status, Long customerId);

    /**
     * 更新合同
     * @param id 合同ID
     * @param contract 合同数据
     * @return 更新后的合同
     */
    Contract updateContract(Long id, Contract contract);

    /**
     * 签订合同（状态置为 SIGNED 并记录签订时间）
     * @param id 合同ID
     * @return 签订后的合同
     */
    Contract signContract(Long id);

    /**
     * 查询即将到期合同（结束日期在未来指定天数内且状态为 SIGNED/ACTIVE）
     * @param days 未来天数
     * @return 即将到期合同列表
     */
    List<Contract> getExpiringContracts(Integer days);
}
