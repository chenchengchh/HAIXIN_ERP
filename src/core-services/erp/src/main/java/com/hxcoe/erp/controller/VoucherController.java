package com.hxcoe.erp.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.VoucherEntity;
import com.hxcoe.erp.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 凭证控制器
 */
@RestController
@RequestMapping("/api/v1/erp/finance/vouchers")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    /**
     * 获取凭证列表
     *
     * @param page 页码
     * @param size 每页条数
     * @param voucherType 凭证类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PageResult<VoucherEntity>> getVouchers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String voucherType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        PageResult<VoucherEntity> result = voucherService.getVoucherList(page, size, voucherType, status, startDate, endDate);
        return success("操作成功", result);
    }

    /**
     * 创建凭证
     *
     * @param voucherEntity 凭证实体
     * @return 创建结果
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VoucherEntity> createVoucher(@Validated @RequestBody VoucherEntity voucherEntity) {
        VoucherEntity result = voucherService.createVoucher(voucherEntity);
        return success("操作成功", result);
    }

    /**
     * 更新凭证
     *
     * @param id 凭证ID
     * @param voucherEntity 凭证实体
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<VoucherEntity> updateVoucher(@PathVariable Long id, @Validated @RequestBody VoucherEntity voucherEntity) {
        voucherEntity.setId(id);
        VoucherEntity result = voucherService.updateVoucher(voucherEntity);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("凭证不存在");
    }

    /**
     * 删除凭证
     *
     * @param id 凭证ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteVoucher(@PathVariable Long id) {
        boolean result = voucherService.deleteVoucher(id);
        if (result) {
            return success("操作成功", null);
        }
        return notFound("凭证不存在");
    }

    /**
     * 审核凭证
     *
     * @param id 凭证ID
     * @return 审核结果
     */
    @PostMapping("/{id}/approve")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<VoucherEntity> approveVoucher(@PathVariable Long id) {
        VoucherEntity result = voucherService.approveVoucher(id);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("凭证不存在");
    }

    /**
     * 过账凭证
     *
     * @param id 凭证ID
     * @return 过账结果
     */
    @PostMapping("/{id}/post")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<VoucherEntity> postVoucher(@PathVariable Long id) {
        VoucherEntity result = voucherService.postVoucher(id);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("凭证不存在");
    }

    /**
     * 提交凭证（发起OA统一审批）。
     * <p>凭证状态置为submitted（审批中），同时向OA提交审批申请，
     * 审批完成后OA回调更新凭证状态为approved/rejected。</p>
     *
     * @param id            凭证ID
     * @param initiatorId   发起人ID（前端传入当前登录用户ID）
     * @param initiatorName 发起人名称（前端传入当前登录用户名称）
     * @return 提交结果
     */
    @PostMapping("/{id}/submit")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<VoucherEntity> submitVoucher(
            @PathVariable Long id,
            @RequestParam(required = false) Long initiatorId,
            @RequestParam(required = false) String initiatorName) {
        VoucherEntity result = voucherService.submitVoucherForApproval(id, initiatorId, initiatorName);
        if (result != null) {
            return success("凭证已提交审批", result);
        }
        return notFound("凭证不存在");
    }

    /**
     * 拒绝凭证
     *
     * @param id 凭证ID
     * @return 拒绝结果
     */
    @PostMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<VoucherEntity> rejectVoucher(@PathVariable Long id) {
        VoucherEntity result = voucherService.rejectVoucher(id);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("凭证不存在");
    }

    /**
     * 根据ID查询凭证
     *
     * @param id 凭证ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<VoucherEntity> getVoucherById(@PathVariable Long id) {
        VoucherEntity result = voucherService.getVoucherById(id);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("凭证不存在");
    }

    /**
     * 根据凭证编号查询凭证
     *
     * @param voucherNo 凭证编号
     * @return 查询结果
     */
    @GetMapping("/no/{voucherNo}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<VoucherEntity> getVoucherByNo(@PathVariable String voucherNo) {
        VoucherEntity result = voucherService.getVoucherByNo(voucherNo);
        if (result != null) {
            return success("操作成功", result);
        }
        return notFound("凭证不存在");
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
