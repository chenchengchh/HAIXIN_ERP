package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scm.service.PurchaseOrderEventOutboxService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scm/admin/integration")
public class IntegrationAdminController {

    @Autowired
    private PurchaseOrderEventOutboxService purchaseOrderEventOutboxService;

    @PostMapping("/po-events/retry")
    public ApiResponse<Map<String, Object>> retryPoEvents(@RequestBody(required = false) Map<String, Object> body) {
        int processed = purchaseOrderEventOutboxService.trySendPendingBatch();
        return ApiResponse.success("成功", Map.of("processed", processed));
    }
}

