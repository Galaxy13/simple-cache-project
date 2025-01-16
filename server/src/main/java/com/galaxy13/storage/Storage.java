package com.galaxy13.storage;

import java.util.Optional;

public interface Storage {
    Optional<Value> get(String key);

    Optional<Value> remove(String key);

    Value put(String key, Value value);
}
