package com.galaxy13.processor.subscription;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.creator.MessageCreator;
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
    private final MessageCreator messageCreator;

    public SubscriptionHandlerImpl(MessageCreator creator) {
        subscriptions = new HashMap<>();
        messageCreator = creator;
    }

    @Override
    public void subscribe(Map<String, String> messageFields, Channel channel) {
        String key = messageFields.get("key");
        if (key != null) {
            subscriptions.computeIfAbsent(key, k -> new ArrayList<>()).add(channel);
            logger.info("Subscription created on key: {} for channel: {}", key, channel);
            String msg = messageCreator.createCodeMessage(MessageCode.SUBSCRIPTION_SUCCESS);
            channel.writeAndFlush(msg);
        } else {
            logger.warn("Subscribe failed: key is null");
            channel.writeAndFlush(messageCreator.createCodeMessage(MessageCode.SUBSCRIPTION_ERROR));
        }
    }

    @Override
    public void handleModification(Value value, String key) {
        logger.trace("Subscription handling value {} for key {}", value, key);
        List<Channel> subscriptionChannels = subscriptions.get(key);
        if (subscriptionChannels != null) {
            String msg = messageCreator.createSubscriptionResponse(key, value);
            subscriptionChannels.forEach(channel ->
                    {
                        channel.writeAndFlush(msg);
                        logger.trace("Sending subscription response to channel: {}", channel);
                    });
        }
        logger.trace("No channels subscribed for key {}", key);
    }
}
