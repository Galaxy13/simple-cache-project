package com.galaxy13.websocket;

import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.storage.CacheController;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);
    private final CacheController cacheController;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public WebSocketHandler(CacheController cacheController) {
        this.cacheController = cacheController;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message){
        cacheController.subscribe("subTest", (response) -> {
            if (response.getCode().equals(MessageCode.SUBSCRIPTION_RESPONSE)){
                session.sendMessage(new TextMessage(response.getParameter("value")));
            }
        }, exception -> log.error("Error while subscribing to websocket", exception));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}
