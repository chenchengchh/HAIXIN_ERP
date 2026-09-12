package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.ProcessRouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProcessRouteRepository extends JpaRepository<ProcessRouteEntity, Long>, JpaSpecificationExecutor<ProcessRouteEntity> {
}

