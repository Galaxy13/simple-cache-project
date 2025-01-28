package com.galaxy13.processor.storage;

import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;

import java.util.Optional;

public abstract class AbstractStorageProcessor implements StorageProcessor {
    protected final Storage storage;

    protected AbstractStorageProcessor(Storage storage) {
        this.storage = storage;
    }

    @Override
    public Optional<Value> process(CacheMessage message) {
        return Optional.empty();
    }

    @Override
    public boolean isModifying() {
        return false;
    }
}
