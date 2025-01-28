package com.galaxy13.processor.storage;

import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.storage.Value;

import java.util.Optional;

public interface StorageProcessor {
    Optional<Value> process(CacheMessage message);

    boolean isModifying();
}
