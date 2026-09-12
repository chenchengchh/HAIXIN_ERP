package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.EquipmentFaultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentFaultRepository extends JpaRepository<EquipmentFaultEntity, Long> {
    List<EquipmentFaultEntity> findTop20ByEquipmentIdOrderByOccurTimeDesc(String equipmentId);
}
