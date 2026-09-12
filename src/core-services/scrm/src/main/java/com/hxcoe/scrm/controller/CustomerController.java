package com.hxcoe.scrm.controller;

import com.hxcoe.scrm.entity.CustomerEntity;
import com.hxcoe.scrm.service.CustomerService;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 客户控制器
 */
@RestController
@RequestMapping({"/scrm/customers", "/api/v1/scrm/customers"})
public class CustomerController {
    
    @Autowired
    private CustomerService customerService;
    
    /**
     * 创建客户
     * @param customer 客户实体
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Result<CustomerEntity>> createCustomer(@RequestBody CustomerEntity customer) {
        CustomerEntity createdCustomer = customerService.createCustomer(customer);
        return ResponseEntity.ok(Result.success(createdCustomer));
    }
    
    /**
     * 更新客户
     * @param id 客户ID
     * @param customer 更新的客户实体
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<CustomerEntity>> updateCustomer(@PathVariable Long id, @RequestBody CustomerEntity customer) {
        CustomerEntity updatedCustomer = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(Result.success(updatedCustomer));
    }
    
    /**
     * 删除客户
     * @param id 客户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(Result.success());
    }
    
    /**
     * 根据ID查询客户
     * @param id 客户ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<CustomerEntity>> getCustomerById(@PathVariable Long id) {
        Optional<CustomerEntity> customer = customerService.getCustomerById(id);
        return customer.map(c -> ResponseEntity.ok(Result.success(c)))
                .orElseGet(() -> ResponseEntity.ok(Result.fail("客户不存在")));
    }
    
    /**
     * 根据客户编号查询客户
     * @param customerCode 客户编号
     * @return 查询结果
     */
    @GetMapping("/by-code/{customerCode}")
    public ResponseEntity<Result<CustomerEntity>> getCustomerByCustomerCode(@PathVariable String customerCode) {
        CustomerEntity customer = customerService.getCustomerByCustomerCode(customerCode);
        return ResponseEntity.ok(Result.success(customer));
    }
    
    /**
     * 根据手机号码查询客户
     * @param phone 手机号码
     * @return 查询结果
     */
    @GetMapping("/by-phone/{phone}")
    public ResponseEntity<Result<CustomerEntity>> getCustomerByPhone(@PathVariable String phone) {
        CustomerEntity customer = customerService.getCustomerByPhone(phone);
        return ResponseEntity.ok(Result.success(customer));
    }
    
    /**
     * 查询所有客户
     * @return 查询结果
     */
    @GetMapping
    public ResponseEntity<Result<List<CustomerEntity>>> getAllCustomers() {
        List<CustomerEntity> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(Result.success(customers));
    }
    
    /**
     * 分页查询客户
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 查询结果
     */
    @GetMapping("/page")
    public ResponseEntity<Result<PageResult<CustomerEntity>>> getCustomersByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        PageResult<CustomerEntity> customers = customerService.getCustomersByPage(pageable);
        return ResponseEntity.ok(Result.success(customers));
    }
    
    /**
     * 根据客户状态查询客户
     * @param status 客户状态
     * @return 查询结果
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<Result<List<CustomerEntity>>> getCustomersByStatus(@PathVariable String status) {
        List<CustomerEntity> customers = customerService.getCustomersByStatus(status);
        return ResponseEntity.ok(Result.success(customers));
    }
}
