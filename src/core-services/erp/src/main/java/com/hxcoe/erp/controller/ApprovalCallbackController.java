package com.hxcoe.erp.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.erp.entity.ProductionEntity;
import com.hxcoe.erp.entity.VoucherEntity;
import com.hxcoe.erp.repository.ProductionRepository;
import com.hxcoe.erp.repository.VoucherRepository;
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
 * ERP审批回调控制器。
 * <p>接收OA统一审批完成后的回调通知，根据业务类型（voucher/production_order/supply_chain）
 * 分发更新对应业务单据状态，实现OA审批结果回流ERP的闭环。</p>
 * <p>回调事件结构（approval.completed.v1）：</p>
 * <pre>
 * {
 *   "business": { "businessType": "voucher", "businessId": "123", "businessNo": "JZ..." },
 *   "approvalResult": { "approvalResult": "approved|rejected", "comment": "...", "approverName": "..." }
 * }
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/erp/approval")
public class ApprovalCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalCallbackController.class);

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private ProductionRepository productionRepository;

    /**
     * 接收OA审批完成回调。
     *
     * @param event 回调事件
     * @return 处理结果
     */
    @PostMapping("/callback")
    public ApiResponse<Void> handleApprovalCallback(@RequestBody Map<String, Object> event) {
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
                return ApiResponse.error(400, "回调参数不完整");
            }

            boolean approved = "approved".equalsIgnoreCase(result);

            // 2. 按业务类型分发处理
            switch (businessType) {
                case "voucher" -> handleVoucherCallback(Long.valueOf(businessId), approved, approverName, comment);
                case "production_order" -> handleProductionCallback(Long.valueOf(businessId), approved, approverName, comment);
                default -> logger.warn("未支持的业务类型回调，跳过: businessType={}", businessType);
            }

            return ApiResponse.success("回调处理成功", null);
        } catch (Exception e) {
            logger.error("审批回调处理失败: event={}, error={}", event, e.getMessage(), e);
            return ApiResponse.error(500, "回调处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理凭证审批回调：审批通过→approved，审批拒绝→rejected。
     *
     * @param voucherId    凭证ID
     * @param approved     是否通过
     * @param approverName 审批人
     * @param comment      审批意见
     */
    private void handleVoucherCallback(Long voucherId, boolean approved, String approverName, String comment) {
        VoucherEntity voucher = voucherRepository.findById(voucherId).orElse(null);
        if (voucher == null) {
            logger.warn("凭证回调未找到单据: voucherId={}", voucherId);
            return;
        }
        // 仅审批中（submitted）状态的凭证允许被回调更新，防止重复回调覆盖已过账状态
        if (!"submitted".equals(voucher.getStatus())) {
            logger.info("凭证当前状态不允许回调更新: voucherId={}, status={}", voucherId, voucher.getStatus());
            return;
        }
        voucher.setStatus(approved ? "approved" : "rejected");
        voucher.setUpdatedBy(approverName);
        voucher.setUpdatedTime(LocalDateTime.now());
        voucherRepository.save(voucher);
        logger.info("凭证审批回调已更新: voucherId={}, voucherNo={}, status={}",
                voucherId, voucher.getVoucherNo(), voucher.getStatus());
    }

    /**
     * 处理生产订单审批回调：审批通过→已审核(1)，审批拒绝→回退草稿(0)。
     *
     * @param productionId 生产订单ID
     * @param approved     是否通过
     * @param approverName 审批人
     * @param comment      审批意见
     */
    private void handleProductionCallback(Long productionId, boolean approved, String approverName, String comment) {
        ProductionEntity production = productionRepository.findById(productionId).orElse(null);
        if (production == null) {
            logger.warn("生产订单回调未找到单据: productionId={}", productionId);
            return;
        }
        // 仅待生产（0）状态允许被回调更新，防止覆盖已开始生产的状态
        if (production.getProductionStatus() == null || production.getProductionStatus() != 0) {
            logger.info("生产订单当前状态不允许回调更新: productionId={}, status={}",
                    productionId, production.getProductionStatus());
            return;
        }
        production.setProductionStatus(approved ? 1 : 0);
        if (!approved) {
            production.setRemark("审批拒绝：" + (comment != null ? comment : ""));
        }
        production.setUpdatedBy(approverName);
        production.setUpdatedTime(LocalDateTime.now());
        productionRepository.save(production);
        logger.info("生产订单审批回调已更新: productionId={}, productionNo={}, status={}",
                productionId, production.getProductionNo(), production.getProductionStatus());
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
