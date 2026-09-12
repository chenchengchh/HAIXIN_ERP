package com.hxcoe.oa.service;

import com.hxcoe.oa.entity.ApprovalProcessInstanceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 审批回调服务。
 * <p>审批完成后通过HTTP回调通知发起方模块，实现OA审批结果回流到各业务模块。</p>
 * <p>回调流程：OA审批完成 → 构造回调事件 → HTTP POST到发起方模块的回调URL → 发起方更新业务状态</p>
 */
@Service
public class ApprovalCallbackService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalCallbackService.class);

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 发送审批回调通知。
     *
     * @param instance      审批实例
     * @param approvalResult 审批结果（approved/rejected/returned）
     * @param comment        审批意见
     * @param approverName   审批人名称
     * @return 回调是否成功
     */
    public boolean sendCallback(ApprovalProcessInstanceEntity instance, String approvalResult,
                                 String comment, String approverName) {
        if (instance == null) {
            logger.warn("审批实例为空，跳过回调");
            return false;
        }

        // 构造回调事件
        Map<String, Object> callbackEvent = buildCallbackEvent(instance, approvalResult, comment, approverName);

        // 从流程变量中获取回调URL
        String callbackUrl = extractCallbackUrl(instance);
        if (callbackUrl == null || callbackUrl.isBlank()) {
            logger.info("审批实例无回调URL，跳过回调 instanceId={}", instance.getId());
            return true;
        }

        try {
            logger.info("发送审批回调: instanceId={}, callbackUrl={}, result={}",
                    instance.getId(), callbackUrl, approvalResult);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(callbackEvent, headers);

            restTemplate.postForEntity(callbackUrl, entity, Map.class);

            logger.info("审批回调成功: instanceId={}, callbackUrl={}", instance.getId(), callbackUrl);
            return true;
        } catch (Exception e) {
            logger.error("审批回调失败: instanceId={}, callbackUrl={}, error={}",
                    instance.getId(), callbackUrl, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 构造审批回调事件。
     *
     * @param instance       审批实例
     * @param approvalResult 审批结果
     * @param comment        审批意见
     * @param approverName   审批人名称
     * @return 回调事件数据
     */
    private Map<String, Object> buildCallbackEvent(ApprovalProcessInstanceEntity instance,
                                                    String approvalResult, String comment, String approverName) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", "OA-CALLBACK-" + instance.getId() + "-" + System.currentTimeMillis());
        event.put("eventType", "approval.completed.v1");
        event.put("eventTime", LocalDateTime.now().toString());
        event.put("sourceSystem", "oa-service");

        // 业务信息
        Map<String, Object> business = new HashMap<>();
        business.put("businessId", instance.getId() != null ? String.valueOf(instance.getId()) : null);
        business.put("processCode", instance.getProcessCode());
        business.put("title", instance.getTitle());
        business.put("initiatorId", instance.getInitiatorId());
        business.put("initiatorName", instance.getInitiatorName());

        // 从流程变量中提取来源系统信息
        Map<String, Object> sourceInfo = extractSourceInfo(instance);
        business.putAll(sourceInfo);

        event.put("business", business);

        // 审批结果
        Map<String, Object> result = new HashMap<>();
        result.put("approvalResult", approvalResult);
        result.put("comment", comment);
        result.put("approverName", approverName);
        result.put("approvalTime", LocalDateTime.now().toString());
        result.put("instanceStatus", instance.getStatus());
        event.put("approvalResult", result);

        return event;
    }

    /**
     * 从流程变量JSON中提取回调URL。
     *
     * @param instance 审批实例
     * @return 回调URL
     */
    private String extractCallbackUrl(ApprovalProcessInstanceEntity instance) {
        String processVariables = instance.getProcessVariables();
        if (processVariables == null || processVariables.isBlank()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> vars = mapper.readValue(processVariables, Map.class);
            Object url = vars.get("callbackUrl");
            return url == null ? null : String.valueOf(url);
        } catch (Exception e) {
            logger.warn("解析流程变量失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从流程变量JSON中提取来源系统信息。
     *
     * @param instance 审批实例
     * @return 来源信息
     */
    private Map<String, Object> extractSourceInfo(ApprovalProcessInstanceEntity instance) {
        Map<String, Object> info = new HashMap<>();
        String processVariables = instance.getProcessVariables();
        if (processVariables == null || processVariables.isBlank()) {
            return info;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> vars = mapper.readValue(processVariables, Map.class);
            info.put("sourceSystem", vars.getOrDefault("sourceSystem", "unknown"));
            info.put("businessType", vars.getOrDefault("businessType", "unknown"));
            info.put("businessId", vars.getOrDefault("businessId", ""));
            info.put("businessNo", vars.getOrDefault("businessNo", ""));
        } catch (Exception e) {
            logger.warn("解析流程变量失败: {}", e.getMessage());
        }
        return info;
    }
}
