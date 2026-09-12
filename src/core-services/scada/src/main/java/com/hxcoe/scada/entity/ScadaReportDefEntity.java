package com.hxcoe.scada.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "scada_report_def")
@Data
public class ScadaReportDefEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_name", length = 100)
    private String reportName;

    @Column(name = "template_path", length = 255)
    private String templatePath;

    @Column(name = "cron_expression", length = 50)
    private String cronExpression;

    @Column(name = "export_format", length = 20)
    private String exportFormat;

    @Column(name = "associated_tags", columnDefinition = "TEXT")
    private String associatedTags;
}

