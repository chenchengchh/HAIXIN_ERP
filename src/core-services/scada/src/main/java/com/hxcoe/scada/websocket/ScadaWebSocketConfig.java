package com.hxcoe.scada.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class ScadaWebSocketConfig implements WebSocketConfigurer {

    private final ScadaRealtimeWebSocketHandler realtimeWebSocketHandler;

    public ScadaWebSocketConfig(ScadaRealtimeWebSocketHandler realtimeWebSocketHandler) {
        this.realtimeWebSocketHandler = realtimeWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realtimeWebSocketHandler, "/ws/scada/realtime")
                .setAllowedOrigins("*");
    }
}

