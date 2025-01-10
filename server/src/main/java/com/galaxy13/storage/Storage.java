package com.galaxy13.storage;

import java.util.Optional;

public interface Storage<K> {
    Optional<Value> get(K key);

    Value putIfAbsent(K key, Value value);

    Value remove(K key);

    Value put(K key, Value value);
}
