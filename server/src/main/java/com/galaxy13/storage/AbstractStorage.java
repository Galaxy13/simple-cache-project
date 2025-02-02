package com.galaxy13.storage;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractStorage<K, V> implements Storage<K, V> {
    protected Map<K, V> storage;

    @Override
    public Optional<V> get(K key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public V put(K key, V value) {
        storage.put(key, value);
        return value;
    }

    @Override
    public Optional<V> remove(K key) {
        return Optional.ofNullable(storage.remove(key));
    }
}
