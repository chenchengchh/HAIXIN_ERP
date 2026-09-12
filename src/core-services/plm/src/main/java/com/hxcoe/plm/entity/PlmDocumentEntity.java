package com.hxcoe.plm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "plm_document")
@Data
public class PlmDocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doc_code", length = 64, unique = true)
    private String docCode;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "doc_type", length = 64)
    private String docType;

    @Column(name = "category", length = 64)
    private String category;

    @Column(name = "version", length = 32)
    private String version;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "author", length = 64)
    private String author;

    @Column(name = "file_name", length = 256)
    private String fileName;

    @Column(name = "file_path", length = 512)
    private String filePath;

    @Column(name = "file_size", length = 32)
    private String fileSize;

    @Column(name = "file_format", length = 32)
    private String fileFormat;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}

