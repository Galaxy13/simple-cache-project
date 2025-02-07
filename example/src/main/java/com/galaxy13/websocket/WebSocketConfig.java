package com.galaxy13.websocket;

import com.galaxy13.storage.CacheComponent;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final CacheComponent cacheComponent;

    public WebSocketConfig(CacheComponent cacheComponent) {
        this.cacheComponent = cacheComponent;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(cacheComponent), "/ws");
    }
}
