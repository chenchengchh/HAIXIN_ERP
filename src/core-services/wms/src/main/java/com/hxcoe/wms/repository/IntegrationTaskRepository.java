package com.hxcoe.wms.repository;

import com.hxcoe.wms.entity.IntegrationTaskEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationTaskRepository extends JpaRepository<IntegrationTaskEntity, Long> {
    Optional<IntegrationTaskEntity> findByIdempotencyKey(String idempotencyKey);

    List<IntegrationTaskEntity> findTop50ByStatusInAndNextRetryAtBeforeOrderByNextRetryAtAsc(List<String> statuses, LocalDateTime before);
}

