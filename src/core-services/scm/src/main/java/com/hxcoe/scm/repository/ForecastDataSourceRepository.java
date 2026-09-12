package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.ForecastDataSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastDataSourceRepository extends JpaRepository<ForecastDataSourceEntity, Long> {
}

