package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.MaterialForecastEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialForecastRepository extends JpaRepository<MaterialForecastEntity, Long>, JpaSpecificationExecutor<MaterialForecastEntity> {
}

