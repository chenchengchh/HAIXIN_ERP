package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.DemandForecastEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForecastRepository extends JpaRepository<DemandForecastEntity, Long>, JpaSpecificationExecutor<DemandForecastEntity> {
    List<DemandForecastEntity> findByPeriod(String period);

    List<DemandForecastEntity> findByPeriodAndVersionId(String period, Long versionId);

    List<DemandForecastEntity> findByVersionId(Long versionId);

    void deleteByVersionId(Long versionId);
}
