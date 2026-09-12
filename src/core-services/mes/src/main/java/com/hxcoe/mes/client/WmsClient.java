package com.hxcoe.mes.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.client.dto.wms.WmsAsnDTO;
import com.hxcoe.mes.client.dto.wms.WmsOutboundOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wms-service")
public interface WmsClient {
    @PostMapping("/wms/inbound/asn")
    Result<Object> createAsn(@RequestBody WmsAsnDTO asn);

    @PostMapping("/wms/outbound/orders")
    Result<Object> createOutboundOrder(@RequestBody WmsOutboundOrderDTO order);
}
