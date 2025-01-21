package com.galaxy13.processor.storage;

import com.galaxy13.storage.Value;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.Optional;

public interface StorageProcessor {
    Optional<Value> process(Channel channel,
                            Map<String, String> fields);

    boolean isModifying();
}
