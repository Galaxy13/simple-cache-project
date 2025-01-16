package com.galaxy13.storage;

import java.util.HashMap;

public class StorageImpl extends AbstractStorage implements Storage {
    public StorageImpl(int capacity) {
        this.storage = new HashMap<>(capacity);
    }
}
