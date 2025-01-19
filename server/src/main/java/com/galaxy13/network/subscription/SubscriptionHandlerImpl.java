package com.galaxy13.network.subscription;

import com.galaxy13.network.MessageCode;
import com.galaxy13.network.message.MessageCreator;
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
    public void subscribe(String key, Channel channel) {
        subscriptions.computeIfAbsent(key, k -> new ArrayList<>()).add(channel);
        String msg = messageCreator.createCodeMessage(MessageCode.SUBSCRIPTION_SUCCESS);
        channel.writeAndFlush(msg);
    }

    @Override
    public void handleModification(Value value, String key) {
        List<Channel> subscriptionChannels = subscriptions.get(key);
        if (subscriptionChannels != null) {
            String msg = messageCreator.createSubscriptionResponse(key, value);
            subscriptionChannels.forEach(channel -> channel.writeAndFlush(msg));
        }
    }
}
