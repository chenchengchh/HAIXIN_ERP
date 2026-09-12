package com.hxcoe.bom.controller;

import com.hxcoe.bom.client.dto.erp.MaterialEventRequest;
import com.hxcoe.bom.service.MaterialMirrorService;
import com.hxcoe.common.result.Result;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/bom/integration", "/bom/integration"})
public class IntegrationController {

    @Autowired
    private MaterialMirrorService materialMirrorService;

    @PostMapping("/erp/material-events")
    public Result<Map<String, Object>> receiveMaterialEvents(@RequestBody MaterialEventRequest req) {
        materialMirrorService.applyErpEvent(req);
        Map<String, Object> data = new HashMap<>();
        data.put("eventKey", req == null ? null : req.getEventKey());
        return Result.success("成功", data);
    }
}

