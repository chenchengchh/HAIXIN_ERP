package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.FinanceTaskEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinanceTaskRepository extends JpaRepository<FinanceTaskEntity, Long> {
    Optional<FinanceTaskEntity> findByIdempotencyKey(String idempotencyKey);

    List<FinanceTaskEntity> findTop50ByStatusInOrderByCreatedTimeAsc(List<String> status);

    @Modifying
    @Query("update FinanceTaskEntity t set t.status = :toStatus where t.id = :id and t.status in :fromStatuses")
    int updateStatusIfIn(@Param("id") Long id, @Param("fromStatuses") List<String> fromStatuses, @Param("toStatus") String toStatus);
}
