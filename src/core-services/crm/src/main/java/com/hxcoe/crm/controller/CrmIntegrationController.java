package com.hxcoe.crm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.dto.les.LesSignCompletedEventDTO;
import com.hxcoe.crm.dto.integration.WmsOutboundShippedRequest;
import com.hxcoe.crm.service.CrmLesSignSyncService;
import com.hxcoe.crm.service.CrmSalesOrderDeliveryService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/crm/integration", "/crm/integration"})
public class CrmIntegrationController {

    @Autowired
    private CrmSalesOrderDeliveryService crmSalesOrderDeliveryService;

    @Autowired
    private CrmLesSignSyncService crmLesSignSyncService;

    @PostMapping("/wms/outbound-shipped")
    public ApiResponse<Map<String, Object>> receiveWmsOutboundShipped(@RequestBody WmsOutboundShippedRequest req) {
        if (req == null || req.getOutboundOrder() == null || req.getOutboundOrder().getOrderNo() == null || req.getOutboundOrder().getOrderNo().isBlank()) {
            return ApiResponse.error(400, "orderNo不能为空");
        }
        boolean ok = crmSalesOrderDeliveryService.applyWmsOutboundShipped(req);
        if (!ok) {
            return ApiResponse.error(409, "事件重复或处理失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req.getEventKey());
        data.put("eventId", req.getEventId());
        data.put("orderNo", req.getOutboundOrder().getOrderNo());
        data.put("sourceNo", req.getOutboundOrder().getSourceNo());
        return ApiResponse.success("成功", data);
    }

    /**
     * 接收 LES 签收完成事件，回写 CRM 销售订单状态为已签收（SIGNED）。
     * <p>P2-A: LES→CRM 签收回流入口。按 crmOrderNo 更新订单状态，幂等处理。</p>
     *
     * @param req LES 签收完成事件
     * @return 处理结果
     */
    @PostMapping("/les/sign-completed")
    public ApiResponse<Map<String, Object>> receiveLesSignCompleted(@RequestBody LesSignCompletedEventDTO req) {
        if (req == null || req.getSignVoucher() == null) {
            return ApiResponse.error(400, "签收凭证载荷不能为空");
        }
        if (req.getSignVoucher().getCrmOrderNo() == null || req.getSignVoucher().getCrmOrderNo().isBlank()) {
            return ApiResponse.error(400, "crmOrderNo不能为空");
        }
        boolean ok = crmLesSignSyncService.applyLesSignCompleted(req);
        if (!ok) {
            return ApiResponse.error(404, "销售订单不存在或处理失败: " + req.getSignVoucher().getCrmOrderNo());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", req.getEventId());
        data.put("idempotencyKey", req.getIdempotencyKey());
        data.put("crmOrderNo", req.getSignVoucher().getCrmOrderNo());
        return ApiResponse.success("成功", data);
    }
}
