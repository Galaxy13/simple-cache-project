package com.galaxy13.processor;


import com.galaxy13.network.netty.decoder.CacheMessage;
import io.netty.channel.Channel;

public interface ProcessorController {
    void processMessage(CacheMessage message, Channel channel);
}
