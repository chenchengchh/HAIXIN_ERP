package com.hxcoe.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ems_report_export_history")
public class EmsReportExportHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "report_name", length = 180, nullable = false)
    private String reportName;

    @Column(name = "format", length = 20, nullable = false)
    private String format;

    @Column(name = "export_time", nullable = false)
    private LocalDateTime exportTime;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "content_text", columnDefinition = "LONGTEXT")
    private String contentText;
}

