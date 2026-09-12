package com.hxcoe.scrm.service.impl;

import com.hxcoe.scrm.entity.CustomerEntity;
import com.hxcoe.scrm.repository.CustomerRepository;
import com.hxcoe.scrm.service.CustomerService;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 客户服务实现类
 */
@Service
public class CustomerServiceImpl implements CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Override
    public CustomerEntity createCustomer(CustomerEntity customer) {
        return customerRepository.save(customer);
    }
    
    @Override
    public CustomerEntity updateCustomer(Long id, CustomerEntity customer) {
        Optional<CustomerEntity> existingCustomer = customerRepository.findById(id);
        if (existingCustomer.isPresent()) {
            CustomerEntity updatedCustomer = existingCustomer.get();
            updatedCustomer.setCustomerCode(customer.getCustomerCode());
            updatedCustomer.setName(customer.getName());
            updatedCustomer.setPhone(customer.getPhone());
            updatedCustomer.setEmail(customer.getEmail());
            updatedCustomer.setGender(customer.getGender());
            updatedCustomer.setBirthDate(customer.getBirthDate());
            updatedCustomer.setSource(customer.getSource());
            updatedCustomer.setLevel(customer.getLevel());
            updatedCustomer.setStatus(customer.getStatus());
            updatedCustomer.setRemark(customer.getRemark());
            updatedCustomer.setCreatedBy(customer.getCreatedBy());
            updatedCustomer.setUpdatedBy(customer.getUpdatedBy());
            return customerRepository.save(updatedCustomer);
        }
        return null;
    }
    
    @Override
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
    
    @Override
    public Optional<CustomerEntity> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }
    
    @Override
    public CustomerEntity getCustomerByCustomerCode(String customerCode) {
        return customerRepository.findByCustomerCode(customerCode);
    }
    
    @Override
    public CustomerEntity getCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }
    
    @Override
    public List<CustomerEntity> getAllCustomers() {
        return customerRepository.findAll();
    }
    
    @Override
    public PageResult<CustomerEntity> getCustomersByPage(Pageable pageable) {
        Page<CustomerEntity> page = customerRepository.findAll(pageable);
        return PageResult.build(
            page.getTotalElements(),
            page.getSize(),
            page.getNumber() + 1,
            page.getContent()
        );
    }
    
    @Override
    public List<CustomerEntity> getCustomersByStatus(String status) {
        return customerRepository.findByStatus(status);
    }
}
