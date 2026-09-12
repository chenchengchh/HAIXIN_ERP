package com.hxcoe.srm.controller;

import com.hxcoe.srm.client.OaApprovalClient;
import com.hxcoe.srm.entity.PurchaseRequestEntity;
import com.hxcoe.srm.service.PurchaseRequestService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 采购申请控制器
 */
@RestController
@RequestMapping("/api/v1/srm")
public class PurchaseRequestController {

    @Autowired
    private PurchaseRequestService purchaseRequestService;

    @Autowired
    private OaApprovalClient oaApprovalClient;

    /**
     * 创建采购申请
     *
     * @param purchaseRequestEntity 采购申请实体
     * @return 创建结果
     */
    @PostMapping("/purchase-requests")
    public Result<PurchaseRequestEntity> createPurchaseRequest(@RequestBody PurchaseRequestEntity purchaseRequestEntity) {
        PurchaseRequestEntity result = purchaseRequestService.createPurchaseRequest(purchaseRequestEntity);
        return Result.success("采购申请创建成功", result);
    }

    /**
     * 获取所有采购申请（分页）
     *
     * @param page 页码
     * @param size 每页大小
     * @return 采购申请分页列表
     */
    @GetMapping("/purchase-requests")
    public Result<PageResult<PurchaseRequestEntity>> getAllPurchaseRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PurchaseRequestEntity> result = purchaseRequestService.getAllPurchaseRequests(pageable);
        PageResult<PurchaseRequestEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("采购申请列表查询成功", pageResult);
    }

    /**
     * 根据ID获取采购申请
     *
     * @param id 采购申请ID
     * @return 采购申请实体
     */
    @GetMapping("/purchase-requests/{id}")
    public Result<PurchaseRequestEntity> getPurchaseRequestById(@PathVariable Long id) {
        Optional<PurchaseRequestEntity> result = purchaseRequestService.getPurchaseRequestById(id);
        if (result.isPresent()) {
            return Result.success("采购申请查询成功", result.get());
        } else {
            return Result.fail("采购申请不存在");
        }
    }

    /**
     * 根据采购申请编号获取采购申请
     *
     * @param requestCode 采购申请编号
     * @return 采购申请实体
     */
    @GetMapping("/purchase-requests/by-code/{requestCode}")
    public Result<PurchaseRequestEntity> getPurchaseRequestByCode(@PathVariable String requestCode) {
        Optional<PurchaseRequestEntity> result = purchaseRequestService.getPurchaseRequestByCode(requestCode);
        if (result.isPresent()) {
            return Result.success("采购申请查询成功", result.get());
        } else {
            return Result.fail("采购申请不存在");
        }
    }

    /**
     * 更新采购申请
     *
     * @param id 采购申请ID
     * @param purchaseRequestEntity 采购申请实体
     * @return 更新结果
     */
    @PutMapping("/purchase-requests/{id}")
    public Result<PurchaseRequestEntity> updatePurchaseRequest(@PathVariable Long id, @RequestBody PurchaseRequestEntity purchaseRequestEntity) {
        PurchaseRequestEntity result = purchaseRequestService.updatePurchaseRequest(id, purchaseRequestEntity);
        if (result != null) {
            return Result.success("采购申请更新成功", result);
        } else {
            return Result.fail("采购申请不存在");
        }
    }

    /**
     * 删除采购申请
     *
     * @param id 采购申请ID
     * @return 删除结果
     */
    @DeleteMapping("/purchase-requests/{id}")
    public Result<String> deletePurchaseRequest(@PathVariable Long id) {
        purchaseRequestService.deletePurchaseRequest(id);
        return Result.success("采购申请删除成功");
    }

    /**
     * 审批采购申请
     *
     * @param id 采购申请ID
     * @param status 审批状态
     * @param approver 审批人
     * @param approvalOpinion 审批意见
     * @return 审批结果
     */
    @PutMapping("/purchase-requests/{id}/approve")
    public Result<PurchaseRequestEntity> approvePurchaseRequest(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam String approver,
            @RequestParam String approvalOpinion) {
        PurchaseRequestEntity result = purchaseRequestService.approvePurchaseRequest(id, status, approver, approvalOpinion);
        if (result != null) {
            return Result.success("采购申请审批成功", result);
        } else {
            return Result.fail("采购申请不存在");
        }
    }

    /**
     * 提交采购申请至OA统一审批。
     *
     * @param id 采购申请ID
     * @return 提交结果
     */
    @PostMapping("/purchase-requests/{id}/submit-approval")
    public Result<Object> submitPurchaseRequestApproval(@PathVariable Long id) {
        Optional<PurchaseRequestEntity> optional = purchaseRequestService.getPurchaseRequestById(id);
        if (optional.isEmpty()) {
            return Result.fail("采购申请不存在");
        }
        PurchaseRequestEntity entity = optional.get();
        if (!"PENDING".equals(entity.getStatus())) {
            return Result.fail("当前状态不允许提交审批");
        }

        Map<String, Object> request = new HashMap<>();
        request.put("sourceSystem", "srm");
        request.put("businessType", "purchase_request");
        request.put("businessId", String.valueOf(entity.getId()));
        request.put("businessNo", entity.getRequestCode());
        request.put("title", "采购申请审批：" + entity.getRequestCode());
        request.put("initiatorId", "1");
        request.put("initiatorName", entity.getApplicant());
        request.put("description", entity.getDescription());
        request.put("callbackUrl", "http://srm:8092/api/v1/srm/approval/callback");

        Result<Object> oaResult = oaApprovalClient.submit(request);
        return Result.success("审批提交成功", oaResult);
    }
}
