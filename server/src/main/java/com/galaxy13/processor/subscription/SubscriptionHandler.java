package com.galaxy13.processor.subscription;

import com.galaxy13.storage.Value;
import io.netty.channel.Channel;

import java.util.Map;

public interface SubscriptionHandler {
    void subscribe(Map<String, String> messageFields, Channel channel);

    void handleModification(Value value, String key);
}
