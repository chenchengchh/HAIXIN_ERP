package com.hxcoe.les.client;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.dto.les.LesSignCompletedEventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * LES → CRM 签收回写 Feign 客户端。
 * <p>LES 签收凭证创建后，通过 Outbox 投递签收完成事件到 CRM，回写销售订单状态。</p>
 */
@FeignClient(name = "crm-service", path = "/api/v1/crm/integration", contextId = "lesCrmSignClient")
public interface CrmSignClient {

    /**
     * 推送签收完成事件到 CRM。
     *
     * @param event 签收完成事件
     * @return CRM 处理结果
     */
    @PostMapping("/les/sign-completed")
    ApiResponse<Map<String, Object>> signCompleted(@RequestBody LesSignCompletedEventDTO event);
}
