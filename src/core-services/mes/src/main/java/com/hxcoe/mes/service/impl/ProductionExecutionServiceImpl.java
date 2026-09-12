package com.hxcoe.mes.service.impl;

import com.hxcoe.mes.entity.ProductionExecutionEntity;
import com.hxcoe.mes.repository.ProductionExecutionRepository;
import com.hxcoe.mes.service.ProductionExecutionService;
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
 * 生产执行服务实现类
 */
@Service
public class ProductionExecutionServiceImpl implements ProductionExecutionService {

    @Autowired
    private ProductionExecutionRepository productionExecutionRepository;

    @Override
    public ProductionExecutionEntity createProductionExecution(ProductionExecutionEntity productionExecutionEntity) {
        if (productionExecutionEntity.getIsDeleted() == null) {
            productionExecutionEntity.setIsDeleted(0);
        }
        if (productionExecutionEntity.getCreatedTime() == null) {
            productionExecutionEntity.setCreatedTime(LocalDateTime.now());
        }
        if (productionExecutionEntity.getUpdatedTime() == null) {
            productionExecutionEntity.setUpdatedTime(LocalDateTime.now());
        }
        if (productionExecutionEntity.getActualQuantity() == null) {
            productionExecutionEntity.setActualQuantity(0);
        }
        if (productionExecutionEntity.getQualifiedQuantity() == null) {
            productionExecutionEntity.setQualifiedQuantity(0);
        }
        if (productionExecutionEntity.getUnqualifiedQuantity() == null) {
            productionExecutionEntity.setUnqualifiedQuantity(0);
        }
        if (productionExecutionEntity.getExecutionStatus() == null) {
            productionExecutionEntity.setExecutionStatus(1);
        }
        return productionExecutionRepository.save(productionExecutionEntity);
    }

    @Override
    public ProductionExecutionEntity getProductionExecutionById(Long id) {
        return productionExecutionRepository.findById(id).orElse(null);
    }

    @Override
    public ProductionExecutionEntity getProductionExecutionByNo(String executionNo) {
        return productionExecutionRepository.findByExecutionNo(executionNo);
    }

    @Override
    public ProductionExecutionEntity updateProductionExecution(ProductionExecutionEntity productionExecutionEntity) {
        productionExecutionEntity.setUpdatedTime(LocalDateTime.now());
        return productionExecutionRepository.save(productionExecutionEntity);
    }

    @Override
    public boolean deleteProductionExecution(Long id) {
        ProductionExecutionEntity productionExecutionEntity = productionExecutionRepository.findById(id).orElse(null);
        if (productionExecutionEntity != null) {
            productionExecutionEntity.setIsDeleted(1);
            productionExecutionEntity.setUpdatedTime(LocalDateTime.now());
            productionExecutionRepository.save(productionExecutionEntity);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<ProductionExecutionEntity> getProductionExecutionList(Integer page, Integer size, String executionNo, String productionOrderNo, String productCode, String productionLine, Integer executionStatus, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<ProductionExecutionEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));
            if (executionNo != null && !executionNo.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("executionNo"), "%" + executionNo + "%"));
            }
            if (productionOrderNo != null && !productionOrderNo.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("productionOrderNo"), "%" + productionOrderNo + "%"));
            }
            if (productCode != null && !productCode.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("productCode"), productCode));
            }
            if (productionLine != null && !productionLine.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("productionLine"), productionLine));
            }
            if (executionStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("executionStatus"), executionStatus));
            }
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("startTime"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), endDate));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<ProductionExecutionEntity> executionPage = productionExecutionRepository.findAll(spec, pageable);
        return PageResult.build(
                executionPage.getTotalElements(),
                size,
                page,
                executionPage.getContent()
        );
    }

    @Override
    public List<ProductionExecutionEntity> getProductionExecutionByStatus(Integer executionStatus) {
        return productionExecutionRepository.findByExecutionStatus(executionStatus);
    }

    @Override
    public boolean startProductionExecution(Long id) {
        ProductionExecutionEntity productionExecutionEntity = productionExecutionRepository.findById(id).orElse(null);
        if (productionExecutionEntity != null && productionExecutionEntity.getExecutionStatus() != null) {
            if (productionExecutionEntity.getExecutionStatus() == 1 || productionExecutionEntity.getExecutionStatus() == 4) {
                productionExecutionEntity.setExecutionStatus(2);
                productionExecutionEntity.setStartTime(LocalDateTime.now());
                productionExecutionEntity.setUpdatedTime(LocalDateTime.now());
                productionExecutionRepository.save(productionExecutionEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pauseProductionExecution(Long id) {
        ProductionExecutionEntity productionExecutionEntity = productionExecutionRepository.findById(id).orElse(null);
        if (productionExecutionEntity != null && productionExecutionEntity.getExecutionStatus() != null) {
            if (productionExecutionEntity.getExecutionStatus() == 2) {
                productionExecutionEntity.setExecutionStatus(4);
                productionExecutionEntity.setUpdatedTime(LocalDateTime.now());
                productionExecutionRepository.save(productionExecutionEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean resumeProductionExecution(Long id) {
        ProductionExecutionEntity productionExecutionEntity = productionExecutionRepository.findById(id).orElse(null);
        if (productionExecutionEntity != null && productionExecutionEntity.getExecutionStatus() != null) {
            if (productionExecutionEntity.getExecutionStatus() == 4) {
                productionExecutionEntity.setExecutionStatus(2);
                productionExecutionEntity.setUpdatedTime(LocalDateTime.now());
                productionExecutionRepository.save(productionExecutionEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean completeProductionExecution(Long id) {
        ProductionExecutionEntity productionExecutionEntity = productionExecutionRepository.findById(id).orElse(null);
        if (productionExecutionEntity != null && productionExecutionEntity.getExecutionStatus() != null) {
            if (productionExecutionEntity.getExecutionStatus() == 2 || productionExecutionEntity.getExecutionStatus() == 4) {
                productionExecutionEntity.setExecutionStatus(3);
                if (productionExecutionEntity.getStartTime() == null) {
                    productionExecutionEntity.setStartTime(LocalDateTime.now());
                }
                productionExecutionEntity.setEndTime(LocalDateTime.now());
                productionExecutionEntity.setUpdatedTime(LocalDateTime.now());
                productionExecutionRepository.save(productionExecutionEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean cancelProductionExecution(Long id) {
        ProductionExecutionEntity productionExecutionEntity = productionExecutionRepository.findById(id).orElse(null);
        if (productionExecutionEntity != null && productionExecutionEntity.getExecutionStatus() != null) {
            if (productionExecutionEntity.getExecutionStatus() == 1 || productionExecutionEntity.getExecutionStatus() == 2 || productionExecutionEntity.getExecutionStatus() == 4) {
                productionExecutionEntity.setExecutionStatus(5);
                productionExecutionEntity.setUpdatedTime(LocalDateTime.now());
                productionExecutionRepository.save(productionExecutionEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateProductionQuantity(Long id, Integer actualQuantity, Integer qualifiedQuantity, Integer unqualifiedQuantity) {
        ProductionExecutionEntity productionExecutionEntity = productionExecutionRepository.findById(id).orElse(null);
        if (productionExecutionEntity != null) {
            productionExecutionEntity.setActualQuantity(actualQuantity);
            productionExecutionEntity.setQualifiedQuantity(qualifiedQuantity);
            productionExecutionEntity.setUnqualifiedQuantity(unqualifiedQuantity);
            productionExecutionEntity.setUpdatedTime(LocalDateTime.now());
            productionExecutionRepository.save(productionExecutionEntity);
            return true;
        }
        return false;
    }
}
