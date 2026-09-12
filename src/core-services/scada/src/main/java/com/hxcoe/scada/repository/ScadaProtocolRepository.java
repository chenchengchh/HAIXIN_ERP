package com.hxcoe.scada.repository;

import com.hxcoe.scada.entity.ScadaProtocolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScadaProtocolRepository extends JpaRepository<ScadaProtocolEntity, Long> {
}

