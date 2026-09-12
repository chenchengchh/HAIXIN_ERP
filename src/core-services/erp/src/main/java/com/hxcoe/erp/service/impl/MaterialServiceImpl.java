package com.hxcoe.erp.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.MaterialEntity;
import com.hxcoe.erp.repository.MaterialRepository;
import com.hxcoe.erp.service.MaterialEventOutboxService;
import com.hxcoe.erp.service.MaterialService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialEventOutboxService materialEventOutboxService;

    @Autowired
    public MaterialServiceImpl(MaterialRepository materialRepository, MaterialEventOutboxService materialEventOutboxService) {
        this.materialRepository = materialRepository;
        this.materialEventOutboxService = materialEventOutboxService;
    }

    @Override
    public PageResult<MaterialEntity> getMaterialList(Integer page, Integer size, String name, String code) {
        if (page == null || page < 1) {
            throw new IllegalArgumentException("page 必须大于等于 1");
        }
        if (size == null || size < 1 || size > 200) {
            throw new IllegalArgumentException("size 必须在 1-200 之间");
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<MaterialEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));
            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("materialName"), "%" + name + "%"));
            }
            if (code != null && !code.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("materialCode"), code));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<MaterialEntity> materialPage = materialRepository.findAll(spec, pageable);
        return PageResult.build(materialPage.getTotalElements(), size, page, materialPage.getContent());
    }

    @Override
    @Transactional
    public MaterialEntity createMaterial(MaterialEntity material) {
        if (material == null) {
            throw new IllegalArgumentException("物料数据不能为空");
        }
        if (material.getMaterialName() == null || material.getMaterialName().isBlank()) {
            throw new IllegalArgumentException("materialName 不能为空");
        }
        if (material.getMaterialCode() == null || material.getMaterialCode().isBlank()) {
            material.setMaterialCode("MAT" + System.currentTimeMillis());
        }
        material.setMaterialCode(material.getMaterialCode().trim());
        material.setMaterialName(material.getMaterialName().trim());
        if (material.getMaterialType() != null) material.setMaterialType(material.getMaterialType().trim());
        if (material.getSpecification() != null) material.setSpecification(material.getSpecification().trim());
        if (material.getUnit() != null) material.setUnit(material.getUnit().trim());
        if (material.getDefaultSupplier() != null) material.setDefaultSupplier(material.getDefaultSupplier().trim());
        if (material.getApprovalStatus() != null) material.setApprovalStatus(material.getApprovalStatus().trim());

        Optional<MaterialEntity> existingByCode = materialRepository.findByMaterialCode(material.getMaterialCode());
        if (existingByCode.isPresent()) {
            throw new IllegalArgumentException("物料编码已存在: " + material.getMaterialCode());
        }

        material.setIsDeleted(0);
        material.setCreatedTime(LocalDateTime.now());
        material.setUpdatedTime(LocalDateTime.now());
        if (material.getStatus() == null) material.setStatus(1);
        if (material.getApprovalStatus() == null || material.getApprovalStatus().isBlank()) {
            material.setApprovalStatus("pending");
        }
        MaterialEntity saved = materialRepository.save(material);
        materialEventOutboxService.enqueueMaterialEvent(saved, "MATERIAL_CREATED");
        return saved;
    }

    @Override
    @Transactional
    public MaterialEntity updateMaterial(MaterialEntity material) {
        if (material == null || material.getId() == null) {
            throw new IllegalArgumentException("物料ID不能为空");
        }
        MaterialEntity existing = materialRepository.findById(material.getId()).orElse(null);
        if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("物料不存在");
        }
        
        if (material.getMaterialName() != null) existing.setMaterialName(material.getMaterialName());
        if (material.getMaterialType() != null) existing.setMaterialType(material.getMaterialType());
        if (material.getSpecification() != null) existing.setSpecification(material.getSpecification());
        if (material.getUnit() != null) existing.setUnit(material.getUnit());
        if (material.getSafetyStock() != null) existing.setSafetyStock(material.getSafetyStock());
        if (material.getMinStock() != null) existing.setMinStock(material.getMinStock());
        if (material.getMaxStock() != null) existing.setMaxStock(material.getMaxStock());
        if (material.getLeadTime() != null) existing.setLeadTime(material.getLeadTime());
        if (material.getDefaultSupplier() != null) existing.setDefaultSupplier(material.getDefaultSupplier());
        if (material.getUnitPrice() != null) existing.setUnitPrice(material.getUnitPrice());
        if (material.getApprovalStatus() != null) existing.setApprovalStatus(material.getApprovalStatus());
        if (material.getStatus() != null) existing.setStatus(material.getStatus());
        if (material.getRemark() != null) existing.setRemark(material.getRemark());
        
        existing.setUpdatedTime(LocalDateTime.now());
        MaterialEntity saved = materialRepository.save(existing);
        materialEventOutboxService.enqueueMaterialEvent(saved, "MATERIAL_UPDATED");
        return saved;
    }

    @Override
    @Transactional
    public boolean deleteMaterial(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("物料ID不能为空");
        }
        MaterialEntity existing = materialRepository.findById(id).orElse(null);
        if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("物料不存在");
        }
        existing.setIsDeleted(1);
        existing.setUpdatedTime(LocalDateTime.now());
        MaterialEntity saved = materialRepository.save(existing);
        materialEventOutboxService.enqueueMaterialEvent(saved, "MATERIAL_DELETED");
        return true;
    }

    @Override
    public MaterialEntity getMaterialById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("物料ID不能为空");
        }
        MaterialEntity existing = materialRepository.findById(id).orElse(null);
        if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("物料不存在");
        }
        return existing;
    }

    @Override
    @Transactional
    public MaterialEntity approveMaterial(Long id) {
        if (id == null) throw new IllegalArgumentException("物料ID不能为空");
        MaterialEntity existing = materialRepository.findById(id).orElse(null);
        if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("物料不存在");
        }
        existing.setApprovalStatus("approved");
        existing.setUpdatedTime(LocalDateTime.now());
        MaterialEntity saved = materialRepository.save(existing);
        materialEventOutboxService.enqueueMaterialEvent(saved, "MATERIAL_APPROVED");
        return saved;
    }

    @Override
    @Transactional
    public int batchApproveMaterials(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        int updated = 0;
        for (Long id : ids) {
            if (id == null) continue;
            MaterialEntity existing = materialRepository.findById(id).orElse(null);
            if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) continue;
            existing.setApprovalStatus("approved");
            existing.setUpdatedTime(LocalDateTime.now());
            MaterialEntity saved = materialRepository.save(existing);
            materialEventOutboxService.enqueueMaterialEvent(saved, "MATERIAL_APPROVED");
            updated++;
        }
        return updated;
    }
}
