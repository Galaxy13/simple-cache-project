package com.galaxy13.storage;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractStorage<K, V> implements Storage<K, V> {
    protected Map<K, Value<V>> storage;

    @Override
    public Optional<Value<V>> get(K key) {
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public Value<V> put(K key, Value<V> value) {
        return storage.put(key, value);
    }

    @Override
    public Optional<Value<V>> remove(K key) {
        return Optional.ofNullable(storage.remove(key));
    }
}
