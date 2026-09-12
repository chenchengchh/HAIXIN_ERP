package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.ReconciliationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReconciliationRepository extends JpaRepository<ReconciliationEntity, Long>, JpaSpecificationExecutor<ReconciliationEntity> {
}

