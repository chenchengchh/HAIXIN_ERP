package com.hxcoe.erp.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 基础事件模型
 * 所有事件的父类，继承自Spring的ApplicationEvent，包含事件的基本属性
 */
public class Event extends ApplicationEvent {
    
    /**
     * 事件ID
     */
    private String id;
    
    /**
     * 事件类型
     */
    private String type;
    
    /**
     * 事件来源名称
     */
    private String sourceName;
    
    /**
     * 事件时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 事件版本
     */
    private String version;
    
    /**
     * 事件数据
     */
    private Object data;
    
    /**
     * 构造方法
     */
    public Event(Object source, String type, String sourceName, Object data) {
        super(source);
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.sourceName = sourceName;
        this.timestamp = LocalDateTime.now();
        this.version = "1.0";
        this.data = data;
    }

    /**
     * 获取事件ID
     * @return 事件ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置事件ID
     * @param id 事件ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取事件类型
     * @return 事件类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置事件类型
     * @param type 事件类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取事件来源名称
     * @return 事件来源名称
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * 设置事件来源名称
     * @param sourceName 事件来源名称
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * 获取事件时间
     * @return 事件时间
     */
    public LocalDateTime getEventTimestamp() {
        return timestamp;
    }

    /**
     * 设置事件时间
     * @param timestamp 事件时间
     */
    public void setEventTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 获取事件版本
     * @return 事件版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置事件版本
     * @param version 事件版本
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取事件数据
     * @return 事件数据
     */
    public Object getData() {
        return data;
    }

    /**
     * 设置事件数据
     * @param data 事件数据
     */
    public void setData(Object data) {
        this.data = data;
    }
}
