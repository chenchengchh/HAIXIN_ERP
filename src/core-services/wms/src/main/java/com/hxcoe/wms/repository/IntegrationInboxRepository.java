package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.IntegrationInboxEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationInboxRepository extends JpaRepository<IntegrationInboxEntity, Long> {
    Optional<IntegrationInboxEntity> findByEventKey(String eventKey);
}

