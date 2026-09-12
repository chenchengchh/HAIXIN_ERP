package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.CustomerFollowUpEntity;
import com.hxcoe.crm.service.CustomerFollowUpService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户跟进记录管理控制器
 */
@RestController
@RequestMapping("/api/v1/crm/follow-ups")
public class CustomerFollowUpController {

    @Autowired
    private CustomerFollowUpService followUpService;

    /**
     * 全量分页查询跟进记录列表（按id倒序，支持客户名称/跟进方式/跟进时间范围筛选）
     *
     * @param page         页码，默认1
     * @param size         每页数量，默认10
     * @param customerName 客户名称（可选，模糊匹配）
     * @param followUpType 跟进方式（可选）
     * @param startTime    跟进时间起（可选）
     * @param endTime      跟进时间止（可选）
     * @return 跟进记录分页结果
     */
    @GetMapping
    public Result<PageResult<CustomerFollowUpEntity>> getFollowUpList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String followUpType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {

        // 转换页码，PageRequest从0开始，按id倒序
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<CustomerFollowUpEntity> followUpPage = followUpService.getFollowUpList(
                customerName, followUpType, startTime, endTime, pageable);
        PageResult<CustomerFollowUpEntity> pageResult = PageResult.build(
                followUpPage.getTotalElements(),
                followUpPage.getSize(),
                followUpPage.getNumber() + 1,
                followUpPage.getContent()
        );
        return Result.success(pageResult);
    }

    /**
     * 创建跟进记录
     *
     * @param followUpEntity 跟进记录实体
     * @return 创建结果
     */
    @PostMapping
    public Result<CustomerFollowUpEntity> createFollowUp(@RequestBody CustomerFollowUpEntity followUpEntity) {
        CustomerFollowUpEntity result = followUpService.createFollowUp(followUpEntity);
        return Result.success("跟进记录创建成功", result);
    }

    /**
     * 根据ID查询跟进记录
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    public Result<CustomerFollowUpEntity> getFollowUpById(@PathVariable Long id) {
        CustomerFollowUpEntity result = followUpService.getFollowUpById(id);
        if (result != null) {
            return Result.success("跟进记录查询成功", result);
        } else {
            return Result.fail("跟进记录不存在");
        }
    }

    /**
     * 更新跟进记录
     *
     * @param followUpEntity 跟进记录实体
     * @return 更新结果
     */
    @PutMapping
    public Result<CustomerFollowUpEntity> updateFollowUp(@RequestBody CustomerFollowUpEntity followUpEntity) {
        CustomerFollowUpEntity result = followUpService.updateFollowUp(followUpEntity);
        return Result.success("跟进记录更新成功", result);
    }

    /**
     * 删除跟进记录
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteFollowUp(@PathVariable Long id) {
        boolean result = followUpService.deleteFollowUp(id);
        if (result) {
            return Result.success("跟进记录删除成功", result);
        } else {
            return Result.fail("跟进记录不存在");
        }
    }

    /**
     * 根据客户ID查询跟进记录列表
     *
     * @param customerId 客户ID
     * @return 跟进记录列表
     */
    @GetMapping("/customer/{customerId}")
    public Result<List<CustomerFollowUpEntity>> getFollowUpsByCustomerId(@PathVariable Long customerId) {
        List<CustomerFollowUpEntity> result = followUpService.getFollowUpsByCustomerId(customerId);
        return Result.success("客户跟进记录查询成功", result);
    }

    /**
     * 查询我的待跟进客户
     *
     * @param followUpUserId 跟进人ID
     * @return 待跟进客户的跟进记录列表
     */
    @GetMapping("/my-pending")
    public Result<List<CustomerFollowUpEntity>> getMyPendingFollowUps(@RequestParam Long followUpUserId) {
        List<CustomerFollowUpEntity> result = followUpService.getMyPendingFollowUps(followUpUserId);
        return Result.success("待跟进客户查询成功", result);
    }
}