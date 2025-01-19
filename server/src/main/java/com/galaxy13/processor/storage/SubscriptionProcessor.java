package com.galaxy13.processor.storage;

import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;

import java.util.Map;
import java.util.Optional;

public class SubscriptionProcessor implements MessageProcessor {
    @Override
    public Optional<Value> process(Storage storage, Map<String, String> fields) {
        return Optional.empty();
    }

    @Override
    public boolean isModifying() {
        return false;
    }
}
