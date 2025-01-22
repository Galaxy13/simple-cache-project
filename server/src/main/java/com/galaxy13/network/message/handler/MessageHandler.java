package com.galaxy13.network.message.handler;

import io.netty.channel.Channel;

public interface MessageHandler {
    void handleMessage(String message, Channel channel);
}
