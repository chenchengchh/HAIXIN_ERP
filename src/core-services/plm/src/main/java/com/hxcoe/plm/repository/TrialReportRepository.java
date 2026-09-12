package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.TrialReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TrialReportRepository extends JpaRepository<TrialReportEntity, Long>, JpaSpecificationExecutor<TrialReportEntity> {
}

