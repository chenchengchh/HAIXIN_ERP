package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.CustomerContactEntity;
import com.hxcoe.crm.service.CustomerContactService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户联系人管理控制器
 */
@RestController
@RequestMapping("/api/v1/crm/contacts")
public class CustomerContactController {

    @Autowired
    private CustomerContactService contactService;

    /**
     * 全量分页查询联系人列表（按id倒序，支持客户名称/联系人姓名/职位模糊筛选）
     *
     * @param page         页码，默认1
     * @param size         每页数量，默认10
     * @param customerName 客户名称（可选，模糊匹配）
     * @param contactName  联系人姓名（可选，模糊匹配）
     * @param position     职位（可选，模糊匹配）
     * @return 联系人分页结果
     */
    @GetMapping
    public Result<PageResult<CustomerContactEntity>> getContactList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String contactName,
            @RequestParam(required = false) String position) {

        // 转换页码，PageRequest从0开始，按id倒序
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<CustomerContactEntity> contactPage = contactService.getContactList(customerName, contactName, position, pageable);
        PageResult<CustomerContactEntity> pageResult = PageResult.build(
                contactPage.getTotalElements(),
                contactPage.getSize(),
                contactPage.getNumber() + 1,
                contactPage.getContent()
        );
        return Result.success(pageResult);
    }

    /**
     * 创建联系人
     *
     * @param contactEntity 联系人实体
     * @return 创建结果
     */
    @PostMapping
    public Result<CustomerContactEntity> createContact(@RequestBody CustomerContactEntity contactEntity) {
        CustomerContactEntity result = contactService.createContact(contactEntity);
        return Result.success("联系人创建成功", result);
    }

    /**
     * 根据ID查询联系人
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    public Result<CustomerContactEntity> getContactById(@PathVariable Long id) {
        CustomerContactEntity result = contactService.getContactById(id);
        if (result != null) {
            return Result.success("联系人查询成功", result);
        } else {
            return Result.fail("联系人不存在");
        }
    }

    /**
     * 更新联系人
     *
     * @param contactEntity 联系人实体
     * @return 更新结果
     */
    @PutMapping
    public Result<CustomerContactEntity> updateContact(@RequestBody CustomerContactEntity contactEntity) {
        CustomerContactEntity result = contactService.updateContact(contactEntity);
        return Result.success("联系人更新成功", result);
    }

    /**
     * 删除联系人
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteContact(@PathVariable Long id) {
        boolean result = contactService.deleteContact(id);
        if (result) {
            return Result.success("联系人删除成功", result);
        } else {
            return Result.fail("联系人不存在");
        }
    }

    /**
     * 根据客户ID查询联系人列表
     *
     * @param customerId 客户ID
     * @return 联系人列表
     */
    @GetMapping("/customer/{customerId}")
    public Result<List<CustomerContactEntity>> getContactsByCustomerId(@PathVariable Long customerId) {
        List<CustomerContactEntity> result = contactService.getContactsByCustomerId(customerId);
        return Result.success("联系人列表查询成功", result);
    }

    /**
     * 根据客户ID查询主要联系人
     *
     * @param customerId 客户ID
     * @return 主要联系人
     */
    @GetMapping("/customer/{customerId}/primary")
    public Result<CustomerContactEntity> getPrimaryContactByCustomerId(@PathVariable Long customerId) {
        CustomerContactEntity result = contactService.getPrimaryContactByCustomerId(customerId);
        if (result != null) {
            return Result.success("主要联系人查询成功", result);
        } else {
            return Result.fail("主要联系人不存在");
        }
    }

    /**
     * 设置主要联系人
     *
     * @param contactId 联系人ID
     * @param customerId 客户ID
     * @return 设置结果
     */
    @PutMapping("/{contactId}/primary")
    public Result<Boolean> setPrimaryContact(@PathVariable Long contactId, @RequestParam Long customerId) {
        boolean result = contactService.setPrimaryContact(contactId, customerId);
        if (result) {
            return Result.success("主要联系人设置成功", result);
        } else {
            return Result.fail("主要联系人设置失败");
        }
    }
}