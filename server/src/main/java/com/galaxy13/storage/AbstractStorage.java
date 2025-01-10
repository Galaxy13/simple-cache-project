package com.galaxy13.storage;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractStorage<K> implements Storage<K> {
    protected Map<K, Value> storage;

    @Override
    public Optional<Value> get(K key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public Value put(K key, Value value) {
        return storage.put(key, value);
    }

    @Override
    public Value remove(K key) {
        return storage.remove(key);
    }

    @Override
    public Value putIfAbsent(K key, Value value) {
        return storage.putIfAbsent(key, value);
    }
}
