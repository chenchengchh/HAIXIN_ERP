package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.LogisticsProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogisticsProviderRepository extends JpaRepository<LogisticsProviderEntity, Long> {
}

