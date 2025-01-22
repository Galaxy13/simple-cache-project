package com.galaxy13.network.message.handler.netty;

public interface MessageAsyncHandler {
    void handleMessage(String message);

    void exceptionCaught(Throwable cause);
}
