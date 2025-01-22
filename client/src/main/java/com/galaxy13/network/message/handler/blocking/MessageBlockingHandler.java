package com.galaxy13.network.message.handler.blocking;

import com.galaxy13.network.message.Response;

public interface MessageBlockingHandler {
    Response handle(String message);

    void exceptionCaught(Throwable throwable);
}
