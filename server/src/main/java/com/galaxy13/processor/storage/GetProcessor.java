package com.galaxy13.processor.storage;

import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;

import java.util.Map;
import java.util.Optional;

public class GetProcessor implements MessageProcessor {
    @Override
    public Optional<Value> process(Storage storage, Map<String, String> fields) {
        String key = fields.get("key");
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
}
