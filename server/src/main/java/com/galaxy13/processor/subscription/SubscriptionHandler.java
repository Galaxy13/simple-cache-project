package com.galaxy13.processor.subscription;

import com.galaxy13.network.netty.decoder.CacheMessage;
import com.galaxy13.storage.Value;
import io.netty.channel.Channel;

public interface SubscriptionHandler {
    void subscribe(CacheMessage message, Channel channel);

    void handleModification(Value value, String key);
}
