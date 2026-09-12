package com.hxcoe.plm.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.plm.entity.PlmDocumentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface PlmDocumentService {
    Result<PageResult<PlmDocumentEntity>> page(Pageable pageable, String keyword, String docType);

    Result<PlmDocumentEntity> create(PlmDocumentEntity entity);

    Result<PlmDocumentEntity> update(Long id, PlmDocumentEntity entity);

    Result<Void> delete(Long id);

    Result<PlmDocumentEntity> upload(MultipartFile file, PlmDocumentEntity entity);

    Optional<PlmDocumentEntity> getById(Long id);
}

