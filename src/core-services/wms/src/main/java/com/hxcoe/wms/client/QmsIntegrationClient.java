package com.hxcoe.wms.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.wms.client.dto.QmsReceiptTriggerRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * WMS → QMS 集成 Feign 客户端。
 * <p>用于 WMS 收货完成后触发 QMS 创建来料检验单（IQC），补齐质量闭环的入口段。</p>
 */
@FeignClient(name = "qms-service", path = "/api/v1/qms/integration", contextId = "wmsQmsIntegrationClient")
public interface QmsIntegrationClient {

    /**
     * 通知 QMS 收货已完成，触发 IQC 质检单创建。
     *
     * @param req 收货触发请求（采购单号、收货单号、明细行）
     * @return QMS 处理结果
     */
    @PostMapping("/wms/receipt-triggered")
    Result<Map<String, Object>> receiptTriggered(@RequestBody QmsReceiptTriggerRequest req);
}
