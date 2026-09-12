package com.hxcoe.scm.service;

import com.hxcoe.scm.client.dto.wms.OutboundShippedRequest;
import com.hxcoe.scm.entity.ShipmentEntity;
import com.hxcoe.scm.entity.TransportEventEntity;
import com.hxcoe.scm.repository.ShipmentRepository;
import com.hxcoe.scm.repository.TransportEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ScmOutboundShipmentProjectionService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private TransportEventRepository transportEventRepository;

    @Transactional
    public void projectWmsOutboundShipped(OutboundShippedRequest req) {
        if (req == null || req.getOutboundOrder() == null) {
            return;
        }
        String orderNo = trim(req.getOutboundOrder().getOrderNo());
        if (orderNo == null) {
            return;
        }

        ShipmentEntity shipment = shipmentRepository.findByShipmentNo(orderNo).orElseGet(ShipmentEntity::new);
        if (shipment.getId() == null) {
            shipment.setShipmentNo(orderNo);
        }
        shipment.setRelatedType(defaultValue(trim(req.getOutboundOrder().getOrderType()), "WMS_OUTBOUND"));
        shipment.setRelatedNo(defaultValue(trim(req.getOutboundOrder().getSourceNo()), orderNo));
        shipment.setSupplierName(trim(req.getOutboundOrder().getCustomerName()));
        shipment.setOrigin("WMS");
        shipment.setDestination(trim(req.getOutboundOrder().getAddress()));
        shipment.setTransportMode(resolveTransportMode(req.getOutboundOrder().getOrderType()));
        shipment.setStatus("IN_TRANSIT");
        ShipmentEntity saved = shipmentRepository.save(shipment);

        TransportEventEntity event = new TransportEventEntity();
        event.setShipmentId(saved.getId());
        event.setEventType("SHIPPED");
        event.setEventTime(req.getOutboundOrder().getShippedTime() == null ? LocalDateTime.now() : req.getOutboundOrder().getShippedTime());
        event.setLocation("WMS");
        event.setDescription(buildDescription(req));
        transportEventRepository.save(event);
    }

    private String buildDescription(OutboundShippedRequest req) {
        StringBuilder sb = new StringBuilder("WMS出库发货完成");
        String orderType = trim(req.getOutboundOrder().getOrderType());
        String sourceNo = trim(req.getOutboundOrder().getSourceNo());
        if (orderType != null) {
            sb.append(" orderType=").append(orderType);
        }
        if (sourceNo != null) {
            sb.append(" sourceNo=").append(sourceNo);
        }
        if (req.getEventId() != null && !req.getEventId().isBlank()) {
            sb.append(" eventId=").append(req.getEventId());
        }
        return sb.toString();
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultValue(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private String resolveTransportMode(String orderType) {
        String normalized = trim(orderType);
        if (normalized == null) {
            return "WMS_OUTBOUND";
        }
        return switch (normalized.toUpperCase()) {
            case "SALES" -> "SALES_OUTBOUND";
            case "TRANSFER" -> "TRANSFER_OUTBOUND";
            case "PURCHASE_RETURN" -> "PURCHASE_RETURN_OUTBOUND";
            default -> "WMS_OUTBOUND";
        };
    }
}
