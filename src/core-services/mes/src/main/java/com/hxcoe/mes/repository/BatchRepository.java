package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.BatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<BatchEntity, Long> {
    Optional<BatchEntity> findByBatchNo(String batchNo);
}
