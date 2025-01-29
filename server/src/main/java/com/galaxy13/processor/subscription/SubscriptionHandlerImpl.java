package com.galaxy13.processor.subscription;

import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.response.CacheResponse;
import com.galaxy13.network.message.response.Response;
import com.galaxy13.storage.Value;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionHandlerImpl implements SubscriptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionHandlerImpl.class);

    private final Map<String, List<Channel>> subscriptions;

    public SubscriptionHandlerImpl() {
        subscriptions = new HashMap<>();
    }

    @Override
    public void subscribe(CacheMessage message, Channel channel) {
        String key = message.getParameter("key");
        if (key != null) {
            subscriptions.computeIfAbsent(key, k -> new ArrayList<>()).add(channel);
            logger.info("Subscription created on key: {} for channel: {}", key, channel);
            channel.writeAndFlush(CacheResponse.create(MessageCode.SUBSCRIPTION_SUCCESS));
        } else {
            logger.warn("Subscribe failed: key is null");
            channel.writeAndFlush(CacheResponse.create(MessageCode.SUBSCRIPTION_ERROR));
        }
    }

    @Override
    public void handleModification(Value value, String key) {
        logger.trace("Subscription handling value {} for key {}", value, key);
        List<Channel> subscriptionChannels = subscriptions.get(key);
        if (subscriptionChannels != null) {
            Response<Value> response = new CacheResponse(MessageCode.SUBSCRIPTION_RESPONSE, key, value);
            subscriptionChannels.forEach(channel ->
                    {
                        channel.writeAndFlush(response);
                        logger.trace("Sending subscription response to channel: {}", channel);
                    });
        }
        logger.trace("No channels subscribed for key {}", key);
    }
}
