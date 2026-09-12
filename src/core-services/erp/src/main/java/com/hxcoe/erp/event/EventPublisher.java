package com.hxcoe.erp.event;

/**
 * 事件发布者接口
 * 用于发布事件到事件总线
 */
public interface EventPublisher {
    
    /**
     * 发布事件
     * @param event 事件对象
     */
    void publish(Event event);
    
    /**
     * 发布指定类型的事件
     * @param type 事件类型
     * @param source 事件来源
     * @param data 事件数据
     */
    void publish(String type, String source, Object data);
}
