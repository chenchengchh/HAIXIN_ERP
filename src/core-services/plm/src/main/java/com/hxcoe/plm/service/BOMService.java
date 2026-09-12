package com.hxcoe.plm.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.plm.entity.BOMEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface BOMService {
    Result<BOMEntity> createBOM(BOMEntity bom);
    Result<BOMEntity> updateBOM(Long id, BOMEntity bom);
    Result<Void> deleteBOM(Long id);
    Result<BOMEntity> getBOMById(Long id);
    Result<List<BOMEntity>> getBOMsByProductId(Long productId, String version);
    Result<PageResult<BOMEntity>> getBOMsByPage(Pageable pageable);
    Result<Void> checkoutByProductId(Long productId);
    Result<Void> checkinByProductId(Long productId);
    Result<Void> releaseByProductId(Long productId);
    Result<Map<String, Object>> compareVersions(Long productId, String fromVersion, String toVersion);
}
