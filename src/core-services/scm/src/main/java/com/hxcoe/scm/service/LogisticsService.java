package com.hxcoe.scm.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.ShipmentEntity;
import com.hxcoe.scm.entity.TransportEventEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LogisticsService {
    ShipmentEntity createShipment(ShipmentEntity shipment);

    ShipmentEntity updateShipment(Long id, ShipmentEntity shipment);

    ShipmentEntity getShipment(Long id);

    PageResult<ShipmentEntity> getShipments(String keyword, String status, Pageable pageable);

    TransportEventEntity addEvent(Long shipmentId, TransportEventEntity event);

    List<TransportEventEntity> getEvents(Long shipmentId);

    ShipmentEntity markArrived(Long shipmentId);
}
