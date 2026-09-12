package com.hxcoe.ems.repository;

import com.hxcoe.ems.entity.EmsCustomReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmsCustomReportRepository extends JpaRepository<EmsCustomReportEntity, Long> {
    Page<EmsCustomReportEntity> findByCreator(String creator, Pageable pageable);
}

