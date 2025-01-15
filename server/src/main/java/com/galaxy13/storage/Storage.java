package com.galaxy13.storage;

import java.util.Optional;

public interface Storage<K, V> {
    Optional<Value<V>> get(K key);

    Optional<Value<V>> remove(K key);

    Value<V> put(K key, Value<V> value);
}
