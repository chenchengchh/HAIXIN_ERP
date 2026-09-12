package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.wms.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.wms.service.WmsPoInstructionService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wms/integration")
public class IntegrationController {

    @Autowired
    private WmsPoInstructionService wmsPoInstructionService;

    @PostMapping("/scm/purchase-order-events")
    public ApiResponse<Map<String, Object>> receiveScmPoEvent(@RequestBody PurchaseOrderEventRequest req) {
        boolean ok = wmsPoInstructionService.applyScmEvent(req);
        if (!ok) {
            return badRequest("事件处理失败");
        }
        return success("接收成功", Map.of("eventKey", req.getEventKey(), "eventId", req.getEventId()));
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.error(400, message);
    }
}

