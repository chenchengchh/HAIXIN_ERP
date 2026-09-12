package com.hxcoe.erp.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 事件监听器
 * 用于监听和处理事件
 */
@Component
public class CustomEventListener {
    
    /**
     * 监听销售订单事件
     * @param event 销售订单事件
     */
    @EventListener
    public void handleSalesOrderEvent(SalesOrderEvent event) {
        System.out.println("Received sales order event: " + event);
        // 这里可以添加销售订单事件的处理逻辑
    }
    
    /**
     * 监听通用事件
     * @param event 通用事件
     */
    @EventListener
    public void handleGenericEvent(GenericEvent event) {
        System.out.println("Received generic event: " + event);
        // 这里可以添加通用事件的处理逻辑
    }
}
