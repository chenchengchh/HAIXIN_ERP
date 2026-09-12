package com.hxcoe.erp.event;

import lombok.Getter;
import lombok.Setter;

/**
 * 通用事件类
 * 用于处理各种类型的事件
 */
@Getter
@Setter
public class GenericEvent {
    
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
    public GenericEvent(String type, String source, Object data) {
        this.type = type;
        this.source = source;
        this.data = data;
    }
}
