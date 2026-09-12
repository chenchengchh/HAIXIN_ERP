package com.hxcoe.eam.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * EAM 调用 WMS 备件领用库存扣减接口的 Feign 客户端（EAM→WMS 备件领用库存联动闭环）。
 *
 * <p>EAM 备件领用审批通过并扣减本地库存后，将领用事件推送到 WMS 侧
 * {@code WmsSpareIntegrationController} 的 /eam/spare-issue 入口同步扣减 WMS 库存。
 */
@FeignClient(name = "wms-service", path = "/api/v1/wms/integration", contextId = "eamWmsSpareIssueClient")
public interface WmsSpareIssueClient {

    /**
     * 推送备件领用事件到 WMS 以扣减库存。
     *
     * @param body 事件体（eventId/spareCode/spareName/quantity/warehouseCode/workOrderNo/issueTime）
     * @return WMS 侧处理结果（spareCode/deductQty/remainingQty）
     */
    @PostMapping("/eam/spare-issue")
    Result<Map<String, Object>> deductSpareStock(@RequestBody Map<String, Object> body);
}
