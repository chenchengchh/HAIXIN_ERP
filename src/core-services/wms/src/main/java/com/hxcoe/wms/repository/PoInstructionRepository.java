package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.PoInstructionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoInstructionRepository extends JpaRepository<PoInstructionEntity, Long> {
    Optional<PoInstructionEntity> findByPoNo(String poNo);
}

