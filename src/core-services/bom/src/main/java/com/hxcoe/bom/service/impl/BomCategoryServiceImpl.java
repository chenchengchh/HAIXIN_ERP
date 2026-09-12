package com.hxcoe.bom.service.impl;

import com.hxcoe.bom.entity.BomCategoryEntity;
import com.hxcoe.bom.repository.BomCategoryRepository;
import com.hxcoe.bom.repository.MaterialRepository;
import com.hxcoe.bom.service.BomCategoryService;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BomCategoryServiceImpl implements BomCategoryService {

    @Autowired
    private BomCategoryRepository bomCategoryRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Override
    public Result<BomCategoryEntity> createCategory(BomCategoryEntity category) {
        if (category == null) {
            return Result.error("参数不能为空");
        }
        String code = category.getCode() == null ? "" : category.getCode().trim();
        String name = category.getName() == null ? "" : category.getName().trim();
        if (code.isEmpty() || name.isEmpty()) {
            return Result.error("分类编码和名称不能为空");
        }
        if (bomCategoryRepository.existsByCode(code)) {
            return Result.error("分类编码已存在");
        }

        BomCategoryEntity toSave = new BomCategoryEntity();
        toSave.setCode(code);
        toSave.setName(name);
        Long parentId = category.getParentId();
        if (parentId != null && parentId <= 0) parentId = null;
        toSave.setParentId(parentId);
        toSave.setDescription(category.getDescription());
        toSave.setStatus(category.getStatus() == null ? 1 : category.getStatus());
        toSave.setRemark(category.getRemark());
        toSave.setCreatedBy(category.getCreatedBy());
        toSave.setCreatedTime(LocalDateTime.now());

        return Result.success(bomCategoryRepository.save(toSave));
    }

    @Override
    public Result<BomCategoryEntity> updateCategory(Long id, BomCategoryEntity category) {
        if (id == null) {
            return Result.error("参数不能为空");
        }
        BomCategoryEntity existing = bomCategoryRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("分类不存在");
        }
        if (category == null) {
            return Result.error("参数不能为空");
        }

        String code = category.getCode() == null ? "" : category.getCode().trim();
        String name = category.getName() == null ? "" : category.getName().trim();
        if (code.isEmpty() || name.isEmpty()) {
            return Result.error("分类编码和名称不能为空");
        }

        if (!code.equals(existing.getCode()) && bomCategoryRepository.existsByCode(code)) {
            return Result.error("分类编码已存在");
        }

        Long parentId = category.getParentId();
        if (parentId != null && parentId <= 0) parentId = null;
        if (parentId != null && parentId.equals(id)) {
            return Result.error("父分类不能是自己");
        }

        existing.setCode(code);
        existing.setName(name);
        existing.setParentId(parentId);
        existing.setDescription(category.getDescription());
        existing.setStatus(category.getStatus() == null ? existing.getStatus() : category.getStatus());
        existing.setRemark(category.getRemark());
        existing.setUpdatedBy(category.getUpdatedBy());
        existing.setUpdatedTime(LocalDateTime.now());

        return Result.success(bomCategoryRepository.save(existing));
    }

    @Override
    public Result<Void> deleteCategory(Long id) {
        if (id == null) {
            return Result.error("参数不能为空");
        }
        if (!bomCategoryRepository.existsById(id)) {
            return Result.error("分类不存在");
        }
        List<BomCategoryEntity> children = bomCategoryRepository.findByParentId(id);
        if (children != null && !children.isEmpty()) {
            return Result.error("存在子分类，无法删除");
        }
        long refCount = materialRepository.countByCategoryId(id);
        if (refCount > 0) {
            return Result.error("存在物料引用该分类，无法删除");
        }
        bomCategoryRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<BomCategoryEntity> getCategoryById(Long id) {
        BomCategoryEntity category = bomCategoryRepository.findById(id).orElse(null);
        if (category == null) {
            return Result.error("分类不存在");
        }
        return Result.success(category);
    }

    @Override
    public Result<List<BomCategoryEntity>> getCategoryTree() {
        List<BomCategoryEntity> all = bomCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        Map<Long, BomCategoryEntity> byId = new HashMap<>();
        List<BomCategoryEntity> roots = new ArrayList<>();

        for (BomCategoryEntity c : all) {
            c.setChildren(new ArrayList<>());
            byId.put(c.getId(), c);
        }

        for (BomCategoryEntity c : all) {
            Long pid = c.getParentId();
            if (pid == null || pid <= 0) {
                roots.add(c);
                continue;
            }
            BomCategoryEntity parent = byId.get(pid);
            if (parent == null) {
                roots.add(c);
                continue;
            }
            parent.getChildren().add(c);
        }

        return Result.success(roots);
    }
}

