package com.galaxy13.storage;

import java.util.Map;

public abstract class AbstractStorage<K> implements Storage<K> {
    private Map<K, Value> storage;

    @Override
    public Value get(K key) {
        return storage.get(key);
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
