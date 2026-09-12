package com.hxcoe.scada.repository;

import com.hxcoe.scada.entity.ScadaCollectPointEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScadaCollectPointRepository extends JpaRepository<ScadaCollectPointEntity, Long> {
    Optional<ScadaCollectPointEntity> findByTagCode(String tagCode);
}

