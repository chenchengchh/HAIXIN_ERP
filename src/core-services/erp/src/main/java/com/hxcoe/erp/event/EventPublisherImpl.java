package com.hxcoe.erp.event;

import org.springframework.context.ApplicationEventPublisher; 
import org.springframework.stereotype.Component;

/**
 * 事件发布者实现类
 * 使用Spring的ApplicationEventPublisher实现事件发布
 */
@Component
public class EventPublisherImpl implements EventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    /**
     * 构造方法
     */
    public EventPublisherImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    @Override
    public void publish(Event event) {
        // 使用Spring的事件发布机制发布事件
        applicationEventPublisher.publishEvent(event);
    }
    
    @Override
    public void publish(String type, String source, Object data) {
        // 创建通用事件对象
        GenericEvent event = new GenericEvent(type, source, data);
        // 直接发布事件，不需要包装成Event对象
        applicationEventPublisher.publishEvent(event);
    }
}
