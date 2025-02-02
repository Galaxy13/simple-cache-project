package com.galaxy13.storage;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class WeakRefStorage extends AbstractStorage<String, Value> implements Storage<String, Value> {
    protected Map<String, WeakReference<Value>> weakReferenceMap;

    public WeakRefStorage(int capacity) {
        this.weakReferenceMap = new ConcurrentHashMap<>(capacity);
    }

    @Override
    public Optional<Value> get(String key) {
        WeakReference<Value> weakReference = weakReferenceMap.get(key);
        if (weakReference != null) {
            if (weakReference.get() != null) {
                return Optional.ofNullable(weakReference.get());
            } else {
                weakReferenceMap.remove(key);
            }
        }
        return Optional.empty();
    }

    @Override
    public Value put(String key, Value value) {
        weakReferenceMap.put(key, new WeakReference<>(value));
        return value;
    }

    @Override
    public Optional<Value> remove(String key) {
        return Optional.ofNullable(weakReferenceMap.remove(key)).map(WeakReference::get);
    }
}
