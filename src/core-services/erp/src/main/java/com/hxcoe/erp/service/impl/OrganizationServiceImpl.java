package com.hxcoe.erp.service.impl;

import com.hxcoe.erp.entity.OrganizationEntity;
import com.hxcoe.erp.repository.OrganizationRepository;
import com.hxcoe.erp.service.OrganizationService;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 组织服务实现类
 */
@Service
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    /**
     * 创建组织
     *
     * @param organizationEntity 组织实体
     * @return 创建结果
     */
    @Override
    @Transactional
    public OrganizationEntity createOrganization(OrganizationEntity organizationEntity) {
        if (organizationEntity == null) {
            throw new IllegalArgumentException("组织数据不能为空");
        }
        if (organizationEntity.getOrganizationName() == null || organizationEntity.getOrganizationName().isBlank()) {
            throw new IllegalArgumentException("organizationName 不能为空");
        }
        if (organizationEntity.getOrganizationType() == null || organizationEntity.getOrganizationType().isBlank()) {
            throw new IllegalArgumentException("organizationType 不能为空");
        }

        if (organizationEntity.getOrganizationCode() == null || organizationEntity.getOrganizationCode().isBlank()) {
            organizationEntity.setOrganizationCode("ORG" + System.currentTimeMillis());
        }

        organizationEntity.setOrganizationCode(organizationEntity.getOrganizationCode().trim());
        organizationEntity.setOrganizationName(organizationEntity.getOrganizationName().trim());
        organizationEntity.setOrganizationType(organizationEntity.getOrganizationType().trim());

        if (organizationRepository.findByOrganizationCode(organizationEntity.getOrganizationCode()).isPresent()) {
            throw new IllegalArgumentException("组织编码已存在: " + organizationEntity.getOrganizationCode());
        }

        if (organizationEntity.getLevel() == null || organizationEntity.getLevel() < 1) {
            organizationEntity.setLevel(1);
        }
        if (organizationEntity.getStatus() == null) {
            organizationEntity.setStatus(1);
        }
        if (organizationEntity.getIsDeleted() == null) {
            organizationEntity.setIsDeleted(0);
        } else {
            organizationEntity.setIsDeleted(0);
        }
        LocalDateTime now = LocalDateTime.now();
        if (organizationEntity.getCreatedTime() == null) {
            organizationEntity.setCreatedTime(now);
        }
        organizationEntity.setUpdatedTime(now);

        return organizationRepository.save(organizationEntity);
    }

    /**
     * 根据ID查询组织
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @Override
    public OrganizationEntity getOrganizationById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("组织ID不能为空");
        }
        Optional<OrganizationEntity> optional = organizationRepository.findById(id);
        OrganizationEntity entity = optional.orElse(null);
        if (entity == null || (entity.getIsDeleted() != null && entity.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("组织不存在");
        }
        return entity;
    }

    /**
     * 更新组织
     *
     * @param organizationEntity 组织实体
     * @return 更新结果
     */
    @Override
    @Transactional
    public OrganizationEntity updateOrganization(OrganizationEntity organizationEntity) {
        if (organizationEntity == null || organizationEntity.getId() == null) {
            throw new IllegalArgumentException("组织ID不能为空");
        }

        OrganizationEntity existing = organizationRepository.findById(organizationEntity.getId()).orElse(null);
        if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("组织不存在");
        }

        if (organizationEntity.getOrganizationName() != null) {
            existing.setOrganizationName(organizationEntity.getOrganizationName().trim());
        }
        if (organizationEntity.getOrganizationType() != null) {
            existing.setOrganizationType(organizationEntity.getOrganizationType().trim());
        }
        if (organizationEntity.getParent() != null) {
            existing.setParent(organizationEntity.getParent());
        }
        if (organizationEntity.getLevel() != null && organizationEntity.getLevel() >= 1) {
            existing.setLevel(organizationEntity.getLevel());
        }
        if (organizationEntity.getStatus() != null) {
            existing.setStatus(organizationEntity.getStatus());
        }
        if (organizationEntity.getRemark() != null) {
            existing.setRemark(organizationEntity.getRemark());
        }

        existing.setUpdatedTime(LocalDateTime.now());
        return organizationRepository.save(existing);
    }

    /**
     * 删除组织
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public boolean deleteOrganization(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("组织ID不能为空");
        }
        OrganizationEntity existing = organizationRepository.findById(id).orElse(null);
        if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("组织不存在");
        }

        existing.setIsDeleted(1);
        existing.setUpdatedTime(LocalDateTime.now());
        organizationRepository.save(existing);
        return true;
    }

    /**
     * 分页查询组织列表
     *
     * @param page 页码
     * @param size 每页条数
     * @param name 组织名称
     * @param code 组织编码
     * @return 分页结果
     */
    @Override
    public PageResult<OrganizationEntity> getOrganizationList(Integer page, Integer size, String name, String code) {
        if (page == null || page < 1) {
            throw new IllegalArgumentException("page 必须大于等于 1");
        }
        if (size == null || size < 1 || size > 200) {
            throw new IllegalArgumentException("size 必须在 1-200 之间");
        }
        // 构建分页请求
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "level").and(Sort.by(Sort.Direction.ASC, "organizationCode")));
        
        // 构建查询条件
        Specification<OrganizationEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));
            
            // 组织名称条件（模糊查询）
            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("organizationName"), "%" + name + "%"));
            }
            
            // 组织编码条件（模糊查询）
            if (code != null && !code.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("organizationCode"), "%" + code + "%"));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        // 执行查询
        Page<OrganizationEntity> resultPage = organizationRepository.findAll(spec, pageable);
        
        // 转换为自定义分页结果
        return PageResult.build(
                resultPage.getTotalElements(),
                size,
                page,
                resultPage.getContent()
        );
    }
}
