package com.galaxy13;

public interface StorageClient {
    <T> T put(String key, T value);

    <T> T get(String key);
}
