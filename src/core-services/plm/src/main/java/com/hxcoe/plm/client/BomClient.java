package com.hxcoe.plm.client;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.plm.client.dto.bom.BomHeaderDTO;
import com.hxcoe.plm.client.dto.bom.BomLineDTO;
import com.hxcoe.plm.client.dto.bom.MaterialCreateDTO;
import com.hxcoe.plm.client.dto.bom.MaterialDTO;
import com.hxcoe.plm.client.dto.bom.MaterialEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "bom-service")
public interface BomClient {

    @GetMapping("/bom/material")
    ApiResponse<PageResult<MaterialDTO>> getMaterials(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @PostMapping("/bom/material")
    ApiResponse<MaterialDTO> createMaterial(@RequestBody MaterialCreateDTO material);

    @PostMapping("/bom/structure")
    ApiResponse<BomHeaderDTO> createBom(@RequestBody BomHeaderDTO bomHeader);

    @PostMapping("/bom/structure/{headerId}/lines")
    ApiResponse<BomLineDTO> addBomLine(@PathVariable("headerId") Long headerId, @RequestBody BomLineDTO bomLine);

    /**
     * 通过BOM官方集成端点发布物料事件（幂等收件箱去重），用于即时刷新BOM物料镜像。
     */
    @PostMapping("/api/v1/bom/integration/erp/material-events")
    ApiResponse<Map<String, Object>> publishMaterialEvent(@RequestBody MaterialEventRequest request);
}
