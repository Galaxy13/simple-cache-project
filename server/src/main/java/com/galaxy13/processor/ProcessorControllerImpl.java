package com.galaxy13.processor;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.network.message.response.CacheResponse;
import com.galaxy13.processor.storage.GetProcessor;
import com.galaxy13.processor.storage.PutProcessor;
import com.galaxy13.processor.storage.StorageProcessor;
import com.galaxy13.processor.subscription.SubscriptionHandler;
import com.galaxy13.processor.subscription.SubscriptionHandlerImpl;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.EnumMap;
import java.util.Optional;

public class ProcessorControllerImpl implements ProcessorController {

    private final EnumMap<Operation, StorageProcessor> storageProcessors;
    private final SubscriptionHandler subscriptionHandler;

    public ProcessorControllerImpl(Storage<String, Value> storage) {
        StorageProcessor getProcessor = new GetProcessor(storage);
        StorageProcessor putProcessor = new PutProcessor(storage);
        this.storageProcessors = new EnumMap<>(Operation.class);
        this.subscriptionHandler = new SubscriptionHandlerImpl(storage);
        this.storageProcessors.put(Operation.GET, getProcessor);
        this.storageProcessors.put(Operation.PUT, putProcessor);
    }

    @Override
    public void processMessage(CacheMessage message, Channel channel) {
        Operation operation = message.getOperation();
        if (operation.equals(Operation.SUBSCRIBE)) {
            subscriptionHandler.subscribe(message, channel);
            return;
        }
        StorageProcessor processor = storageProcessors.get(operation);
        if (processor == null) {
            channel.writeAndFlush((CacheResponse.create(MessageCode.UNSUPPORTED_OPERATION)))
                    .addListener(ChannelFutureListener.CLOSE);
        } else {
            executeProcessorOperation(processor, message, channel).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private ChannelFuture executeProcessorOperation(StorageProcessor processor, CacheMessage message, Channel channel) {
        Optional<Value> result = processor.process(message);
        if (result.isPresent()) {
            Value value = result.get();
            String key = message.getParameter("key");
            if (processor.isModifying()){
                this.subscriptionHandler.handleModification(value, key);
            }
            return channel.writeAndFlush(CacheResponse.createFrom(MessageCode.OK, "value", value.value(), "key", key));
        } else {
            return channel.writeAndFlush(CacheResponse.create(MessageCode.NOT_PRESENT));
        }
    }
}
