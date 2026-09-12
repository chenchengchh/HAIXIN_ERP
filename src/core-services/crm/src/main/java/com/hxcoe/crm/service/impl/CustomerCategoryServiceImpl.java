package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.CustomerCategoryEntity;
import com.hxcoe.crm.repository.CustomerCategoryRepository;
import com.hxcoe.crm.service.CustomerCategoryService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户分类服务实现类
 */
@Service
public class CustomerCategoryServiceImpl implements CustomerCategoryService {

    @Autowired
    private CustomerCategoryRepository categoryRepository;

    /**
     * 创建分类，默认父分类为根、状态启用、排序自动递增
     */
    @Override
    public CustomerCategoryEntity createCategory(CustomerCategoryEntity category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortOrder() == null) {
            int maxSortOrder = categoryRepository.findAll().stream()
                    .mapToInt(c -> c.getSortOrder() != null ? c.getSortOrder() : 0)
                    .max()
                    .orElse(0);
            category.setSortOrder(maxSortOrder + 1);
        }
        return categoryRepository.save(category);
    }

    /**
     * 根据ID查询分类
     */
    @Override
    public CustomerCategoryEntity getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    /**
     * 更新分类的非空字段
     */
    @Override
    public CustomerCategoryEntity updateCategory(Long id, CustomerCategoryEntity category) {
        CustomerCategoryEntity existing = categoryRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        if (category.getCategoryName() != null) {
            existing.setCategoryName(category.getCategoryName());
        }
        if (category.getCategoryCode() != null) {
            existing.setCategoryCode(category.getCategoryCode());
        }
        if (category.getParentId() != null) {
            existing.setParentId(category.getParentId());
        }
        if (category.getDescription() != null) {
            existing.setDescription(category.getDescription());
        }
        if (category.getSortOrder() != null) {
            existing.setSortOrder(category.getSortOrder());
        }
        if (category.getStatus() != null) {
            existing.setStatus(category.getStatus());
        }
        return categoryRepository.save(existing);
    }

    /**
     * 删除分类，存在子分类时返回2禁止删除
     */
    @Override
    public int deleteCategory(Long id) {
        CustomerCategoryEntity existing = categoryRepository.findById(id).orElse(null);
        if (existing == null) {
            return 1;
        }
        List<CustomerCategoryEntity> children = categoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            return 2;
        }
        categoryRepository.delete(existing);
        return 0;
    }

    /**
     * 分页查询分类列表，支持名称/编码关键词模糊匹配
     */
    @Override
    public Page<CustomerCategoryEntity> getCategoryList(String keyword, Pageable pageable) {
        Specification<CustomerCategoryEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                Predicate p1 = cb.like(root.get("categoryName"), "%" + keyword + "%");
                Predicate p2 = cb.like(root.get("categoryCode"), "%" + keyword + "%");
                predicates.add(cb.or(p1, p2));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return categoryRepository.findAll(spec, pageable);
    }

    /**
     * 查询所有分类
     */
    @Override
    public List<CustomerCategoryEntity> getAllCategories() {
        return categoryRepository.findAll();
    }
}
