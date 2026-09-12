package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 合同数据访问接口
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {

    /**
     * 查询结束日期在指定区间内且状态在指定状态列表中的合同（用于即将到期合同查询）
     * @param startDate 区间开始日期
     * @param endDate 区间结束日期
     * @param statuses 状态列表
     * @return 合同列表
     */
    List<Contract> findByEndDateBetweenAndStatusIn(LocalDate startDate, LocalDate endDate, List<String> statuses);
}
