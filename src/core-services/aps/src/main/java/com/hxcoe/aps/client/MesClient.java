package com.hxcoe.aps.client;

import com.hxcoe.aps.client.dto.mes.MesWorkOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mes-service")
public interface MesClient {
    /**
     * 创建MES工单。返回原始JSON字符串而非Result泛型对象：
     * 各服务Result结构存在差异（msg/message字段名不同、timestamp格式不同），
     * Feign泛型反序列化易失败，由调用方手动解析code字段判断结果
     */
    @PostMapping("/api/mes/work-orders")
    String createWorkOrder(@RequestBody MesWorkOrderDTO workOrder);
}
