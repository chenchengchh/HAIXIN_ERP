package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.wms.entity.PoInstructionEntity;
import com.hxcoe.wms.repository.PoInstructionRepository;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wms/admin/po-instructions")
public class PoInstructionAdminController {

    @Autowired
    private PoInstructionRepository poInstructionRepository;

    @GetMapping("/by-no/{poNo}")
    public ApiResponse<Map<String, Object>> byPoNo(@PathVariable String poNo) {
        PoInstructionEntity e = poInstructionRepository.findByPoNo(poNo).orElse(null);
        if (e == null) {
            return notFound("未找到");
        }
        return success("查询成功", Map.of(
                "poNo", e.getPoNo(),
                "lastEventType", e.getLastEventType(),
                "updatedTime", e.getUpdatedTime()
        ));
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}

