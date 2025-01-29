package com.galaxy13.network.blocking.handler;

import com.galaxy13.network.message.Response;

public interface MessageBlockingHandler {
    Response handle(String message);

    void exceptionCaught(Throwable throwable);
}
