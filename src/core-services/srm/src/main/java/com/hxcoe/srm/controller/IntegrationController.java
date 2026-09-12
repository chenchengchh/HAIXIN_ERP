package com.hxcoe.srm.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.srm.dto.integration.MaterialEventRequest;
import com.hxcoe.srm.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.srm.service.MaterialMirrorService;
import com.hxcoe.srm.service.PurchaseOrderMirrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/srm/integration", "/srm/integration"})
public class IntegrationController {

    @Autowired
    private PurchaseOrderMirrorService purchaseOrderMirrorService;

    @Autowired
    private MaterialMirrorService materialMirrorService;

    @PostMapping("/scm/purchase-order-events")
    public Result<Map<String, Object>> receivePurchaseOrderEvents(@RequestBody PurchaseOrderEventRequest req) {
        purchaseOrderMirrorService.applyScmEvent(req);
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req == null ? null : req.getEventKey());
        return Result.success("成功", data);
    }

    @PostMapping("/erp/material-events")
    public Result<Map<String, Object>> receiveMaterialEvents(@RequestBody MaterialEventRequest req) {
        materialMirrorService.applyErpEvent(req);
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req == null ? null : req.getEventKey());
        return Result.success("成功", data);
    }
}
