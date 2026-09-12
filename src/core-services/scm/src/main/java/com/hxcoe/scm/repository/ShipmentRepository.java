package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.ShipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long>, JpaSpecificationExecutor<ShipmentEntity> {
    Optional<ShipmentEntity> findByShipmentNo(String shipmentNo);
}
