package com.hxcoe.crm.client;

import com.hxcoe.crm.client.dto.WmsOutboundOrderDTO;
import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * WMS服务客户端
 */
@FeignClient(name = "wms-service")
public interface WmsClient {

    /**
     * 创建出库单
     * @param order 出库单数据
     * @return 结果
     */
    @PostMapping("/wms/outbound/orders")
    Result<Object> createOutboundOrder(@RequestBody WmsOutboundOrderDTO order);
}
