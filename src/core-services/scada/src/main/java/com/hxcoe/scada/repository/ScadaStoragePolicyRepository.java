package com.hxcoe.scada.repository;

import com.hxcoe.scada.entity.ScadaStoragePolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScadaStoragePolicyRepository extends JpaRepository<ScadaStoragePolicyEntity, Long> {
}

