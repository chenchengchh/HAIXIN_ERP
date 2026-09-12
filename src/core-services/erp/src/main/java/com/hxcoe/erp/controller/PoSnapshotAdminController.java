package com.hxcoe.erp.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.erp.service.ErpPoSnapshotService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/erp/admin/po-snapshots")
public class PoSnapshotAdminController {

    @Autowired
    private ErpPoSnapshotService erpPoSnapshotService;

    @GetMapping("/by-no/{poNo}")
    public ApiResponse<Map<String, Object>> byPoNo(@PathVariable String poNo) {
        Map<String, Object> row = erpPoSnapshotService.getByPoNo(poNo);
        if (row == null) {
            return ApiResponse.error(404, "未找到");
        }
        return ApiResponse.success("查询成功", Map.of(
                "poNo", row.get("po_no"),
                "lastEventType", row.get("last_event_type"),
                "updatedTime", row.get("updated_time")
        ));
    }
}

