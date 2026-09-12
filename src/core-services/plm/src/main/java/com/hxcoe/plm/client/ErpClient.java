package com.hxcoe.plm.client;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.plm.client.dto.erp.ErpMaterialDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ERP服务Feign客户端：物料主数据权威在ERP，PLM发布产品时通过此客户端写入物料主数据。
 */
@FeignClient(name = "erp-service")
public interface ErpClient {

    /**
     * 按物料编码查询ERP物料。
     */
    @GetMapping("/api/v1/erp/basic-data/materials")
    ApiResponse<PageResult<ErpMaterialDTO>> getMaterials(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    /**
     * 在ERP创建物料主数据（创建成功后ERP通过Outbox事件同步BOM/SRM镜像）。
     */
    @PostMapping("/api/v1/erp/basic-data/materials")
    ApiResponse<ErpMaterialDTO> createMaterial(@RequestBody ErpMaterialDTO material);
}
