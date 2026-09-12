package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.TransportEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportEventRepository extends JpaRepository<TransportEventEntity, Long> {
    List<TransportEventEntity> findByShipmentIdOrderByEventTimeDesc(Long shipmentId);
}
