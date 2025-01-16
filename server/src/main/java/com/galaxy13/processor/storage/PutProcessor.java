package com.galaxy13.processor.storage;

import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;

import java.util.Map;
import java.util.Optional;

public class PutProcessor implements MessageProcessor {
    @Override
    public Optional<Value> process(Storage storage, Map<String, String> fields) {
        String key = fields.get("key");
        String value = fields.get("value");
        String type = fields.get("value_type");
        if (key != null && value != null && type != null) {
            Value result = storage.put(key, new Value(type, value));
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }
}
