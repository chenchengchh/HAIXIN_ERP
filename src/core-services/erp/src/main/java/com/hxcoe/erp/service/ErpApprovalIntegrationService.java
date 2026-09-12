package com.hxcoe.erp.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.erp.client.OaApprovalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * ERP审批集成服务。
 * <p>负责将ERP业务单据（凭证/生产订单/供应链单据）提交到OA统一审批中心，
 * 并构造审批回调URL，实现ERP与OA审批流的集成。</p>
 */
@Service
public class ErpApprovalIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(ErpApprovalIntegrationService.class);

    @Autowired
    private OaApprovalClient oaApprovalClient;

    /**
     * ERP服务基础URL（用于OA审批完成后回调）。
     * Docker网络内服务名为 erp，端口 8081。
     */
    @Value("${erp.approval.callback-base-url:http://erp:8081}")
    private String erpBaseUrl;

    /**
     * 提交凭证审批。
     *
     * @param voucherId     凭证ID
     * @param voucherNo     凭证编号
     * @param voucherType   凭证类型
     * @param debitTotal    借方合计
     * @param creditTotal   贷方合计
     * @param summary       摘要
     * @param initiatorId   发起人ID
     * @param initiatorName 发起人名称
     * @return 是否提交成功
     */
    public boolean submitVoucherApproval(Long voucherId, String voucherNo, String voucherType,
                                         Object debitTotal, Object creditTotal, String summary,
                                         Long initiatorId, String initiatorName) {
        Map<String, Object> formData = new HashMap<>();
        formData.put("voucherNo", voucherNo);
        formData.put("voucherType", voucherType);
        formData.put("debitTotal", debitTotal);
        formData.put("creditTotal", creditTotal);
        formData.put("summary", summary);

        Map<String, Object> request = buildBaseRequest(
                "erp", "voucher", String.valueOf(voucherId), voucherNo,
                "ERP凭证审批-" + voucherNo, "凭证摘要：" + (summary != null ? summary : ""),
                initiatorId, initiatorName, formData);

        return doSubmit(request, "voucher", voucherId);
    }

    /**
     * 提交生产订单审批。
     *
     * @param productionId   生产订单ID
     * @param productionNo   生产单号
     * @param productName    产品名称
     * @param quantity       生产数量
     * @param workshop       生产车间
     * @param initiatorId    发起人ID
     * @param initiatorName  发起人名称
     * @return 是否提交成功
     */
    public boolean submitProductionOrderApproval(Long productionId, String productionNo, String productName,
                                                 Object quantity, String workshop,
                                                 Long initiatorId, String initiatorName) {
        Map<String, Object> formData = new HashMap<>();
        formData.put("productionNo", productionNo);
        formData.put("productName", productName);
        formData.put("quantity", quantity);
        formData.put("workshop", workshop);

        Map<String, Object> request = buildBaseRequest(
                "erp", "production_order", String.valueOf(productionId), productionNo,
                "ERP生产订单审批-" + productionNo, "生产产品：" + (productName != null ? productName : ""),
                initiatorId, initiatorName, formData);

        return doSubmit(request, "production_order", productionId);
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
        // OA审批完成后回调ERP的地址
        request.put("callbackUrl", erpBaseUrl + "/api/v1/erp/approval/callback");
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
