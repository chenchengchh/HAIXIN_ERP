package com.hxcoe.bom.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.bom.entity.MaterialEntity;
import org.springframework.data.domain.Pageable;

public interface MaterialService {
    Result<MaterialEntity> createMaterial(MaterialEntity material);
    Result<MaterialEntity> updateMaterial(Long id, MaterialEntity material);
    Result<Void> deleteMaterial(Long id);
    Result<MaterialEntity> getMaterialById(Long id);
    Result<MaterialEntity> getMaterialByCode(String materialCode);
    Result<PageResult<MaterialEntity>> getMaterialsByPage(Pageable pageable);
    Result<PageResult<MaterialEntity>> getMaterials(String code, String name, Pageable pageable);
    Result<PageResult<MaterialEntity>> getMaterials(String code, String name, String spec, String status, Pageable pageable);
}
