package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.ForecastConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForecastConfigRepository extends JpaRepository<ForecastConfigEntity, Long> {
    Optional<ForecastConfigEntity> findTopByEnabledOrderByUpdatedTimeDesc(Integer enabled);
}

