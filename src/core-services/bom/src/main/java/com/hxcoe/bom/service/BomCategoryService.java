package com.hxcoe.bom.service;

import com.hxcoe.bom.entity.BomCategoryEntity;
import com.hxcoe.common.result.Result;

import java.util.List;

public interface BomCategoryService {
    Result<BomCategoryEntity> createCategory(BomCategoryEntity category);

    Result<BomCategoryEntity> updateCategory(Long id, BomCategoryEntity category);

    Result<Void> deleteCategory(Long id);

    Result<BomCategoryEntity> getCategoryById(Long id);

    Result<List<BomCategoryEntity>> getCategoryTree();
}

