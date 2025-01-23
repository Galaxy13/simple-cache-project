package com.galaxy13.storage;

import java.util.Optional;

public interface Storage<K> {
    Optional<Value> get(K key);

    Optional<Value> remove(K key);

    Value put(K key, Value value);
}
