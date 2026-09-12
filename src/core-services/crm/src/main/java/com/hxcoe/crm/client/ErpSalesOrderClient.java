package com.hxcoe.crm.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * ERP销售订单集成客户端（O2C核心链路：CRM订单确认后推送ERP）
 */
@FeignClient(name = "erp-service", path = "/api/v1/erp/integration", contextId = "crmErpSalesOrderClient")
public interface ErpSalesOrderClient {

    /**
     * 推送已确认销售订单到ERP
     *
     * @param body 订单事件载荷（eventId/orderNo/customerId/customerName/totalAmount/currency/orderDate/status/items）
     * @return 处理结果，data 携带 orderNo 与 action（created/updated）
     */
    @PostMapping("/crm/sales-order")
    Result<Map<String, Object>> pushSalesOrder(@RequestBody Map<String, Object> body);
}
