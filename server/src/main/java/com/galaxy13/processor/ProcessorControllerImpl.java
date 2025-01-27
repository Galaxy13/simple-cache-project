package com.galaxy13.processor;

import com.galaxy13.network.netty.decoder.CacheMessage;
import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.processor.subscription.SubscriptionHandler;
import com.galaxy13.processor.subscription.SubscriptionHandlerImpl;
import com.galaxy13.processor.storage.GetProcessor;
import com.galaxy13.processor.storage.StorageProcessor;
import com.galaxy13.processor.storage.PutProcessor;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProcessorControllerImpl implements ProcessorController {

    private final Map<Operation, StorageProcessor> storageProcessors;
    private final MessageCreator messageCreator;
    private final SubscriptionHandler subscriptionHandler;


    public ProcessorControllerImpl(Storage storage, MessageCreator messageCreator) {
        this.subscriptionHandler = new SubscriptionHandlerImpl(messageCreator);
        this.storageProcessors = new HashMap<>(){{
            put(Operation.GET, new GetProcessor(storage, messageCreator));
            put(Operation.PUT, new PutProcessor(storage, messageCreator));
        }};
        this.messageCreator = messageCreator;
    }

    @Override
    public void processMessage(CacheMessage message, Channel channel) {
        Operation operation = message.getOperation();
        if (operation == null) {
            channel.writeAndFlush(messageCreator.createCodeMessage(MessageCode.FORMAT_EXCEPTION));
        } else {
            if (operation.equals(Operation.SUBSCRIBE)) {
                subscriptionHandler.subscribe(message, channel);
                return;
            }
            StorageProcessor processor = storageProcessors.get(operation);
            if (processor == null) {
                channel.writeAndFlush(messageCreator.createCodeMessage(MessageCode.UNSUPPORTED_OPERATION));
            } else {
                Optional<Value> result = processor.process(channel, message);
                if (result.isPresent()) {
                    Value value = result.get();
                    channel.writeAndFlush(messageCreator.createResponse(value));
                    if (processor.isModifying()){
                        String key = message.getParameter("key");
                        this.subscriptionHandler.handleModification(value, key);
                    }
                } else {
                    channel.writeAndFlush(messageCreator.createCodeMessage(MessageCode.NOT_PRESENT));
                }
            }
        }
        channel.close().syncUninterruptibly();
    }
}
