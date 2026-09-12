package com.hxcoe.crm.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.crm.client.OaApprovalClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * CRM审批集成服务。
 * <p>负责将CRM业务单据（销售订单）提交到OA统一审批中心，
 * 并构造审批回调URL，实现CRM与OA审批流的集成。</p>
 */
@Service
public class CrmApprovalIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(CrmApprovalIntegrationService.class);

    @Autowired
    private OaApprovalClient oaApprovalClient;

    /**
     * CRM服务基础URL（用于OA审批完成后回调）。
     * Docker网络内服务名为 crm，端口 8086。
     */
    @Value("${crm.approval.callback-base-url:http://crm:8086}")
    private String crmBaseUrl;

    /**
     * 提交销售订单审批。
     *
     * @param orderId       订单ID
     * @param orderNo       订单编号
     * @param customerName  客户名称
     * @param finalAmount   最终金额
     * @param initiatorId   发起人ID（取订单销售人员ID）
     * @param initiatorName 发起人名称
     * @return 是否提交成功
     */
    public boolean submitSalesOrderApproval(Long orderId, String orderNo, String customerName,
                                            Object finalAmount, Long initiatorId, String initiatorName) {
        Map<String, Object> formData = new HashMap<>();
        formData.put("orderNo", orderNo);
        formData.put("customerName", customerName);
        formData.put("finalAmount", finalAmount);

        Map<String, Object> request = new HashMap<>();
        request.put("sourceSystem", "crm");
        request.put("businessType", "sales_order");
        request.put("businessId", String.valueOf(orderId));
        request.put("businessNo", orderNo);
        request.put("title", "CRM销售订单审批-" + orderNo);
        request.put("description", "客户：" + (customerName != null ? customerName : "") + "，金额：" + finalAmount);
        // OA实例initiator_id非空，销售人员ID缺失时回退为系统管理员
        request.put("initiatorId", initiatorId != null ? initiatorId : 1L);
        request.put("initiatorName", initiatorName != null && !initiatorName.isBlank() ? initiatorName : "crm");
        request.put("formData", formData);
        // OA审批完成后回调CRM的地址
        request.put("callbackUrl", crmBaseUrl + "/api/v1/crm/approval/callback");

        try {
            Result<Object> result = oaApprovalClient.submit(request);
            // OA统一审批服务成功码为0（ResultCode.SUCCESS），网关/其他服务可能返回200，两者均视为成功
            Integer code = result != null ? result.getCode() : null;
            if (code != null && (code == 0 || code == 200)) {
                logger.info("OA审批提交成功: businessType=sales_order, businessId={}", orderId);
                return true;
            }
            logger.warn("OA审批提交返回异常: businessType=sales_order, businessId={}, result={}",
                    orderId, result != null ? result.getMsg() : "null");
            return false;
        } catch (Exception e) {
            // OA服务不可用时记录错误，不阻断本地提交（本地状态已置为submitted/审批中）
            logger.error("OA审批提交失败: businessType=sales_order, businessId={}, error={}",
                    orderId, e.getMessage(), e);
            return false;
        }
    }
}
