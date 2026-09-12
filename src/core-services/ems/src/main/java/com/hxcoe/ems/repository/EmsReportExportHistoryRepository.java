package com.hxcoe.ems.repository;

import com.hxcoe.ems.entity.EmsReportExportHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmsReportExportHistoryRepository extends JpaRepository<EmsReportExportHistoryEntity, Long> {
    List<EmsReportExportHistoryEntity> findByReportNameContaining(String reportName);

    List<EmsReportExportHistoryEntity> findByStatus(String status);
}

