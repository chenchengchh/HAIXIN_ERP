package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.ApsOrderStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApsOrderStatusRepository extends JpaRepository<ApsOrderStatusEntity, Long> {
    Optional<ApsOrderStatusEntity> findByPlanId(Long planId);
}

