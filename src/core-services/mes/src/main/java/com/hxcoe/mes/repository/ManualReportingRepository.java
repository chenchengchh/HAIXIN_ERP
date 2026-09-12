package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.ManualReportingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManualReportingRepository extends JpaRepository<ManualReportingEntity, Long> {
}
