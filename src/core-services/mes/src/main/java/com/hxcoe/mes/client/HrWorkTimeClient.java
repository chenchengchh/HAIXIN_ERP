package com.hxcoe.mes.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * HR报工工时Feign客户端
 *
 * <p>用于将生产报工工时数据推送至HR落库，形成 MES→HR 报工工时闭环。
 */
@FeignClient(name = "hr-service", path = "/api/v1/hr/integration", contextId = "mesHrWorkTimeClient")
public interface HrWorkTimeClient {

    /**
     * 推送报工工时事件至HR
     *
     * @param body 报工工时事件体（eventId/employeeNo/employeeName/workOrderNo/
     *             workHours/outputQuantity/reportDate/workstation）
     * @return HR处理结果，data 携带 received 与 duplicated
     */
    @PostMapping("/mes/work-report")
    Result<Map<String, Object>> pushWorkReport(@RequestBody Map<String, Object> body);
}
