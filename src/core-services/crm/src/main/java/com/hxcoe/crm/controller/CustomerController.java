package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.CustomerEntity;
import com.hxcoe.crm.entity.CustomerTagEntity;
import com.hxcoe.crm.entity.dto.Customer360ViewDTO;
import com.hxcoe.crm.service.Customer360Service;
import com.hxcoe.crm.service.CustomerService;
import com.hxcoe.crm.service.CustomerTagService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 客户管理控制器
 */
@RestController
@RequestMapping("/api/v1/crm/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private Customer360Service customer360Service;

    @Autowired
    private CustomerTagService tagService;

    /**
     * 创建客户
     *
     * @param customerEntity 客户实体
     * @return 创建结果
     */
    @PostMapping
    public Result<CustomerEntity> createCustomer(@RequestBody CustomerEntity customerEntity) {
        CustomerEntity result = customerService.createCustomer(customerEntity);
        return Result.success("客户创建成功", result);
    }

    /**
     * 根据ID查询客户
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    public Result<CustomerEntity> getCustomerById(@PathVariable Long id) {
        CustomerEntity result = customerService.getCustomerById(id);
        if (result != null) {
            return Result.success("客户查询成功", result);
        } else {
            return Result.fail("客户不存在");
        }
    }

    /**
     * 根据客户编号查询客户
     *
     * @param customerNo 客户编号
     * @return 查询结果
     */
    @GetMapping("/no/{customerNo}")
    public Result<CustomerEntity> getCustomerByNo(@PathVariable String customerNo) {
        CustomerEntity result = customerService.getCustomerByNo(customerNo);
        if (result != null) {
            return Result.success("客户查询成功", result);
        } else {
            return Result.fail("客户不存在");
        }
    }

    /**
     * 更新客户
     *
     * @param customerEntity 客户实体
     * @return 更新结果
     */
    @PutMapping
    public Result<CustomerEntity> updateCustomer(@RequestBody CustomerEntity customerEntity) {
        CustomerEntity result = customerService.updateCustomer(customerEntity);
        return Result.success("客户更新成功", result);
    }

    /**
     * 删除客户
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCustomer(@PathVariable Long id) {
        boolean result = customerService.deleteCustomer(id);
        if (result) {
            return Result.success("客户删除成功", result);
        } else {
            return Result.fail("客户不存在");
        }
    }

    /**
     * 分页查询客户列表
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param customerNo 客户编号
     * @param customerName 客户名称
     * @param customerType 客户类型
     * @param industry 行业
     * @param scale 规模
     * @param level 级别
     * @param status 状态
     * @param source 来源
     * @param ownerName 负责人姓名
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<CustomerEntity>> getCustomerList(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "customerNo", required = false) String customerNo,
            @RequestParam(name = "customerName", required = false) String customerName,
            @RequestParam(name = "customerType", required = false) String customerType,
            @RequestParam(name = "industry", required = false) String industry,
            @RequestParam(name = "scale", required = false) String scale,
            @RequestParam(name = "level", required = false) String level,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "source", required = false) String source,
            @RequestParam(name = "ownerName", required = false) String ownerName,
            @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        PageResult<CustomerEntity> result = customerService.getCustomerList(page, size, customerNo, customerName, customerType, industry, scale, level, status, source, ownerName, startDate, endDate);
        return Result.success("客户列表查询成功", result);
    }

    /**
     * 根据客户名称模糊查询
     *
     * @param customerName 客户名称
     * @return 客户实体列表
     */
    @GetMapping("/search")
    public Result<Object> searchCustomerByName(@RequestParam String customerName) {
        return Result.success("客户查询成功", customerService.searchCustomerByName(customerName));
    }

    /**
     * 更新客户状态
     *
     * @param id 客户ID
     * @param status 客户状态
     * @return 更新结果
     */
    @PutMapping("/{id}/status/{status}")
    public Result<Boolean> updateCustomerStatus(@PathVariable Long id, @PathVariable String status) {
        boolean result = customerService.updateCustomerStatus(id, status);
        if (result) {
            return Result.success("客户状态更新成功", result);
        } else {
            return Result.fail("客户不存在");
        }
    }

    /**
     * 更新客户级别
     *
     * @param id 客户ID
     * @param level 客户级别
     * @return 更新结果
     */
    @PutMapping("/{id}/level/{level}")
    public Result<Boolean> updateCustomerLevel(@PathVariable Long id, @PathVariable String level) {
        boolean result = customerService.updateCustomerLevel(id, level);
        if (result) {
            return Result.success("客户级别更新成功", result);
        } else {
            return Result.fail("客户不存在");
        }
    }

    /**
     * 获取客户360°视图数据
     *
     * @param id 客户ID
     * @return 客户360°视图数据
     */
    @GetMapping("/{id}/360-view")
    public Result<Customer360ViewDTO> getCustomer360View(@PathVariable Long id) {
        Customer360ViewDTO result = customer360Service.getCustomer360View(id);
        if (result != null) {
            return Result.success("客户360°视图查询成功", result);
        } else {
            return Result.fail("客户不存在");
        }
    }

    /**
     * 为客户添加标签
     *
     * @param customerId 客户ID
     * @param tagIds 标签ID列表
     * @return 添加结果
     */
    @PostMapping("/{customerId}/tags")
    public Result<Boolean> addTagsToCustomer(@PathVariable Long customerId, @RequestBody List<Long> tagIds) {
        for (Long tagId : tagIds) {
            tagService.addTagToCustomer(customerId, tagId);
        }
        return Result.success("标签添加成功", true);
    }

    /**
     * 为客户移除标签
     *
     * @param customerId 客户ID
     * @param tagId 标签ID
     * @return 移除结果
     */
    @DeleteMapping("/{customerId}/tags/{tagId}")
    public Result<Boolean> removeTagFromCustomer(@PathVariable Long customerId, @PathVariable Long tagId) {
        boolean result = tagService.removeTagFromCustomer(customerId, tagId);
        if (result) {
            return Result.success("标签移除成功", result);
        } else {
            return Result.fail("标签移除失败");
        }
    }

    /**
     * 设置客户标签
     *
     * @param customerId 客户ID
     * @param tagIds 标签ID列表
     * @return 设置结果
     */
    @PutMapping("/{customerId}/tags")
    public Result<Boolean> setTagsToCustomer(@PathVariable Long customerId, @RequestBody List<Long> tagIds) {
        boolean result = tagService.setTagsToCustomer(customerId, tagIds);
        if (result) {
            return Result.success("标签设置成功", result);
        } else {
            return Result.fail("标签设置失败");
        }
    }

    /**
     * 获取客户标签
     *
     * @param customerId 客户ID
     * @return 客户标签列表
     */
    @GetMapping("/{customerId}/tags")
    public Result<List<CustomerTagEntity>> getCustomerTags(@PathVariable Long customerId) {
        List<CustomerTagEntity> tags = tagService.getTagsByCustomerId(customerId);
        return Result.success("客户标签查询成功", tags);
    }

    /**
     * 测试客户管理接口
     *
     * @return 测试结果
     */
    @GetMapping("/test")
    public Result<String> testCustomer() {
        return Result.success("客户管理接口测试成功");
    }
}
