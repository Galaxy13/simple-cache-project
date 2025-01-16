package com.galaxy13.storage;

public interface StorageClient {
    <T> Storage.ClientFuture put(String key, T value, Class<T> clazz);

    Storage.ClientFuture get(String key);
}
