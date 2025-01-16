package com.galaxy13.processor;

import com.galaxy13.processor.storage.MessageProcessor;

import java.util.Map;
import java.util.Optional;

public interface ProcessorController {
    Optional<MessageProcessor> getMessageProcessor(Map<String, String> fields);
}
