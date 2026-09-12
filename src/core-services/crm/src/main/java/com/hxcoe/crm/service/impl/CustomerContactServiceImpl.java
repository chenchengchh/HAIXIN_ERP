package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.CustomerContactEntity;
import com.hxcoe.crm.entity.CustomerEntity;
import com.hxcoe.crm.repository.CustomerContactRepository;
import com.hxcoe.crm.repository.CustomerRepository;
import com.hxcoe.crm.service.CustomerContactService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客户联系人服务实现类
 */
@Service
public class CustomerContactServiceImpl implements CustomerContactService {

    @Autowired
    private CustomerContactRepository contactRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public CustomerContactEntity createContact(CustomerContactEntity contactEntity) {
        // 设置默认值
        if (contactEntity.getIsPrimary() == null) {
            contactEntity.setIsPrimary(false);
        }
        // 如果设置为主要联系人，先将其他联系人设置为非主要
        if (contactEntity.getIsPrimary()) {
            setPrimaryContactOnly(contactEntity.getCustomerId(), contactEntity.getId());
        }
        return contactRepository.save(contactEntity);
    }

    @Override
    public CustomerContactEntity getContactById(Long id) {
        return contactRepository.findById(id).orElse(null);
    }

    @Override
    public CustomerContactEntity updateContact(CustomerContactEntity contactEntity) {
        // 如果设置为主要联系人，先将其他联系人设置为非主要
        if (contactEntity.getIsPrimary()) {
            setPrimaryContactOnly(contactEntity.getCustomerId(), contactEntity.getId());
        }
        return contactRepository.save(contactEntity);
    }

    @Override
    public boolean deleteContact(Long id) {
        CustomerContactEntity contact = contactRepository.findById(id).orElse(null);
        if (contact != null) {
            contactRepository.delete(contact);
            return true;
        }
        return false;
    }

    @Override
    public List<CustomerContactEntity> getContactsByCustomerId(Long customerId) {
        return contactRepository.findByCustomerId(customerId);
    }

    @Override
    public CustomerContactEntity getPrimaryContactByCustomerId(Long customerId) {
        return contactRepository.findByCustomerIdAndIsPrimary(customerId, true);
    }

    @Override
    public boolean setPrimaryContact(Long contactId, Long customerId) {
        // 先将所有联系人设置为非主要
        List<CustomerContactEntity> contacts = contactRepository.findByCustomerId(customerId);
        for (CustomerContactEntity contact : contacts) {
            contact.setIsPrimary(false);
        }
        contactRepository.saveAll(contacts);
        
        // 将指定联系人设置为主要
        CustomerContactEntity primaryContact = contactRepository.findById(contactId).orElse(null);
        if (primaryContact != null && primaryContact.getCustomerId().equals(customerId)) {
            primaryContact.setIsPrimary(true);
            contactRepository.save(primaryContact);
            return true;
        }
        return false;
    }

    /**
     * 将指定联系人设置为唯一的主要联系人，其他联系人自动变为非主要
     *
     * @param customerId 客户ID
     * @param contactId 要设置为主要联系人的ID
     */
    private void setPrimaryContactOnly(Long customerId, Long contactId) {
        List<CustomerContactEntity> contacts = contactRepository.findByCustomerId(customerId);
        for (CustomerContactEntity contact : contacts) {
            if (!contact.getId().equals(contactId)) {
                contact.setIsPrimary(false);
            }
        }
        contactRepository.saveAll(contacts);
    }

    /**
     * 全量分页查询联系人，按id倒序
     * 客户名称筛选：先按名称模糊查询匹配的客户ID集合，再以 customerId IN 作为分页条件，保证分页总数正确；
     * 查询完成后批量查询客户信息并填充 customerName 字段
     *
     * @param customerName 客户名称（可选，模糊匹配）
     * @param contactName  联系人姓名（可选，模糊匹配）
     * @param position     职位（可选，模糊匹配）
     * @param pageable     分页参数
     * @return 联系人分页结果
     */
    @Override
    public Page<CustomerContactEntity> getContactList(String customerName, String contactName, String position, Pageable pageable) {
        // 客户名称模糊筛选：先查出匹配的客户ID集合
        List<Long> matchedCustomerIds = null;
        if (customerName != null && !customerName.isBlank()) {
            matchedCustomerIds = customerRepository.findByCustomerNameContaining(customerName)
                    .stream().map(CustomerEntity::getId).collect(Collectors.toList());
            // 无匹配客户时直接返回空分页
            if (matchedCustomerIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        // 动态拼接查询条件
        final List<Long> customerIds = matchedCustomerIds;
        Specification<CustomerContactEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (customerIds != null) {
                predicates.add(root.get("customerId").in(customerIds));
            }
            if (contactName != null && !contactName.isBlank()) {
                predicates.add(cb.like(root.get("contactName"), "%" + contactName + "%"));
            }
            if (position != null && !position.isBlank()) {
                predicates.add(cb.like(root.get("position"), "%" + position + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CustomerContactEntity> contactPage = contactRepository.findAll(spec, pageable);

        // 批量查询客户信息，填充客户名称
        List<Long> pageCustomerIds = contactPage.getContent().stream()
                .map(CustomerContactEntity::getCustomerId)
                .distinct()
                .collect(Collectors.toList());
        if (!pageCustomerIds.isEmpty()) {
            Map<Long, CustomerEntity> customerMap = customerRepository.findAllById(pageCustomerIds).stream()
                    .collect(Collectors.toMap(CustomerEntity::getId, Function.identity()));
            contactPage.getContent().forEach(contact -> {
                CustomerEntity customer = customerMap.get(contact.getCustomerId());
                if (customer != null) {
                    contact.setCustomerName(customer.getCustomerName());
                }
            });
        }
        return contactPage;
    }
}