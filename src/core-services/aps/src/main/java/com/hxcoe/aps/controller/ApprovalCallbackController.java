package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ProductionPlanEntity;
import com.hxcoe.aps.repository.ProductionPlanRepository;
import com.hxcoe.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * APS审批回调控制器。
 * <p>接收OA统一审批完成后的回调通知，根据业务类型（production_plan）
 * 更新对应生产计划状态，实现OA审批结果回流APS的闭环。</p>
 * <p>回调事件结构（approval.completed.v1）：</p>
 * <pre>
 * {
 *   "business": { "businessType": "production_plan", "businessId": "123", "businessNo": "PP..." },
 *   "approvalResult": { "approvalResult": "approved|rejected", "comment": "...", "approverName": "..." }
 * }
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/aps/approval")
public class ApprovalCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalCallbackController.class);

    @Autowired
    private ProductionPlanRepository planRepository;

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
                return Result.error("回调参数不完整");
            }

            boolean approved = "approved".equalsIgnoreCase(result);

            // 2. 按业务类型分发处理（aps_production_plan为标准类型，production_plan兼容历史实例）
            if ("aps_production_plan".equals(businessType) || "production_plan".equals(businessType)) {
                handleProductionPlanCallback(Long.valueOf(businessId), approved, approverName, comment);
            } else {
                logger.warn("未支持的业务类型回调，跳过: businessType={}", businessType);
            }

            return Result.success();
        } catch (Exception e) {
            logger.error("审批回调处理失败: event={}, error={}", event, e.getMessage(), e);
            return Result.error("回调处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理生产计划审批回调：审批通过→approved，审批拒绝→rejected。
     *
     * @param planId       计划ID
     * @param approved     是否通过
     * @param approverName 审批人
     * @param comment      审批意见
     */
    private void handleProductionPlanCallback(Long planId, boolean approved, String approverName, String comment) {
        Optional<ProductionPlanEntity> planOpt = planRepository.findById(planId);
        if (planOpt.isEmpty()) {
            logger.warn("生产计划回调未找到单据: planId={}", planId);
            return;
        }
        
        ProductionPlanEntity plan = planOpt.get();
        // 仅审批中（submitted）状态的计划允许被回调更新，防止重复回调覆盖已排程状态
        if (!"submitted".equals(plan.getStatus())) {
            logger.info("生产计划当前状态不允许回调更新: planId={}, status={}", planId, plan.getStatus());
            return;
        }
        
        plan.setStatus(approved ? "approved" : "rejected");
        if (!approved) {
            plan.setRemark("审批拒绝：" + (comment != null ? comment : ""));
        }
        planRepository.save(plan);
        logger.info("生产计划审批回调已更新: planId={}, planNo={}, status={}",
                planId, plan.getPlanNo(), plan.getStatus());
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
