package com.hxcoe.mes.client;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.mes.client.dto.bom.BomStructureDTO;
import com.hxcoe.mes.client.dto.bom.MaterialDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bom-service")
public interface BomClient {

    @GetMapping("/bom/material")
    ApiResponse<PageResult<MaterialDTO>> getMaterials(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @GetMapping("/bom/structure/tree/{productId}")
    ApiResponse<BomStructureDTO> getBomTree(@PathVariable("productId") Long productId);
}
