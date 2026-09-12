package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.IntegrationTaskEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IntegrationTaskRepository extends JpaRepository<IntegrationTaskEntity, Long> {
    Optional<IntegrationTaskEntity> findByIdempotencyKey(String idempotencyKey);

    List<IntegrationTaskEntity> findTop50ByStatusInOrderByCreatedTimeAsc(List<String> statuses);
}

