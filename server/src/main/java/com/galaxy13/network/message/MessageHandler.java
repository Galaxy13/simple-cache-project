package com.galaxy13.network.message;

import io.netty.channel.Channel;

public interface MessageHandler {
    void handleMessage(String message, Channel channel);
}
