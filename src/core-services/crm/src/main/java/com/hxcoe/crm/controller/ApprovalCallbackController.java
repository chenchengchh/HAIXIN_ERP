package com.hxcoe.crm.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.crm.service.SalesOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * CRM审批回调控制器。
 * <p>接收OA统一审批完成后的回调通知，根据业务类型（sales_order）
 * 分发更新对应业务单据状态，实现OA审批结果回流CRM的闭环。</p>
 * <p>回调事件结构（approval.completed.v1）：</p>
 * <pre>
 * {
 *   "business": { "businessType": "sales_order", "businessId": "123", "businessNo": "SO..." },
 *   "approvalResult": { "approvalResult": "approved|rejected", "comment": "...", "approverName": "..." }
 * }
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/crm/approval")
public class ApprovalCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalCallbackController.class);

    @Autowired
    private SalesOrderService salesOrderService;

    /**
     * 接收OA审批完成回调。
     *
     * @param event 回调事件
     * @return 处理结果
     */
    @PostMapping("/callback")
    public Result<Void> handleApprovalCallback(@RequestBody Map<String, Object> event) {
        try {
            // 1. 解析业务信息
            Map<String, Object> business = asMap(event.get("business"));
            Map<String, Object> approvalResult = asMap(event.get("approvalResult"));
            String businessType = asString(business.get("businessType"));
            String businessId = asString(business.get("businessId"));
            String businessNo = asString(business.get("businessNo"));
            String result = asString(approvalResult.get("approvalResult"));
            String approverName = asString(approvalResult.get("approverName"));
            String comment = asString(approvalResult.get("comment"));

            logger.info("收到OA审批回调: businessType={}, businessId={}, businessNo={}, result={}, approver={}",
                    businessType, businessId, businessNo, result, approverName);

            if (businessType == null || businessId == null || result == null) {
                logger.warn("审批回调参数不完整，跳过处理: event={}", event);
                return Result.fail("回调参数不完整");
            }

            boolean approved = "approved".equalsIgnoreCase(result);

            // 2. 按业务类型分发处理
            if ("sales_order".equals(businessType)) {
                handleSalesOrderCallback(Long.valueOf(businessId), approved, comment);
            } else {
                logger.warn("未支持的业务类型回调，跳过: businessType={}", businessType);
            }

            return Result.success("回调处理成功", null);
        } catch (Exception e) {
            logger.error("审批回调处理失败: event={}, error={}", event, e.getMessage(), e);
            return Result.fail("回调处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理销售订单审批回调。
     * <p>审批通过：复用approveOrder逻辑（状态置approved并联动WMS出库/ERP推送）；
     * 审批拒绝：状态置rejected并回写审批意见。</p>
     *
     * @param orderId 订单ID
     * @param approved 是否通过
     * @param comment 审批意见
     */
    private void handleSalesOrderCallback(Long orderId, boolean approved, String comment) {
        if (approved) {
            salesOrderService.approveOrder(orderId);
            logger.info("销售订单审批回调已通过: orderId={}", orderId);
        } else {
            salesOrderService.rejectOrder(orderId, comment);
            logger.info("销售订单审批回调已拒绝: orderId={}, comment={}", orderId, comment);
        }
    }

    /**
     * 安全转换为Map。
     *
     * @param obj 源对象
     * @return Map实例，转换失败返回空Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return Map.of();
    }

    /**
     * 安全转换为字符串。
     *
     * @param obj 源对象
     * @return 字符串，null返回null
     */
    private String asString(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }
}
