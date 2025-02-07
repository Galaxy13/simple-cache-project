package com.galaxy13.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.storage.CacheComponent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
    private final CacheComponent cacheComponent;

    private final ObjectMapper objectMapper;

    public WebSocketHandler(CacheComponent cacheComponent) {
        this.cacheComponent = cacheComponent;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) {
        cacheComponent.subscribe("subTest", response -> {
            if (response.getCode().equals(MessageCode.SUBSCRIPTION_RESPONSE) || response.getCode().equals(MessageCode.SUBSCRIPTION_SUCCESS)) {
                String value;
                if (response.getParameter("value") != null) {
                    value = response.getParameter("value");
                } else {
                    value = "no value";
                }
                try {
                    session.sendMessage(new TextMessage(value));
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, exception -> log.error("Error while subscribing to websocket", exception));
    }

    @Override
    public void handleMessage(@NotNull WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Ensure the message is a text message
        if (message.getPayload() instanceof String jsonMessage) {
            SecondValue value = objectMapper.readValue(jsonMessage, SecondValue.class);
            cacheComponent.put("subTest", value.getValue());
        }
    }

    static class SecondValue{
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
