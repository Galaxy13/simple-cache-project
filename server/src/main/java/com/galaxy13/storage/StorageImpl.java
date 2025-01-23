package com.galaxy13.storage;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StorageImpl extends AbstractStorage<String> implements Storage<String> {
    protected Map<String, WeakReference<Value>> storage;

    public StorageImpl(int capacity) {
        this.storage = new ConcurrentHashMap<>(capacity);
    }

    @Override
    public Optional<Value> get(String key) {
        WeakReference<Value> weakReference = storage.get(key);
        if (weakReference != null) {
            if (weakReference.get() != null) {
                return Optional.ofNullable(weakReference.get());
            } else {
                storage.remove(key);
            }
        }
        return Optional.empty();
    }

    @Override
    public Value put(String key, Value value) {
        storage.put(key, new WeakReference<>(value));
        return value;
    }

    @Override
    public Optional<Value> remove(String key) {
        return Optional.ofNullable(storage.remove(key)).map(WeakReference::get);
    }
}
