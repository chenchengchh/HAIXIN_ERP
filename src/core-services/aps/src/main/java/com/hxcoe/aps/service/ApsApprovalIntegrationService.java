package com.hxcoe.aps.service;

import com.hxcoe.aps.client.OaApprovalClient;
import com.hxcoe.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * APS审批集成服务。
 * <p>负责将APS生产计划提交到OA统一审批中心，
 * 并构造审批回调URL，实现APS与OA审批流的集成。</p>
 */
@Service
public class ApsApprovalIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(ApsApprovalIntegrationService.class);

    @Autowired
    private OaApprovalClient oaApprovalClient;

    /**
     * APS服务基础URL（用于OA审批完成后回调）。
     * Docker网络内服务名为 aps，端口 8087。
     */
    @Value("${aps.approval.callback-base-url:http://aps:8087}")
    private String apsBaseUrl;

    /**
     * 提交生产计划审批。
     *
     * @param planId        计划ID
     * @param planNo        计划编号
     * @param planName      计划名称
     * @param productName   产品名称
     * @param quantity      计划数量
     * @param startDate     开始日期
     * @param endDate       结束日期
     * @param initiatorId   发起人ID
     * @param initiatorName 发起人名称
     * @return 是否提交成功
     */
    public boolean submitProductionPlanApproval(Long planId, String planNo, String planName,
                                               String productName, Object quantity,
                                               String startDate, String endDate,
                                               Long initiatorId, String initiatorName) {
        Map<String, Object> formData = new HashMap<>();
        formData.put("planNo", planNo);
        formData.put("planName", planName);
        formData.put("productName", productName);
        formData.put("quantity", quantity);
        formData.put("startDate", startDate);
        formData.put("endDate", endDate);

        Map<String, Object> request = buildBaseRequest(
                "aps", "aps_production_plan", String.valueOf(planId), planNo,
                "APS生产计划审批-" + planNo, "计划名称：" + (planName != null ? planName : ""),
                initiatorId, initiatorName, formData);

        return doSubmit(request, "aps_production_plan", planId);
    }

    /**
     * 构造审批申请基础请求体。
     *
     * @param sourceSystem   来源系统
     * @param businessType   业务类型
     * @param businessId     业务ID
     * @param businessNo     业务单号
     * @param title          审批标题
     * @param description    审批描述
     * @param initiatorId    发起人ID
     * @param initiatorName  发起人名称
     * @param formData       表单数据
     * @return 请求体
     */
    private Map<String, Object> buildBaseRequest(String sourceSystem, String businessType, String businessId,
                                                 String businessNo, String title, String description,
                                                 Long initiatorId, String initiatorName,
                                                 Map<String, Object> formData) {
        Map<String, Object> request = new HashMap<>();
        request.put("sourceSystem", sourceSystem);
        request.put("businessType", businessType);
        request.put("businessId", businessId);
        request.put("businessNo", businessNo);
        request.put("title", title);
        request.put("description", description);
        request.put("initiatorId", initiatorId);
        request.put("initiatorName", initiatorName);
        request.put("formData", formData);
        // OA审批完成后回调APS的地址
        request.put("callbackUrl", apsBaseUrl + "/api/v1/aps/approval/callback");
        return request;
    }

    /**
     * 执行OA审批提交，并处理异常（失败不阻断本地流程，记录日志）。
     *
     * @param request      请求体
     * @param businessType 业务类型（日志用）
     * @param businessId   业务ID（日志用）
     * @return 是否提交成功
     */
    private boolean doSubmit(Map<String, Object> request, String businessType, Long businessId) {
        try {
            Result<Object> result = oaApprovalClient.submit(request);
            // OA统一审批服务成功码为0（ResultCode.SUCCESS），网关/其他服务可能返回200，两者均视为成功
            Integer code = result != null ? result.getCode() : null;
            if (code != null && (code == 0 || code == 200)) {
                logger.info("OA审批提交成功: businessType={}, businessId={}", businessType, businessId);
                return true;
            }
            logger.warn("OA审批提交返回异常: businessType={}, businessId={}, result={}",
                    businessType, businessId, result != null ? result.getMsg() : "null");
            return false;
        } catch (Exception e) {
            // OA服务不可用时记录错误，不阻断本地提交（本地状态已置为submitted/审批中）
            logger.error("OA审批提交失败: businessType={}, businessId={}, error={}",
                    businessType, businessId, e.getMessage(), e);
            return false;
        }
    }
}
