package com.galaxy13.processor.storage;

import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;

import java.util.Optional;

public class GetProcessor extends AbstractStorageProcessor implements StorageProcessor {
    public GetProcessor(Storage<String> storage) {
        super(storage);
    }

    @Override
    public Optional<Value> process(CacheMessage message) {
        String key = message.getParameter("key");
        if (key != null) {
            Optional<Value> value = storage.get(key);
            if (value.isPresent()) {
                Value v = value.get();
                return Optional.of(v);
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isModifying() {
        return false;
    }
}
