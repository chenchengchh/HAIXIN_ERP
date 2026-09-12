package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.ChangeRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequestEntity, Long>, JpaSpecificationExecutor<ChangeRequestEntity> {
}

