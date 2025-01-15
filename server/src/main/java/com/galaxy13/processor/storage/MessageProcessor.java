package com.galaxy13.processor.storage;

import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;

import java.util.Map;
import java.util.Optional;

public interface MessageProcessor<V> {
    Optional<Value<V>> process(
                            Storage<String, V> storage,
                            Map<String, String> fields);
}
