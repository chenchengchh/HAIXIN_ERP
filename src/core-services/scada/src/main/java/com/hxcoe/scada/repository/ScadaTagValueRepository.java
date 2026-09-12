package com.hxcoe.scada.repository;

import com.hxcoe.scada.entity.ScadaTagValueEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScadaTagValueRepository extends JpaRepository<ScadaTagValueEntity, Long> {
    List<ScadaTagValueEntity> findByTagCodeOrderByTsDesc(String tagCode, Pageable pageable);

    List<ScadaTagValueEntity> findByTagCodeAndTsBetweenOrderByTsAsc(String tagCode, LocalDateTime start, LocalDateTime end);

    List<ScadaTagValueEntity> findByTsBetweenOrderByTagCodeAscTsAsc(LocalDateTime start, LocalDateTime end);
}

