package com.hxcoe.srm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.srm.entity.PurchaseRequestEntity;
import com.hxcoe.srm.repository.PurchaseRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * SRM审批回调控制器。
 * <p>接收OA统一审批完成后的回调通知，根据业务类型（purchase_request）
 * 更新对应采购申请状态，实现OA审批结果回流SRM的闭环。</p>
 * <p>回调事件结构（approval.completed.v1）：</p>
 * <pre>
 * {
 *   "business": { "businessType": "purchase_request", "businessId": "123", "businessNo": "PR-..." },
 *   "approvalResult": { "approvalResult": "approved|rejected", "comment": "...", "approverName": "..." }
 * }
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/srm/approval")
public class ApprovalCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalCallbackController.class);

    @Autowired
    private PurchaseRequestRepository purchaseRequestRepository;

    /**
     * 接收OA审批完成回调。
     *
     * @param event 回调事件
     * @return 处理结果
     */
    @PostMapping("/callback")
    public ApiResponse<Void> handleApprovalCallback(@RequestBody Map<String, Object> event) {
        try {
            Map<String, Object> business = asMap(event.get("business"));
            Map<String, Object> approvalResult = asMap(event.get("approvalResult"));
            String businessType = asString(business.get("businessType"));
            String businessId = asString(business.get("businessId"));
            String result = asString(approvalResult.get("approvalResult"));
            String approverName = asString(approvalResult.get("approverName"));
            String comment = asString(approvalResult.get("comment"));

            logger.info("收到OA审批回调: businessType={}, businessId={}, result={}, approver={}",
                    businessType, businessId, result, approverName);

            if (businessType == null || businessId == null || result == null) {
                logger.warn("审批回调参数不完整，跳过处理: event={}", event);
                return ApiResponse.error(400, "回调参数不完整");
            }

            boolean approved = "approved".equalsIgnoreCase(result);

            if ("purchase_request".equals(businessType)) {
                handlePurchaseRequestCallback(Long.valueOf(businessId), approved, approverName, comment);
            } else {
                logger.warn("未支持的业务类型回调，跳过: businessType={}", businessType);
            }

            return ApiResponse.success("回调处理成功", null);
        } catch (Exception e) {
            logger.error("审批回调处理失败: event={}, error={}", event, e.getMessage(), e);
            return ApiResponse.error(500, "回调处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理采购申请审批回调：审批通过→APPROVED，审批拒绝→REJECTED。
     *
     * @param requestId    采购申请ID
     * @param approved     是否通过
     * @param approverName 审批人
     * @param comment      审批意见
     */
    private void handlePurchaseRequestCallback(Long requestId, boolean approved, String approverName, String comment) {
        PurchaseRequestEntity request = purchaseRequestRepository.findById(requestId).orElse(null);
        if (request == null) {
            logger.warn("采购申请回调未找到单据: requestId={}", requestId);
            return;
        }
        if (!"PENDING".equals(request.getStatus())) {
            logger.info("采购申请当前状态不允许回调更新: requestId={}, status={}", requestId, request.getStatus());
            return;
        }
        request.setStatus(approved ? "APPROVED" : "REJECTED");
        request.setApprover(approverName);
        request.setApprovalOpinion(comment);
        request.setApprovalTime(LocalDateTime.now());
        request.setUpdatedTime(LocalDateTime.now());
        purchaseRequestRepository.save(request);
        logger.info("采购申请审批回调已更新: requestId={}, requestCode={}, status={}",
                requestId, request.getRequestCode(), request.getStatus());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return Map.of();
    }

    private String asString(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }
}
