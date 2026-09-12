package com.hxcoe.ems.repository;

import com.hxcoe.ems.entity.EmsStandardReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmsStandardReportRepository extends JpaRepository<EmsStandardReportEntity, Long> {
    List<EmsStandardReportEntity> findByStatus(String status);
}

