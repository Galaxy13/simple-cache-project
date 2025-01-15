package com.galaxy13.storage;

import java.util.HashMap;

public class StorageImpl<K, V> extends AbstractStorage<K, V> implements Storage<K, V> {
    public StorageImpl(int capacity) {
        this.storage = new HashMap<>(capacity);
    }
}
