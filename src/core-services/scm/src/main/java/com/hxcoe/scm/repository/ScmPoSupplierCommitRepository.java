package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.ScmPoSupplierCommitEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScmPoSupplierCommitRepository extends JpaRepository<ScmPoSupplierCommitEntity, Long> {
    Optional<ScmPoSupplierCommitEntity> findByOrderNo(String orderNo);
}

