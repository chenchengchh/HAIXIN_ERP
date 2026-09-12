package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.IntegrationTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationTaskRepository extends JpaRepository<IntegrationTaskEntity, Long> {
    Optional<IntegrationTaskEntity> findByResultIdAndActionType(Long resultId, String actionType);
    Optional<IntegrationTaskEntity> findByIdempotencyKey(String idempotencyKey);
    List<IntegrationTaskEntity> findTop50ByStatusInOrderByCreatedTimeAsc(List<String> status);
    List<IntegrationTaskEntity> findTop50ByStatusInAndActionTypeOrderByCreatedTimeAsc(List<String> status, String actionType);
}
