package com.galaxy13.processor.storage;

import com.galaxy13.network.netty.decoder.CacheMessage;
import com.galaxy13.storage.Value;
import io.netty.channel.Channel;

import java.util.Optional;

public interface StorageProcessor {
    Optional<Value> process(Channel channel,
                            CacheMessage message);

    boolean isModifying();
}
