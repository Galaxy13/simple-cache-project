package com.galaxy13.network.blocking;

import com.galaxy13.network.message.Response;

public interface BlockingClient {
    Response sendMessage(String message) throws InterruptedException;
}
