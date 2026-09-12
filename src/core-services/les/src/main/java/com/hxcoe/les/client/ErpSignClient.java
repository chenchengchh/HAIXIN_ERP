package com.hxcoe.les.client;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.dto.les.LesSignCompletedEventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * LES → ERP 签收回写 Feign 客户端。
 * <p>LES 签收凭证创建后，通过 Outbox 投递签收完成事件到 ERP，回写订单/出库状态。</p>
 */
@FeignClient(name = "erp-service", path = "/api/v1/erp/integration", contextId = "lesErpSignClient")
public interface ErpSignClient {

    /**
     * 推送签收完成事件到 ERP。
     *
     * @param event 签收完成事件
     * @return ERP 处理结果
     */
    @PostMapping("/les/sign-completed")
    ApiResponse<Map<String, Object>> signCompleted(@RequestBody LesSignCompletedEventDTO event);
}
