package com.hxcoe.erp.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.MaterialEntity;

import java.util.List;

public interface MaterialService {
    PageResult<MaterialEntity> getMaterialList(Integer page, Integer size, String name, String code);
    MaterialEntity createMaterial(MaterialEntity material);
    MaterialEntity updateMaterial(MaterialEntity material);
    boolean deleteMaterial(Long id);
    MaterialEntity getMaterialById(Long id);
    MaterialEntity approveMaterial(Long id);
    int batchApproveMaterials(List<Long> ids);
}
