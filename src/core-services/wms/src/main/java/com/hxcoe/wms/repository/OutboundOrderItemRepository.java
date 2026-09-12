package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.OutboundOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboundOrderItemRepository extends JpaRepository<OutboundOrderItemEntity, Long> {
    List<OutboundOrderItemEntity> findByOutboundOrder_Id(Long outboundOrderId);
}
