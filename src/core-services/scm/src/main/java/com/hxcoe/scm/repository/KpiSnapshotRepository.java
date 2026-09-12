package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.KpiSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KpiSnapshotRepository extends JpaRepository<KpiSnapshotEntity, Long> {
    Optional<KpiSnapshotEntity> findTopBySnapshotDate(LocalDate snapshotDate);

    List<KpiSnapshotEntity> findBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate from, LocalDate to);
}

