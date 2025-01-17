package com.galaxy13.storage;

import com.galaxy13.storage.action.ResourceSupplier;

public interface StorageClient {
    <T> Storage.ClientFuture put(String key, T value);

    Storage.ClientFuture get(String key);

    <T> Storage.ClientFuture putIfAbsent(String key, T value);

    <T> Storage.ClientFuture putIfAbsent(String key, ResourceSupplier<T> supplier);
}
