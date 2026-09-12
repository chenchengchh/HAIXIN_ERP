package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcRegistrationEntity;
import com.hxcoe.qms.service.NcRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 不合格品登记管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/nc-registrations")
public class NcRegistrationController {

    @Autowired
    private NcRegistrationService ncRegistrationService;

    /**
     * 分页查询不合格品登记列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param registrationNo 登记编号（模糊）
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<NcRegistrationEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String registrationNo,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return ncRegistrationService.page(registrationNo, status, pageable);
    }

    /**
     * 获取不合格品登记详情
     *
     * @param id 登记ID
     * @return 登记详情
     */
    @GetMapping("/{id}")
    public Result<NcRegistrationEntity> getById(@PathVariable Long id) {
        return ncRegistrationService.getById(id);
    }

    /**
     * 创建不合格品登记
     *
     * @param entity 登记数据
     * @return 创建结果
     */
    @PostMapping
    public Result<NcRegistrationEntity> create(@RequestBody NcRegistrationEntity entity) {
        return ncRegistrationService.create(entity);
    }

    /**
     * 更新不合格品登记
     *
     * @param id 登记ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<NcRegistrationEntity> update(@PathVariable Long id, @RequestBody NcRegistrationEntity entity) {
        return ncRegistrationService.update(id, entity);
    }

    /**
     * 删除不合格品登记
     *
     * @param id 登记ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return ncRegistrationService.delete(id);
    }

    /**
     * 提交评审
     *
     * @param id 登记ID
     * @return 提交结果
     */
    @PutMapping("/{id}/submit-review")
    public Result<Void> submitReview(@PathVariable Long id) {
        return ncRegistrationService.submitReview(id);
    }
}
