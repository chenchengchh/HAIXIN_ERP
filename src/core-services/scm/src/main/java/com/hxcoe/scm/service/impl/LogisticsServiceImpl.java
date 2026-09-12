package com.hxcoe.scm.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.ShipmentEntity;
import com.hxcoe.scm.entity.TransportEventEntity;
import com.hxcoe.scm.repository.ShipmentRepository;
import com.hxcoe.scm.repository.TransportEventRepository;
import com.hxcoe.scm.service.LogisticsService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogisticsServiceImpl implements LogisticsService {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private TransportEventRepository transportEventRepository;

    @Transactional
    @Override
    public ShipmentEntity createShipment(ShipmentEntity shipment) {
        return shipmentRepository.save(shipment);
    }

    @Transactional
    @Override
    public ShipmentEntity updateShipment(Long id, ShipmentEntity shipment) {
        ShipmentEntity existing = shipmentRepository.findById(id).orElse(null);
        if (existing == null) return null;
        if (shipment.getRelatedType() != null) existing.setRelatedType(shipment.getRelatedType());
        if (shipment.getRelatedId() != null) existing.setRelatedId(shipment.getRelatedId());
        if (shipment.getRelatedNo() != null) existing.setRelatedNo(shipment.getRelatedNo());
        if (shipment.getSupplierId() != null) existing.setSupplierId(shipment.getSupplierId());
        if (shipment.getSupplierName() != null) existing.setSupplierName(shipment.getSupplierName());
        if (shipment.getOrigin() != null) existing.setOrigin(shipment.getOrigin());
        if (shipment.getDestination() != null) existing.setDestination(shipment.getDestination());
        if (shipment.getTransportMode() != null) existing.setTransportMode(shipment.getTransportMode());
        if (shipment.getEta() != null) existing.setEta(shipment.getEta());
        if (shipment.getStatus() != null) existing.setStatus(shipment.getStatus());
        return shipmentRepository.save(existing);
    }

    @Override
    public ShipmentEntity getShipment(Long id) {
        return shipmentRepository.findById(id).orElse(null);
    }

    @Override
    public PageResult<ShipmentEntity> getShipments(String keyword, String status, Pageable pageable) {
        Specification<ShipmentEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isBlank()) {
                Predicate p1 = cb.like(root.get("shipmentNo"), "%" + keyword + "%");
                Predicate p2 = cb.like(root.get("relatedNo"), "%" + keyword + "%");
                Predicate p3 = cb.like(root.get("supplierName"), "%" + keyword + "%");
                predicates.add(cb.or(p1, p2, p3));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ShipmentEntity> page = shipmentRepository.findAll(spec, pageable);
        return PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
    }

    @Transactional
    @Override
    public TransportEventEntity addEvent(Long shipmentId, TransportEventEntity event) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) return null;
        event.setShipmentId(shipmentId);
        TransportEventEntity saved = transportEventRepository.save(event);
        if ("DELAYED".equals(event.getEventType())) {
            shipment.setStatus("DELAYED");
            shipmentRepository.save(shipment);
        } else if ("IN_TRANSIT".equals(event.getEventType())) {
            shipment.setStatus("IN_TRANSIT");
            shipmentRepository.save(shipment);
        } else if ("ARRIVED".equals(event.getEventType())) {
            shipment.setStatus("ARRIVED");
            shipment.setArrivedTime(LocalDateTime.now());
            shipmentRepository.save(shipment);
        } else if ("SIGNED".equals(event.getEventType())) {
            shipment.setStatus("SIGNED");
            shipment.setArrivedTime(LocalDateTime.now());
            shipmentRepository.save(shipment);
        }
        return saved;
    }

    @Override
    public List<TransportEventEntity> getEvents(Long shipmentId) {
        return transportEventRepository.findByShipmentIdOrderByEventTimeDesc(shipmentId);
    }

    @Transactional
    @Override
    public ShipmentEntity markArrived(Long shipmentId) {
        ShipmentEntity shipment = shipmentRepository.findById(shipmentId).orElse(null);
        if (shipment == null) return null;
        shipment.setStatus("ARRIVED");
        shipment.setArrivedTime(LocalDateTime.now());
        return shipmentRepository.save(shipment);
    }
}
