package com.hxcoe.erp.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.SupplierEntity;
import com.hxcoe.erp.repository.SupplierRepository;
import com.hxcoe.erp.service.SupplierService;
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
import java.util.Optional;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Override
    public PageResult<SupplierEntity> getSupplierList(Integer page, Integer size, String name, String code) {
        if (page == null || page < 1) {
            throw new IllegalArgumentException("page 必须大于等于 1");
        }
        if (size == null || size < 1 || size > 200) {
            throw new IllegalArgumentException("size 必须在 1-200 之间");
        }
        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<SupplierEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));
            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("supplierName"), "%" + name + "%"));
            }
            if (code != null && !code.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("supplierCode"), code));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<SupplierEntity> supplierPage = supplierRepository.findAll(spec, pageable);
        return PageResult.build(supplierPage.getTotalElements(), size, page, supplierPage.getContent());
    }

    @Override
    public SupplierEntity createSupplier(SupplierEntity supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("供应商数据不能为空");
        }
        if (supplier.getSupplierName() == null || supplier.getSupplierName().isBlank()) {
            throw new IllegalArgumentException("supplierName 不能为空");
        }

        if (supplier.getSupplierCode() == null || supplier.getSupplierCode().isBlank()) {
            supplier.setSupplierCode("SUP" + System.currentTimeMillis());
        }

        supplier.setSupplierCode(supplier.getSupplierCode().trim());
        supplier.setSupplierName(supplier.getSupplierName().trim());
        if (supplier.getSupplierType() != null) supplier.setSupplierType(supplier.getSupplierType().trim());
        if (supplier.getContactPerson() != null) supplier.setContactPerson(supplier.getContactPerson().trim());
        if (supplier.getContactPhone() != null) supplier.setContactPhone(supplier.getContactPhone().trim());
        if (supplier.getEmail() != null) supplier.setEmail(supplier.getEmail().trim());
        if (supplier.getFax() != null) supplier.setFax(supplier.getFax().trim());
        if (supplier.getAddress() != null) supplier.setAddress(supplier.getAddress().trim());
        if (supplier.getPaymentMethod() != null) supplier.setPaymentMethod(supplier.getPaymentMethod().trim());
        if (supplier.getBankAccount() != null) supplier.setBankAccount(supplier.getBankAccount().trim());
        if (supplier.getBankName() != null) supplier.setBankName(supplier.getBankName().trim());

        Optional<SupplierEntity> existingByCode = supplierRepository.findBySupplierCode(supplier.getSupplierCode());
        if (existingByCode.isPresent()) {
            throw new IllegalArgumentException("供应商编码已存在: " + supplier.getSupplierCode());
        }

        supplier.setIsDeleted(0);
        supplier.setCreatedTime(LocalDateTime.now());
        supplier.setUpdatedTime(LocalDateTime.now());
        if (supplier.getStatus() == null) supplier.setStatus(1);
        return supplierRepository.save(supplier);
    }

    @Override
    public SupplierEntity updateSupplier(SupplierEntity supplier) {
        if (supplier == null || supplier.getId() == null) {
            throw new IllegalArgumentException("供应商ID不能为空");
        }
        SupplierEntity existing = supplierRepository.findById(supplier.getId()).orElse(null);
        if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("供应商不存在");
        }
        
        if (supplier.getSupplierName() != null) existing.setSupplierName(supplier.getSupplierName());
        if (supplier.getSupplierType() != null) existing.setSupplierType(supplier.getSupplierType());
        if (supplier.getRating() != null) existing.setRating(supplier.getRating());
        if (supplier.getContactPerson() != null) existing.setContactPerson(supplier.getContactPerson());
        if (supplier.getContactPhone() != null) existing.setContactPhone(supplier.getContactPhone());
        if (supplier.getEmail() != null) existing.setEmail(supplier.getEmail());
        if (supplier.getFax() != null) existing.setFax(supplier.getFax());
        if (supplier.getAddress() != null) existing.setAddress(supplier.getAddress());
        if (supplier.getPaymentMethod() != null) existing.setPaymentMethod(supplier.getPaymentMethod());
        if (supplier.getBankAccount() != null) existing.setBankAccount(supplier.getBankAccount());
        if (supplier.getBankName() != null) existing.setBankName(supplier.getBankName());
        if (supplier.getStatus() != null) existing.setStatus(supplier.getStatus());
        if (supplier.getRemark() != null) existing.setRemark(supplier.getRemark());
        
        existing.setUpdatedTime(LocalDateTime.now());
        return supplierRepository.save(existing);
    }

    @Override
    public boolean deleteSupplier(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供应商ID不能为空");
        }
        SupplierEntity existing = supplierRepository.findById(id).orElse(null);
        if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("供应商不存在");
        }
        existing.setIsDeleted(1);
        existing.setUpdatedTime(LocalDateTime.now());
        supplierRepository.save(existing);
        return true;
    }

    @Override
    public SupplierEntity getSupplierById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供应商ID不能为空");
        }
        SupplierEntity existing = supplierRepository.findById(id).orElse(null);
        if (existing == null || (existing.getIsDeleted() != null && existing.getIsDeleted() == 1)) {
            throw new IllegalArgumentException("供应商不存在");
        }
        return existing;
    }
}
