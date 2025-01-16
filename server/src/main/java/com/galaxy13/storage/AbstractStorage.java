package com.galaxy13.storage;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractStorage implements Storage {
    protected Map<String, Value> storage;

    @Override
    public Optional<Value> get(String key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public Value put(String key, Value value) {
        storage.put(key, value);
        return value;
    }

    @Override
    public Optional<Value> remove(String key) {
        return Optional.ofNullable(storage.remove(key));
    }
}
