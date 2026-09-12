package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.ErpEnergyCostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 能源成本Repository（EMS→ERP 能源成本闭环）
 * 用于访问和操作 erp_energy_cost 表
 */
@Repository
public interface ErpEnergyCostRepository extends JpaRepository<ErpEnergyCostEntity, Long> {

    /**
     * 判断指定事件ID的能源成本记录是否已存在（幂等查重）
     *
     * @param eventId 事件ID（UUID）
     * @return true-已存在
     */
    boolean existsByEventId(String eventId);
}
