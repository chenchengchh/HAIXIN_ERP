package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.ForecastVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForecastVersionRepository extends JpaRepository<ForecastVersionEntity, Long> {
    List<ForecastVersionEntity> findByPeriodOrderByVersionNoDesc(String period);

    Optional<ForecastVersionEntity> findTopByPeriodOrderByVersionNoDesc(String period);

    Optional<ForecastVersionEntity> findTopByPeriodAndStatusOrderByVersionNoDesc(String period, String status);

    long countByPeriod(String period);
}
