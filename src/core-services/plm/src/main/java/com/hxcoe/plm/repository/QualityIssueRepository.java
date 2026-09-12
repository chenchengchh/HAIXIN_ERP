package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.QualityIssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QualityIssueRepository extends JpaRepository<QualityIssueEntity, Long>, JpaSpecificationExecutor<QualityIssueEntity> {
}

