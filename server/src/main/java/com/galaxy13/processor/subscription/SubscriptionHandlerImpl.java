package com.galaxy13.processor.subscription;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.network.message.response.CacheResponse;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SubscriptionHandlerImpl implements SubscriptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionHandlerImpl.class);

    private final Map<String, List<Channel>> subscriptions;
    private final Storage<String, Value> storage;

    public SubscriptionHandlerImpl(Storage<String, Value> storage) {
        this.subscriptions = new HashMap<>();
        this.storage = storage;
    }

    @Override
    public void subscribe(CacheMessage message, Channel channel) {
        String key = message.getParameter("key");
        if (key != null) {
            subscriptions.computeIfAbsent(key, k -> new ArrayList<>()).add(channel);
            logger.info("Subscription created on key: {} for channel: {}", key, channel);
            Optional<Value> value = storage.get(key);
            if (value.isPresent()) {
                channel.writeAndFlush(CacheResponse.createFrom(MessageCode.SUBSCRIPTION_SUCCESS, "key", key, "value", value.get().value()));
            } else {
                channel.writeAndFlush(CacheResponse.createFrom(MessageCode.SUBSCRIPTION_SUCCESS, "key", key));
            }
        } else {
            logger.warn("Subscribe failed: key is null");
            channel.writeAndFlush(CacheResponse.create(MessageCode.SUBSCRIPTION_ERROR)).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void handleModification(Value value, String key) {
        logger.trace("Subscription handling value {} for key {}", value, key);
        List<Channel> subscriptionChannels = subscriptions.get(key);
        if (subscriptionChannels != null) {
            var response = CacheResponse.createFrom(MessageCode.SUBSCRIPTION_RESPONSE,
                    "key", key,
                    "value", value.value());
            for (Channel channel : subscriptionChannels) {
                if (channel.isOpen()) {
                    channel.writeAndFlush(response);
                }
                logger.info("Subscription handling on key: {} for channel: {}", key, channel);
            }
        }
        logger.trace("No channels subscribed for key {}", key);
    }
}
