package com.galaxy13.processor;

import com.galaxy13.processor.storage.MessageProcessor;

import java.util.Map;
import java.util.Optional;

public interface ProcessorController<V> {
    Optional<MessageProcessor<V>> getMessageProcessor(Map<String, String> fields);
}
