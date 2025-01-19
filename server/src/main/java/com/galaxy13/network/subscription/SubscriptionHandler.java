package com.galaxy13.network.subscription;

import com.galaxy13.storage.Value;
import io.netty.channel.Channel;

public interface SubscriptionHandler {
    void subscribe(String key, Channel channel);

    void handleModification(Value value, String key);
}
