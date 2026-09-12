package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.PoReconciliationDiffEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PoReconciliationDiffRepository extends JpaRepository<PoReconciliationDiffEntity, Long> {
    Optional<PoReconciliationDiffEntity> findByOrderNo(String orderNo);
    Page<PoReconciliationDiffEntity> findByStatusOrderByUpdatedTimeDesc(String status, Pageable pageable);
}

