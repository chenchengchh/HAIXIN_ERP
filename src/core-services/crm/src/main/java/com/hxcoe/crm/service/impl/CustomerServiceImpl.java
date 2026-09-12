package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.CustomerEntity;
import com.hxcoe.crm.repository.CustomerRepository;
import com.hxcoe.crm.service.CustomerService;
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
 * 客户服务实现类
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public CustomerEntity createCustomer(CustomerEntity customerEntity) {
        // 设置默认值
        if (customerEntity.getIsDeleted() == null) {
            customerEntity.setIsDeleted(false);
        }
        if (customerEntity.getCreateTime() == null) {
            customerEntity.setCreateTime(LocalDateTime.now());
        }
        if (customerEntity.getUpdateTime() == null) {
            customerEntity.setUpdateTime(LocalDateTime.now());
        }
        if (customerEntity.getStatus() == null) {
            customerEntity.setStatus("potential"); // 默认潜在客户
        }
        if (customerEntity.getLevel() == null) {
            customerEntity.setLevel("C"); // 默认C级客户
        }
        return customerRepository.save(customerEntity);
    }

    @Override
    public CustomerEntity getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    @Override
    public CustomerEntity getCustomerByNo(String customerNo) {
        return customerRepository.findByCustomerNo(customerNo);
    }

    @Override
    public CustomerEntity updateCustomer(CustomerEntity customerEntity) {
        // 更新时间
        customerEntity.setUpdateTime(LocalDateTime.now());
        return customerRepository.save(customerEntity);
    }

    @Override
    public boolean deleteCustomer(Long id) {
        CustomerEntity customerEntity = customerRepository.findById(id).orElse(null);
        if (customerEntity != null) {
            // 逻辑删除
            customerEntity.setIsDeleted(true);
            customerEntity.setUpdateTime(LocalDateTime.now());
            customerRepository.save(customerEntity);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<CustomerEntity> getCustomerList(Integer page, Integer size, String customerNo, String customerName, String customerType, String industry, String scale, String level, String status, String source, String ownerName, LocalDateTime startDate, LocalDateTime endDate) {
        // 构建分页请求
        Pageable pageable = PageRequest.of(page - 1, size);

        // 构建查询条件
        Specification<CustomerEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 过滤逻辑删除
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // 客户编号条件
            if (customerNo != null && !customerNo.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("customerNo"), customerNo));
            }

            // 客户名称条件
            if (customerName != null && !customerName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("customerName"), "%" + customerName + "%"));
            }

            // 客户类型条件
            if (customerType != null && !customerType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("customerType"), customerType));
            }

            // 行业条件
            if (industry != null && !industry.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("industry"), industry));
            }

            // 规模条件
            if (scale != null && !scale.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("scale"), scale));
            }

            // 级别条件
            if (level != null && !level.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("level"), level));
            }

            // 状态条件
            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 来源条件
            if (source != null && !source.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("source"), source));
            }

            // 负责人姓名条件
            if (ownerName != null && !ownerName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("ownerName"), "%" + ownerName + "%"));
            }

            // 日期范围条件
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("createTime"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<CustomerEntity> customerPage = customerRepository.findAll(spec, pageable);

        // 构建分页结果
        return PageResult.build(
                customerPage.getTotalElements(),
                size,
                page,
                customerPage.getContent()
        );
    }

    @Override
    public List<CustomerEntity> searchCustomerByName(String customerName) {
        return customerRepository.findByCustomerNameContaining(customerName);
    }

    @Override
    public boolean updateCustomerStatus(Long id, String status) {
        CustomerEntity customerEntity = customerRepository.findById(id).orElse(null);
        if (customerEntity != null) {
            customerEntity.setStatus(status);
            customerEntity.setUpdateTime(LocalDateTime.now());
            customerRepository.save(customerEntity);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateCustomerLevel(Long id, String level) {
        CustomerEntity customerEntity = customerRepository.findById(id).orElse(null);
        if (customerEntity != null) {
            customerEntity.setLevel(level);
            customerEntity.setUpdateTime(LocalDateTime.now());
            customerRepository.save(customerEntity);
            return true;
        }
        return false;
    }
}
