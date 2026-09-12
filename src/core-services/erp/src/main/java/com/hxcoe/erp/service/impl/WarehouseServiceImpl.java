package com.hxcoe.erp.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.WarehouseEntity;
import com.hxcoe.erp.repository.WarehouseRepository;
import com.hxcoe.erp.service.WarehouseService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    /**
     * 分页查询仓库列表（仅返回未删除数据）。
     *
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @param name 仓库名称（模糊查询）
     * @param code 仓库编码（精确查询）
     * @return 分页结果
     */
    public PageResult<WarehouseEntity> getWarehouseList(Integer page, Integer size, String name, String code) {
        if (page == null || page < 1) {
            throw new IllegalArgumentException("page 必须大于等于 1");
        }
        if (size == null || size < 1 || size > 200) {
            throw new IllegalArgumentException("size 必须在 1-200 之间");
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<WarehouseEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));
            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("warehouseName"), "%" + name + "%"));
            }
            if (code != null && !code.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("warehouseCode"), code));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<WarehouseEntity> warehousePage = warehouseRepository.findAll(spec, pageable);
        return PageResult.build(warehousePage.getTotalElements(), size, page, warehousePage.getContent());
    }

    @Override
    /**
     * 创建仓库（自动填充删除标识与创建/更新时间）。
     *
     * @param warehouse 仓库实体
     * @return 创建后的仓库
     */
    public WarehouseEntity createWarehouse(WarehouseEntity warehouse) {
        if (warehouse == null) {
            throw new IllegalArgumentException("仓库数据不能为空");
        }
        if (warehouse.getWarehouseCode() == null || warehouse.getWarehouseCode().isBlank()) {
            throw new IllegalArgumentException("warehouseCode 不能为空");
        }
        if (warehouse.getWarehouseName() == null || warehouse.getWarehouseName().isBlank()) {
            throw new IllegalArgumentException("warehouseName 不能为空");
        }
        if (warehouseRepository.findByWarehouseCode(warehouse.getWarehouseCode().trim()).isPresent()) {
            throw new IllegalArgumentException("仓库编码已存在: " + warehouse.getWarehouseCode());
        }

        warehouse.setWarehouseCode(warehouse.getWarehouseCode().trim());
        warehouse.setWarehouseName(warehouse.getWarehouseName().trim());
        warehouse.setIsDeleted(0);
        warehouse.setCreatedTime(LocalDateTime.now());
        warehouse.setUpdatedTime(LocalDateTime.now());
        if (warehouse.getStatus() == null) warehouse.setStatus(1);
        return warehouseRepository.save(warehouse);
    }

    @Override
    /**
     * 更新仓库（不允许更新仓库编码）。
     *
     * @param warehouse 仓库实体（必须包含id）
     * @return 更新后的仓库
     */
    public WarehouseEntity updateWarehouse(WarehouseEntity warehouse) {
        if (warehouse == null || warehouse.getId() == null) {
            throw new IllegalArgumentException("仓库ID不能为空");
        }
        WarehouseEntity existing = warehouseRepository.findById(warehouse.getId()).orElse(null);
        if (existing == null || existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
            throw new IllegalArgumentException("仓库不存在");
        }
        
        if (warehouse.getWarehouseName() != null) existing.setWarehouseName(warehouse.getWarehouseName());
        if (warehouse.getWarehouseType() != null) existing.setWarehouseType(warehouse.getWarehouseType());
        if (warehouse.getLocation() != null) existing.setLocation(warehouse.getLocation());
        if (warehouse.getManager() != null) existing.setManager(warehouse.getManager());
        if (warehouse.getStatus() != null) existing.setStatus(warehouse.getStatus());
        if (warehouse.getRemark() != null) existing.setRemark(warehouse.getRemark());
        
        existing.setUpdatedTime(LocalDateTime.now());
        return warehouseRepository.save(existing);
    }

    @Override
    /**
     * 逻辑删除仓库（将 is_deleted 置为 1）。
     *
     * @param id 仓库ID
     * @return 是否删除成功
     */
    public boolean deleteWarehouse(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("仓库ID不能为空");
        }
        WarehouseEntity existing = warehouseRepository.findById(id).orElse(null);
        if (existing != null && (existing.getIsDeleted() == null || existing.getIsDeleted() == 0)) {
            existing.setIsDeleted(1);
            existing.setUpdatedTime(LocalDateTime.now());
            warehouseRepository.save(existing);
            return true;
        }
        throw new IllegalArgumentException("仓库不存在");
    }

    @Override
    /**
     * 根据ID查询仓库（仅返回未删除数据）。
     *
     * @param id 仓库ID
     * @return 仓库实体
     */
    public WarehouseEntity getWarehouseById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("仓库ID不能为空");
        }
        WarehouseEntity existing = warehouseRepository.findById(id).orElse(null);
        if (existing == null || existing.getIsDeleted() != null && existing.getIsDeleted() == 1) {
            throw new IllegalArgumentException("仓库不存在");
        }
        return existing;
    }
}
