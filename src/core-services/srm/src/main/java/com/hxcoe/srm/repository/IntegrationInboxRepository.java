package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.IntegrationInboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IntegrationInboxRepository extends JpaRepository<IntegrationInboxEntity, Long> {
    Optional<IntegrationInboxEntity> findByEventKey(String eventKey);
}

