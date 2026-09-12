package com.hxcoe.wms.client;

import com.hxcoe.common.dto.erp.IntegrationVoucherDTO;
import com.hxcoe.common.result.Result;
import com.hxcoe.wms.client.dto.ErpOutboundShippedRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "erp-service", path = "/api/v1/erp", contextId = "wmsErpClient")
public interface ErpClient {

    /**
     * 同步凭证到ERP
     */
    @PostMapping("/finance/vouchers/integration")
    Result<Void> syncVoucher(@RequestBody IntegrationVoucherDTO voucher);

    @PostMapping("/integration/wms/outbound-shipped")
    Result<Map<String, Object>> syncOutboundShipped(@RequestBody ErpOutboundShippedRequest request);
}
