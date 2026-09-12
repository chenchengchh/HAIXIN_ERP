package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.IntegrationOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IntegrationOutboxRepository extends JpaRepository<IntegrationOutboxEntity, Long> {
    Optional<IntegrationOutboxEntity> findByEventTypeAndRefNo(String eventType, String refNo);
    List<IntegrationOutboxEntity> findTop50ByStatusInAndNextRetryAtBeforeOrderByNextRetryAtAsc(List<String> status, LocalDateTime now);
}

