package com.galaxy13.storage;

import java.util.HashMap;

public class StorageImpl<K> extends AbstractStorage<K> implements Storage<K> {
    public StorageImpl(int capacity) {
        this.storage = new HashMap<>(capacity);
    }
}
