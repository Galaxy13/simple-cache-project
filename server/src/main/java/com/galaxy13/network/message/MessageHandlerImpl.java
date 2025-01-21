package com.galaxy13.network.message;


import com.galaxy13.processor.ProcessorController;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MessageHandlerImpl implements MessageHandler{
    private final Logger logger = LoggerFactory.getLogger(MessageHandlerImpl.class);

    private final ProcessorController controller;

    public MessageHandlerImpl(ProcessorController controller) {
        this.controller = controller;
    }

    @Override
    public void handleMessage(String message, Channel channel) {
        Map<String, String> fields = getFields(message);
        controller.processMessage(fields, channel);
    }

    private Map<String, String> getFields(String message) {
        return Arrays.stream(message.split(";"))
                .map(s -> s.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1].strip()));
    }
}
