package com.hxcoe.erp.service.impl;

import com.hxcoe.erp.entity.SupplyChainEntity;
import com.hxcoe.erp.repository.SupplyChainRepository;
import com.hxcoe.erp.service.SupplyChainService;
import com.hxcoe.common.result.PageResult;
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

/**
 * 供应链服务实现类
 */
@Service
public class SupplyChainServiceImpl implements SupplyChainService {

    @Autowired
    private SupplyChainRepository supplyChainRepository;

    @Override
    public SupplyChainEntity createSupplyChain(SupplyChainEntity supplyChainEntity) {
        // 设置默认值
        if (supplyChainEntity.getIsDeleted() == null) {
            supplyChainEntity.setIsDeleted(0);
        }
        if (supplyChainEntity.getCreatedTime() == null) {
            supplyChainEntity.setCreatedTime(LocalDateTime.now());
        }
        if (supplyChainEntity.getUpdatedTime() == null) {
            supplyChainEntity.setUpdatedTime(LocalDateTime.now());
        }
        return supplyChainRepository.save(supplyChainEntity);
    }

    @Override
    public SupplyChainEntity getSupplyChainById(Long id) {
        return supplyChainRepository.findById(id).orElse(null);
    }

    @Override
    public SupplyChainEntity getSupplyChainByNo(String scNo) {
        return supplyChainRepository.findByScNo(scNo);
    }

    @Override
    public SupplyChainEntity updateSupplyChain(SupplyChainEntity supplyChainEntity) {
        // 更新时间
        supplyChainEntity.setUpdatedTime(LocalDateTime.now());
        return supplyChainRepository.save(supplyChainEntity);
    }

    @Override
    public boolean deleteSupplyChain(Long id) {
        SupplyChainEntity supplyChainEntity = supplyChainRepository.findById(id).orElse(null);
        if (supplyChainEntity != null) {
            // 逻辑删除
            supplyChainEntity.setIsDeleted(1);
            supplyChainEntity.setUpdatedTime(LocalDateTime.now());
            supplyChainRepository.save(supplyChainEntity);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<SupplyChainEntity> getSupplyChainList(Integer page, Integer size, Integer businessType, String materialCode, String warehouseCode, Integer status, LocalDateTime startDate, LocalDateTime endDate) {
        // 构建分页请求
        Pageable pageable = PageRequest.of(page - 1, size);

        // 构建查询条件
        Specification<SupplyChainEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 过滤逻辑删除
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));

            // 业务类型条件
            if (businessType != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessType"), businessType));
            }

            // 物料编码条件
            if (materialCode != null && !materialCode.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("materialCode"), materialCode));
            }

            // 仓库编码条件
            if (warehouseCode != null && !warehouseCode.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("warehouseCode"), warehouseCode));
            }

            // 状态条件
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 日期范围条件
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("createdTime"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdTime"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdTime"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<SupplyChainEntity> scPage = supplyChainRepository.findAll(spec, pageable);

        // 构建分页结果
        return PageResult.build(
                scPage.getTotalElements(),
                size,
                page,
                scPage.getContent()
        );
    }

    @Override
    public List<SupplyChainEntity> getSupplyChainByMaterialCode(String materialCode) {
        return supplyChainRepository.findByMaterialCode(materialCode);
    }

    @Override
    public List<SupplyChainEntity> getSupplyChainByWarehouseCode(String warehouseCode) {
        return supplyChainRepository.findByWarehouseCode(warehouseCode);
    }
}
