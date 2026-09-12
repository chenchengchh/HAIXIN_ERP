package com.hxcoe.bom.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.bom.entity.MaterialEntity;
import com.hxcoe.bom.repository.MaterialRepository;
import com.hxcoe.bom.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.annotation.Nonnull;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    @Value("${bom.master-data.material-write-enabled:false}")
    private boolean materialWriteEnabled;

    @Autowired
    public MaterialServiceImpl(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public @Nonnull Result<MaterialEntity> createMaterial(@Nonnull MaterialEntity material) {
        if (!materialWriteEnabled) {
            return Result.fail("物料主数据权威归 ERP，BOM 禁止写入");
        }
        material.setCreatedTime(LocalDateTime.now());
        MaterialEntity savedMaterial = materialRepository.save(material);
        return Result.success(savedMaterial);
    }

    @Override
    public @Nonnull Result<MaterialEntity> updateMaterial(@Nonnull Long id, @Nonnull MaterialEntity material) {
        if (!materialWriteEnabled) {
            return Result.fail("物料主数据权威归 ERP，BOM 禁止写入");
        }
        MaterialEntity existingMaterial = materialRepository.findById(id).orElse(null);
        if (existingMaterial == null) {
            return Result.error("物料不存在");
        }
        material.setId(id);
        material.setUpdatedTime(LocalDateTime.now());
        MaterialEntity updatedMaterial = materialRepository.save(material);
        return Result.success(updatedMaterial);
    }

    @Override
    public @Nonnull Result<Void> deleteMaterial(@Nonnull Long id) {
        if (!materialWriteEnabled) {
            return Result.fail("物料主数据权威归 ERP，BOM 禁止写入");
        }
        if (!materialRepository.existsById(id)) {
            return Result.error("物料不存在");
        }
        materialRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public @Nonnull Result<MaterialEntity> getMaterialById(@Nonnull Long id) {
        MaterialEntity material = materialRepository.findById(id).orElse(null);
        if (material == null) {
            return Result.error("物料不存在");
        }
        return Result.success(material);
    }

    @Override
    public @Nonnull Result<MaterialEntity> getMaterialByCode(@Nonnull String materialCode) {
        MaterialEntity material = materialRepository.findFirstByMaterialCode(materialCode).orElse(null);
        if (material == null) {
            return Result.error("物料不存在");
        }
        return Result.success(material);
    }

    @Override
    public @Nonnull Result<PageResult<MaterialEntity>> getMaterialsByPage(@Nonnull Pageable pageable) {
        Page<MaterialEntity> page = materialRepository.findAll(pageable);
        PageResult<MaterialEntity> pageResult = PageResult.build(
            page.getTotalElements(), 
            (int) page.getSize(), 
            (int) (page.getNumber() + 1), 
            page.getContent()
        );
        return Result.success(pageResult);
    }

    @Override
    public @Nonnull Result<PageResult<MaterialEntity>> getMaterials(String code, String name, @Nonnull Pageable pageable) {
        return getMaterials(code, name, null, null, pageable);
    }

    @Override
    public @Nonnull Result<PageResult<MaterialEntity>> getMaterials(String code, String name, String spec, String status, @Nonnull Pageable pageable) {
        // 动态条件查询：编码/名称/规格模糊匹配，状态精确匹配
        Specification<MaterialEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (code != null && !code.isEmpty()) {
                predicates.add(cb.like(root.get("materialCode"), "%" + code + "%"));
            }
            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(root.get("materialName"), "%" + name + "%"));
            }
            if (spec != null && !spec.isEmpty()) {
                predicates.add(cb.like(root.get("materialSpec"), "%" + spec + "%"));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<MaterialEntity> page = materialRepository.findAll(specification, pageable);
        PageResult<MaterialEntity> pageResult = PageResult.build(
            page.getTotalElements(),
            (int) page.getSize(),
            (int) (page.getNumber() + 1),
            page.getContent()
        );
        return Result.success(pageResult);
    }
}
