package com.galaxy13.storage;

import java.util.Optional;

@SuppressWarnings("unused")
public interface Storage<K, V> {
    Optional<V> get(K key);

    Optional<V> remove(K key);

    V put(K key, V value);
}
