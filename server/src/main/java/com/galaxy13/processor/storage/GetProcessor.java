package com.galaxy13.processor.storage;

import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;

import java.util.Map;
import java.util.Optional;

public class GetProcessor<V> implements MessageProcessor<V> {
    @Override
    public Optional<Value<V>> process(Storage<String, V> storage, Map<String, String> fields) {
        String key = fields.get("key");
        String valueType = fields.get("valueType");
        if (key != null && valueType != null) {
            Optional<Value<V>> value = storage.get(key);
            if (value.isPresent()) {
                Value<V> v = value.get();
                if (v.type().equals(valueType)){
                    return Optional.of(v);
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
