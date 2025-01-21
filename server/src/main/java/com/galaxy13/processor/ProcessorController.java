package com.galaxy13.processor;


import io.netty.channel.Channel;

import java.util.Map;

public interface ProcessorController {
    void processMessage(Map<String, String> fields, Channel channel);
}
