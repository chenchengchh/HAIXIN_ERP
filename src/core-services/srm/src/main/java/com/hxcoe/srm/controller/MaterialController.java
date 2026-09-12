package com.hxcoe.srm.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.srm.entity.MaterialCategoryEntity;
import com.hxcoe.srm.entity.MaterialEntity;
import com.hxcoe.srm.repository.MaterialCategoryRepository;
import com.hxcoe.srm.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srm")
public class MaterialController {

    @Autowired
    private MaterialCategoryRepository materialCategoryRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Value("${srm.master-data.material-write-enabled:false}")
    private boolean materialWriteEnabled;

    @GetMapping("/material-categories")
    public Result<List<MaterialCategoryEntity>> getMaterialCategories() {
        return Result.success("物料分类查询成功", materialCategoryRepository.findAll());
    }

    @PostMapping("/material-categories")
    public Result<MaterialCategoryEntity> createMaterialCategory(@RequestBody MaterialCategoryEntity category) {
        MaterialCategoryEntity saved = materialCategoryRepository.save(category);
        return Result.success("物料分类创建成功", saved);
    }

    @PutMapping("/material-categories/{id}")
    public Result<MaterialCategoryEntity> updateMaterialCategory(@PathVariable Long id, @RequestBody MaterialCategoryEntity category) {
        return materialCategoryRepository.findById(id).map(existing -> {
            existing.setName(category.getName());
            existing.setCode(category.getCode());
            return Result.success("物料分类更新成功", materialCategoryRepository.save(existing));
        }).orElseGet(() -> Result.fail("物料分类不存在"));
    }

    @DeleteMapping("/material-categories/{id}")
    public Result<String> deleteMaterialCategory(@PathVariable Long id) {
        materialCategoryRepository.deleteById(id);
        return Result.success("物料分类删除成功");
    }

    @GetMapping("/materials")
    public Result<PageResult<MaterialEntity>> getMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<MaterialEntity> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("name"), like),
                        cb.like(root.get("materialCode"), like),
                        cb.like(root.get("specification"), like)
                ));
            }
            if (categoryId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("categoryId"), categoryId));
            }
            if (status != null && !status.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
        Page<MaterialEntity> result = materialRepository.findAll(spec, pageable);
        PageResult<MaterialEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("物料列表查询成功", pageResult);
    }

    @GetMapping("/materials/{id}")
    public Result<MaterialEntity> getMaterialById(@PathVariable Long id) {
        return materialRepository.findById(id)
                .map(entity -> Result.success("物料查询成功", entity))
                .orElseGet(() -> Result.fail("物料不存在"));
    }

    @PostMapping("/materials")
    public Result<MaterialEntity> createMaterial(@RequestBody MaterialEntity material) {
        if (!materialWriteEnabled) {
            return Result.fail("物料主数据权威归 ERP，SRM 禁止写入");
        }
        attachCategoryName(material);
        MaterialEntity saved = materialRepository.save(material);
        return Result.success("物料创建成功", saved);
    }

    @PutMapping("/materials/{id}")
    public Result<MaterialEntity> updateMaterial(@PathVariable Long id, @RequestBody MaterialEntity material) {
        if (!materialWriteEnabled) {
            return Result.fail("物料主数据权威归 ERP，SRM 禁止写入");
        }
        return materialRepository.findById(id).map(existing -> {
            existing.setMaterialCode(material.getMaterialCode());
            existing.setName(material.getName());
            existing.setSpecification(material.getSpecification());
            existing.setUnit(material.getUnit());
            existing.setCategoryId(material.getCategoryId());
            existing.setPrice(material.getPrice());
            existing.setStatus(material.getStatus());
            attachCategoryName(existing);
            return Result.success("物料更新成功", materialRepository.save(existing));
        }).orElseGet(() -> Result.fail("物料不存在"));
    }

    @DeleteMapping("/materials/{id}")
    public Result<String> deleteMaterial(@PathVariable Long id) {
        if (!materialWriteEnabled) {
            return Result.fail("物料主数据权威归 ERP，SRM 禁止写入");
        }
        materialRepository.deleteById(id);
        return Result.success("物料删除成功");
    }

    private void attachCategoryName(MaterialEntity material) {
        Long cid = material.getCategoryId();
        if (cid == null) return;
        Optional<MaterialCategoryEntity> optional = materialCategoryRepository.findById(cid);
        optional.ifPresent(category -> material.setCategoryName(category.getName()));
    }
}
