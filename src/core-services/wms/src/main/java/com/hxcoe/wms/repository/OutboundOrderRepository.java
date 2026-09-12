package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.OutboundOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutboundOrderRepository extends JpaRepository<OutboundOrderEntity, Long>, JpaSpecificationExecutor<OutboundOrderEntity> {
    List<OutboundOrderEntity> findByStatus(String status);
    List<OutboundOrderEntity> findByWaveId(Long waveId);
    java.util.Optional<OutboundOrderEntity> findByOrderNo(String orderNo);
}
