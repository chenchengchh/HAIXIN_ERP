package com.hxcoe.qms.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.dto.inspection.InspectionItemDTO;
import com.hxcoe.qms.dto.inspection.InspectionStandardDTO;
import com.hxcoe.qms.entity.InspectionStandardEntity;
import com.hxcoe.qms.repository.InspectionStandardRepository;
import com.hxcoe.qms.service.InspectionStandardService;
import com.hxcoe.qms.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 检验标准服务实现
 */
@Service
public class InspectionStandardServiceImpl implements InspectionStandardService {

    @Autowired
    private InspectionStandardRepository inspectionStandardRepository;

    /**
     * 分页查询检验标准
     *
     * @param standardNo 标准编号（模糊）
     * @param materialName 物料名称（模糊）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<InspectionStandardDTO>> page(String standardNo, String materialName, String status, Pageable pageable) {
        Specification<InspectionStandardEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (standardNo != null && !standardNo.isBlank()) {
                predicates.add(cb.like(root.get("standardNo"), "%" + standardNo.trim() + "%"));
            }
            if (materialName != null && !materialName.isBlank()) {
                predicates.add(cb.like(root.get("materialName"), "%" + materialName.trim() + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<InspectionStandardEntity> page = inspectionStandardRepository.findAll(specification, pageable);
        List<InspectionStandardDTO> dtos = page.getContent().stream().map(this::toDto).toList();
        PageResult<InspectionStandardDTO> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, dtos);
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取检验标准
     *
     * @param id 标准ID
     * @return 检验标准
     */
    @Override
    public Result<InspectionStandardDTO> getById(Long id) {
        InspectionStandardEntity entity = inspectionStandardRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验标准不存在");
        }
        return Result.success(toDto(entity));
    }

    /**
     * 根据标准编号获取检验标准
     *
     * @param standardNo 标准编号
     * @return 检验标准
     */
    @Override
    public Result<InspectionStandardDTO> getByStandardNo(String standardNo) {
        InspectionStandardEntity entity = inspectionStandardRepository.findByStandardNo(standardNo).orElse(null);
        if (entity == null) {
            return Result.error("检验标准不存在");
        }
        return Result.success(toDto(entity));
    }

    /**
     * 创建检验标准
     *
     * @param dto 检验标准
     * @return 创建结果
     */
    @Override
    public Result<InspectionStandardDTO> create(InspectionStandardDTO dto) {
        if (dto.getStandardNo() == null || dto.getStandardNo().isBlank()) {
            return Result.error("标准编号不能为空");
        }
        if (inspectionStandardRepository.findByStandardNo(dto.getStandardNo().trim()).isPresent()) {
            return Result.error("标准编号已存在");
        }

        InspectionStandardEntity entity = new InspectionStandardEntity();
        entity.setStandardNo(dto.getStandardNo().trim());
        entity.setMaterialCode(dto.getMaterialCode());
        entity.setMaterialName(dto.getMaterialName());
        entity.setVersion(dto.getVersion());
        entity.setAqlLevel(dto.getAqlLevel());
        entity.setStatus(dto.getStatus() == null ? "active" : dto.getStatus());
        entity.setInspectionItemsJson(JsonUtils.toJson(dto.getInspectionItems()));
        entity.setCreator(dto.getCreator());
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        InspectionStandardEntity saved = inspectionStandardRepository.save(entity);
        return Result.success(toDto(saved));
    }

    /**
     * 更新检验标准
     *
     * @param id 标准ID
     * @param dto 更新数据
     * @return 更新结果
     */
    @Override
    public Result<InspectionStandardDTO> update(Long id, InspectionStandardDTO dto) {
        InspectionStandardEntity entity = inspectionStandardRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验标准不存在");
        }

        if (dto.getStandardNo() != null && !dto.getStandardNo().isBlank()) {
            String nextStandardNo = dto.getStandardNo().trim();
            if (!nextStandardNo.equals(entity.getStandardNo())
                    && inspectionStandardRepository.findByStandardNo(nextStandardNo).isPresent()) {
                return Result.error("标准编号已存在");
            }
            entity.setStandardNo(nextStandardNo);
        }
        if (dto.getMaterialCode() != null) {
            entity.setMaterialCode(dto.getMaterialCode());
        }
        if (dto.getMaterialName() != null) {
            entity.setMaterialName(dto.getMaterialName());
        }
        if (dto.getVersion() != null) {
            entity.setVersion(dto.getVersion());
        }
        if (dto.getAqlLevel() != null) {
            entity.setAqlLevel(dto.getAqlLevel());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getInspectionItems() != null) {
            entity.setInspectionItemsJson(JsonUtils.toJson(dto.getInspectionItems()));
        }
        if (dto.getCreator() != null) {
            entity.setCreator(dto.getCreator());
        }
        entity.setUpdatedTime(LocalDateTime.now());

        InspectionStandardEntity saved = inspectionStandardRepository.save(entity);
        return Result.success(toDto(saved));
    }

    /**
     * 删除检验标准
     *
     * @param id 标准ID
     * @return 删除结果
     */
    @Override
    public Result<Void> delete(Long id) {
        if (!inspectionStandardRepository.existsById(id)) {
            return Result.error("检验标准不存在");
        }
        inspectionStandardRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 启用检验标准
     *
     * @param id 标准ID
     * @return 更新结果
     */
    @Override
    public Result<Void> activate(Long id) {
        InspectionStandardEntity entity = inspectionStandardRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验标准不存在");
        }
        entity.setStatus("active");
        entity.setUpdatedTime(LocalDateTime.now());
        inspectionStandardRepository.save(entity);
        return Result.success();
    }

    /**
     * 停用检验标准
     *
     * @param id 标准ID
     * @return 更新结果
     */
    @Override
    public Result<Void> deactivate(Long id) {
        InspectionStandardEntity entity = inspectionStandardRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("检验标准不存在");
        }
        entity.setStatus("inactive");
        entity.setUpdatedTime(LocalDateTime.now());
        inspectionStandardRepository.save(entity);
        return Result.success();
    }

    private InspectionStandardDTO toDto(InspectionStandardEntity entity) {
        InspectionStandardDTO dto = new InspectionStandardDTO();
        dto.setId(entity.getId());
        dto.setStandardNo(entity.getStandardNo());
        dto.setMaterialCode(entity.getMaterialCode());
        dto.setMaterialName(entity.getMaterialName());
        dto.setVersion(entity.getVersion());
        dto.setAqlLevel(entity.getAqlLevel());
        dto.setStatus(entity.getStatus());
        dto.setCreator(entity.getCreator());
        dto.setCreateTime(entity.getCreatedTime());
        dto.setUpdateTime(entity.getUpdatedTime());

        List<InspectionItemDTO> items = JsonUtils.fromJson(entity.getInspectionItemsJson(), new TypeReference<List<InspectionItemDTO>>() {});
        dto.setInspectionItems(items);
        return dto;
    }
}
