package com.hxcoe.qms.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * SRM供应商绩效Feign客户端
 *
 * <p>用于将IQC检验结果推送至SRM，回写供应商质量绩效，形成 QMS→SRM 闭环。
 */
@FeignClient(name = "srm-service", path = "/api/v1/srm/integration/qms", contextId = "qmsSrmPerformanceClient")
public interface SrmPerformanceClient {

    /**
     * 推送IQC检验结果至SRM供应商绩效
     *
     * @param body 检验结果事件体（eventId/supplierId/supplierName/materialCode/inspectionNo/
     *             result/inspectionDate/batchQuantity/rejectedQuantity/poNo）
     * @return SRM处理结果，data 携带 supplierId 与 action
     */
    @PostMapping("/inspection-result")
    Result<Map<String, Object>> pushInspectionResult(@RequestBody Map<String, Object> body);
}
