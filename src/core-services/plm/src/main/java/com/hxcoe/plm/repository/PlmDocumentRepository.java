package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.PlmDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlmDocumentRepository extends JpaRepository<PlmDocumentEntity, Long>, JpaSpecificationExecutor<PlmDocumentEntity> {
}

