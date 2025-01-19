package com.galaxy13.processor.storage;

import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;

import java.util.Map;
import java.util.Optional;

public interface MessageProcessor {
    Optional<Value> process(
                            Storage storage,
                            Map<String, String> fields);

    boolean isModifying();
}
