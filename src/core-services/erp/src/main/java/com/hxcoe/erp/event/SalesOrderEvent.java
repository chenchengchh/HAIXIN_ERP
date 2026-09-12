package com.hxcoe.erp.event;

import lombok.Getter;
import lombok.Setter;

/**
 * 销售订单事件
 * 用于销售订单创建、更新、删除等操作的事件通知
 */
@Getter
@Setter
public class SalesOrderEvent {
    
    /**
     * 销售订单ID
     */
    private String orderId;
    
    /**
     * 客户ID
     */
    private String customerId;
    
    /**
     * 订单状态
     */
    private String status;
    
    /**
     * 事件类型
     */
    private String type;
    
    /**
     * 事件来源
     */
    private String source;
    
    /**
     * 事件数据
     */
    private Object data;
    
    /**
     * 构造方法
     */
    public SalesOrderEvent(String type, String source, Object data) {
        this.type = type;
        this.source = source;
        this.data = data;
    }
}
