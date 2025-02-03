package com.galaxy13.storage;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class WeakRefStorage<K, V> extends AbstractStorage<K, V> implements Storage<K, V> {
    protected Map<K, WeakReference<V>> weakReferenceMap;

    public WeakRefStorage(int capacity) {
        this.weakReferenceMap = new ConcurrentHashMap<>(capacity);
    }

    @Override
    public Optional<V> get(K key) {
        WeakReference<V> weakReference = weakReferenceMap.get(key);
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
    public V put(K key, V value) {
        weakReferenceMap.put(key, new WeakReference<>(value));
        return value;
    }

    @Override
    public Optional<V> remove(K key) {
        return Optional.ofNullable(weakReferenceMap.remove(key)).map(WeakReference::get);
    }

    @Override
    public int size() {
        return weakReferenceMap.size();
    }
}
