package com.hxcoe.wms.service;

import com.hxcoe.wms.entity.OutboundOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface OutboundOrderService {
    OutboundOrderEntity createOutboundOrder(OutboundOrderEntity order);
    Page<OutboundOrderEntity> getOutboundOrders(Pageable pageable, String status, String type, String orderNo, String customerName, Long waveId);
    Optional<OutboundOrderEntity> getOutboundOrderById(Long id);
    OutboundOrderEntity updateOutboundOrder(Long id, OutboundOrderEntity order);
    OutboundOrderEntity shipOutboundOrder(Long id);
    OutboundOrderEntity approveOutboundOrder(Long id);
    OutboundOrderEntity cancelOutboundOrder(Long id);
}
