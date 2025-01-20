package com.galaxy13.storage;

public abstract class CacheValue<T> {
    public CacheValue(String msg) {

    }

    String toCacheFormat() {
        return null;
    }

    protected T fromCacheFormat(String msg) {
        return null;
    }
}
