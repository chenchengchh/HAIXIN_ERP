package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.WorkOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrderEntity, Long> {
    Optional<WorkOrderEntity> findByWorkOrderNo(String workOrderNo);
    Optional<WorkOrderEntity> findBySourceId(String sourceId);

    /**
     * 按 ERP 生产单号查询工单（跨服务业务键，用于 B1 消费端幂等校验）。
     *
     * @param erpProductionNo ERP 生产单号
     * @return 工单实体（存在时）
     */
    Optional<WorkOrderEntity> findByErpProductionNo(String erpProductionNo);
}
