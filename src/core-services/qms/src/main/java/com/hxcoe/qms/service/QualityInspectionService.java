package com.hxcoe.qms.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.qms.entity.QualityInspectionEntity;
import org.springframework.data.domain.Pageable;

public interface QualityInspectionService {
    Result<QualityInspectionEntity> createInspection(QualityInspectionEntity inspection);
    Result<QualityInspectionEntity> updateInspection(Long id, QualityInspectionEntity inspection);
    Result<Void> deleteInspection(Long id);
    Result<QualityInspectionEntity> getInspectionById(Long id);
    Result<PageResult<QualityInspectionEntity>> getInspectionsByPage(Pageable pageable);
}