package com.galaxy13.network.message;

import com.galaxy13.network.MessageCode;
import com.galaxy13.network.subscription.SubscriptionHandler;
import com.galaxy13.network.subscription.SubscriptionHandlerImpl;
import com.galaxy13.processor.storage.MessageProcessor;
import com.galaxy13.processor.ProcessorController;
import com.galaxy13.processor.storage.SubscriptionProcessor;
import com.galaxy13.storage.Storage;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MessageHandlerImpl implements MessageHandler{
    private final Logger logger = LoggerFactory.getLogger(MessageHandlerImpl.class);

    private final Storage storage;
    private final ProcessorController controller;
    private final MessageCreator messageCreator;
    private final SubscriptionHandler subscriptionHandler;

    public MessageHandlerImpl(Storage storage, ProcessorController controller, MessageCreator messageCreator) {
        this.storage = storage;
        this.controller = controller;
        this.messageCreator = messageCreator;
        this.subscriptionHandler = new SubscriptionHandlerImpl(messageCreator);
    }

    @Override
    public void handleMessage(String message, Channel channel) {
        Map<String, String> fields = getFields(message);
        String msg;
        Optional<MessageProcessor> messageProcessor = controller.getMessageProcessor(fields);
        if (messageProcessor.isPresent()) {
            var processor = messageProcessor.get();
            if (processor instanceof SubscriptionProcessor) {
                subscriptionHandler.subscribe(fields.get("key"), channel);
                return;
            }
            var result = processor.process(storage, fields);
            if (result.isPresent()){
                msg = messageCreator.createResponse(result.get());
                if(processor.isModifying()){
                    subscriptionHandler.handleModification(result.get(), fields.get("key"));
                    return;
                }
            } else {
                msg = messageCreator.createCodeMessage(MessageCode.NOT_PRESENT);
            }
        } else {
            msg = messageCreator.createCodeMessage(MessageCode.UNSUPPORTED_OPERATION);
        }
        channel.writeAndFlush(msg);
    }

    private Map<String, String> getFields(String message) {
        return Arrays.stream(message.split(";"))
                .map(s -> s.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1].strip()));
    }
}
