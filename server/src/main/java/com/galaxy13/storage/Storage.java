package com.galaxy13.storage;

public interface Storage<K> {
    Value get(K key);

    Value putIfAbsent(K key, Value value);

    Value remove(K key);

    Value put(K key, Value value);
}
