package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.SalesTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 销售目标数据访问接口
 */
@Repository
public interface SalesTargetRepository extends JpaRepository<SalesTarget, Long> {

    /**
     * 根据负责人姓名分页查询销售目标
     * @param ownerName 负责人姓名（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<SalesTarget> findByOwnerNameContaining(String ownerName, Pageable pageable);
}
